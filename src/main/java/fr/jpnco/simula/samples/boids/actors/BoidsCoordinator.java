package fr.jpnco.simula.samples.boids.actors;

import fr.jpnco.simula.Actor;
import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.actors.Barrier;
import fr.jpnco.simula.actors.BarrierMode;
import fr.jpnco.simula.engine.ActorDelegate;
import fr.jpnco.simula.engine.EventImpl;
import fr.jpnco.simula.engine.IdBuilder;
import fr.jpnco.simula.samples.boids.FlockParameters;
import fr.jpnco.simula.samples.boids.states.BoidState;
import fr.jpnco.simula.samples.boids.states.BoidView;
import fr.jpnco.simula.samples.boids.states.FlockState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * The coordinator actor that pilots the boids flocking simulation and assembles its snapshots.
 *
 * <p>It owns the fixed flock of {@link Boid} actors and the {@link Barrier} that synchronizes their
 * per-tick reports. The simulation is driven by events: on each {@link Engine#TIME_EVENT} the
 * coordinator opens a new tick and broadcasts {@link Topics#NEXT_BOID_STATES}, carrying the tick
 * and the flock state of the previous tick, so each boid decides on a deterministic, already-known
 * state.
 *
 * <p>The synchronization of the reports is delegated to a simula {@link Barrier} actor (in {@code
 * CYCLIC} mode with distinct-source counting) that counts the {@link Topics#BOID_STATE} reports.
 * When all boids of the tick have reported, the barrier fires {@link Topics#BOIDS_READY}. Once the
 * coordinator has seen that completion signal it pulls the last state of every boid (each boid
 * stores its state before broadcasting it), assembles an immutable {@link FlockState} and
 * broadcasts it on {@link Topics#NEW_STATE}.
 *
 * <p>All randomness uses a fixed seed, so the same scenario produces the same outcome in every
 * execution mode (FR-007, FR-008). The final outcome aggregates the boid count and the total
 * distance travelled: the sum over all boids of their per-tick displacement magnitude.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-003, FR-005, FR-007, FR-008, FR-009, SC-001, SC-002, SC-003.
 */
public final class BoidsCoordinator implements Actor {

  /** The fixed seed that makes the random initial placement reproducible across execution modes. */
  private static final long RANDOM_SEED = 20260924L;

  /** The delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The unique identity of the coordinator. */
  private final Integer id;

  /** The flocking parameters of the simulation. */
  private final FlockParameters params;

  /** The number of simulated seconds the demo runs before stopping. */
  private final int durationSeconds;

  /** The boid actors of the flock. */
  private final List<Boid> boids = new ArrayList<>();

  /** The latch released once the demo completes. */
  private final CountDownLatch done = new CountDownLatch(1);

  /** The current simulated time. */
  private int simTime = 0;

  /** Whether every boid of the current tick has reported (set by {@link Topics#BOIDS_READY}). */
  private boolean boidsReady = false;

  /** The flock state of the most recently completed tick, given to the boids. */
  private FlockState currentFlock;

  /** The latest immutable snapshot, broadcast after each completed tick. */
  private volatile FlockState state;

  /** The total distance travelled by all boids, accumulated over the whole run. */
  private double totalDistance = 0.0;

  /**
   * Creates a coordinator actor and subscribes it to the {@link Engine#TIME_EVENT} and {@link
   * Topics#BOIDS_READY} topics. Participates in: FR-002, FR-003, FR-005, FR-007, FR-008, FR-009,
   * SC-001, SC-002, SC-003.
   *
   * @param engine the engine this actor lives on
   * @param params the flocking parameters
   * @param durationSeconds the number of simulated seconds to run before stopping
   */
  public BoidsCoordinator(
      final Engine engine, final FlockParameters params, final int durationSeconds) {
    this.params = params;
    this.durationSeconds = durationSeconds;
    id = IdBuilder.nextId();
    delegate = ActorDelegate.createDelegate(engine, this);
    engine.subscribe(this, Engine.TIME_EVENT);
    engine.subscribe(this, Topics.BOIDS_READY);
  }

  /**
   * Returns the actor delegate that drives this actor. Participates in: FR-002, FR-003, FR-005,
   * FR-007, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this actor. Participates in: FR-002, FR-003, FR-005, FR-007,
   * FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @return the id
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Creates and starts the boid actors with a fixed seed, records their initial positions as the
   * first flock state, then creates and starts the synchronization barrier. Participates in:
   * FR-002, FR-003, FR-005, FR-007, FR-008, FR-009, SC-001, SC-002, SC-003.
   */
  public void seed() {
    final Random random = new Random(RANDOM_SEED);
    final List<BoidView> initialViews = new ArrayList<>();
    for (int i = 0; i < params.getBoidCount(); i++) {
      final double x = random.nextDouble() * params.getWorldWidth();
      final double y = random.nextDouble() * params.getWorldHeight();
      final double angle = random.nextDouble() * TWO_PI;
      final double speed = params.getMaxSpeed();
      final double vx = Math.cos(angle) * speed;
      final double vy = Math.sin(angle) * speed;
      final Boid boid =
          new Boid(
              getEngine(), i, x, y, vx, vy, params.getPerceptionRadius(), params.getMaxSpeed());
      boids.add(boid);
      initialViews.add(new BoidView(i, x, y, vx, vy));
      getEngine().registerAndStart(boid);
    }
    currentFlock = new FlockState(0, initialViews, params.getWorldWidth(), params.getWorldHeight());
    final Barrier barrier =
        new Barrier(
            getEngine(),
            boids.size(),
            Topics.BOID_STATE,
            Topics.BOIDS_READY,
            BarrierMode.CYCLIC,
            true);
    getEngine().registerAndStart(barrier);
  }

  /**
   * Processes a received event, dispatching on its topic: {@link Engine#TIME_EVENT} opens a new
   * tick and {@link Topics#BOIDS_READY} records that all boids reported. Participates in: FR-002,
   * FR-003, FR-005, FR-007, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    final String topic = event.getTopic();
    if (Engine.TIME_EVENT.equals(topic)) {
      openTick();
    } else if (Topics.BOIDS_READY.equals(topic)) {
      boidsReady = true;
      assemble();
    }
  }

  /**
   * Opens a new simulated tick: resets the completion flag and asks every boid to report against
   * the previous tick's flock state. Participates in: FR-002, FR-003, FR-005, FR-007, FR-008,
   * FR-009, SC-001, SC-002, SC-003.
   */
  private void openTick() {
    simTime++;
    boidsReady = false;
    getEngine().signal(EventImpl.createEvent(Topics.NEXT_BOID_STATES, this, simTime, currentFlock));
  }

  /**
   * Assembles and broadcasts the {@link FlockState} once the coordinator has seen the boids
   * completion signal for the current tick, accumulates the total distance, and stops the engine
   * once the configured duration is reached. Participates in: FR-002, FR-003, FR-005, FR-007,
   * FR-008, FR-009, SC-001, SC-002, SC-003.
   */
  private void assemble() {
    final List<BoidView> views = new ArrayList<>(boids.size());
    for (final Boid boid : boids) {
      final BoidState boidState = boid.getLastState();
      views.add(
          new BoidView(
              boidState.getId(),
              boidState.getX(),
              boidState.getY(),
              boidState.getVx(),
              boidState.getVy()));
      totalDistance += Math.hypot(boidState.getVx(), boidState.getVy());
    }
    views.sort(Comparator.comparingInt(BoidView::getId));
    final FlockState flock =
        new FlockState(simTime, views, params.getWorldWidth(), params.getWorldHeight());
    currentFlock = flock;
    state = flock;
    getEngine().signal(EventImpl.createEvent(Topics.NEW_STATE, this, flock));
    if (simTime >= durationSeconds) {
      getEngine().stop();
      done.countDown();
    }
  }

  /**
   * Returns the latest immutable snapshot of the flock, or {@code null} if none has been produced
   * yet. Participates in: FR-002, FR-003, FR-005, FR-007, FR-008, SC-001, SC-002, SC-003.
   *
   * @return the latest snapshot, may be {@code null}
   */
  public FlockState snapshot() {
    return state;
  }

  /**
   * Returns the number of boids in the flock. Participates in: FR-002, FR-003, FR-005, SC-001.
   *
   * @return the boid count
   */
  public int getBoidCount() {
    return boids.size();
  }

  /**
   * Returns the total distance travelled by all boids over the whole run. Participates in: FR-002,
   * FR-003, FR-007, FR-008, SC-002, SC-003.
   *
   * @return the total distance
   */
  public double getTotalDistance() {
    return totalDistance;
  }

  /**
   * Applies a live parameter change to every boid of the flock. The GUI sliders call this on the
   * Event Dispatch Thread while the simulation runs; each {@link Boid} stores the values in {@code
   * volatile} fields so the change is picked up on its own event loop thread at the next tick.
   * Participates in: FR-003, FR-009.
   *
   * @param updated the parameters to apply to every boid
   */
  public void updateParameters(final FlockParameters updated) {
    for (final Boid boid : boids) {
      boid.updateParameters(
          updated.getSeparationWeight(),
          updated.getAlignmentWeight(),
          updated.getCohesionWeight(),
          updated.getPerceptionRadius(),
          updated.getMaxSpeed());
    }
  }

  /**
   * Returns the latch released once the demo completes. Participates in: FR-005, SC-002.
   *
   * @return the completion latch
   */
  public CountDownLatch getDoneLatch() {
    return done;
  }

  /** The full circle in radians, used to seed initial velocities. */
  private static final double TWO_PI = 2.0 * Math.PI;
}
