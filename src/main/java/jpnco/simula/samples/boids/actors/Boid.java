package jpnco.simula.samples.boids.actors;

import jpnco.simula.Actor;
import jpnco.simula.Engine;
import jpnco.simula.Event;
import jpnco.simula.engine.ActorDelegate;
import jpnco.simula.engine.EventImpl;
import jpnco.simula.samples.boids.states.BoidModel;
import jpnco.simula.samples.boids.states.BoidState;
import jpnco.simula.samples.boids.states.FlockState;

/**
 * An autonomous {@link Actor} representing a single boid in the flock. It holds its own identity,
 * position, velocity, perception radius and maximum speed. On each {@link Topics#NEXT_BOID_STATES}
 * event it receives the flock state of the previous tick, applies the Reynolds flocking rules
 * (separation, alignment, cohesion) against the neighbouring boids within its perception radius
 * (FR-003), advances its position by its velocity (dt = 1) and wraps it around the toroidal world
 * (FR-004), then broadcasts its new {@link BoidState} on {@link Topics#BOID_STATE}.
 *
 * <p>The world dimensions are read from the previous {@link FlockState} each tick. Each boid
 * decides on the previous tick's flock state so its movement does not depend on report arrival
 * order (determinism, FR-007, FR-008).
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-003, FR-004, FR-008.
 */
final class Boid implements Actor {

  /** The default separation weight applied by every boid (configurable via the pure model). */
  private static final double SEPARATION_WEIGHT = 1.0;

  /** The default alignment weight applied by every boid (configurable via the pure model). */
  private static final double ALIGNMENT_WEIGHT = 1.0;

  /** The default cohesion weight applied by every boid (configurable via the pure model). */
  private static final double COHESION_WEIGHT = 1.0;

  /** The delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The unique identity of the boid. */
  private final Integer id;

  /** The position of the boid on the horizontal axis. */
  private double x;

  /** The position of the boid on the vertical axis. */
  private double y;

  /** The velocity of the boid on the horizontal axis. */
  private double vx;

  /** The velocity of the boid on the vertical axis. */
  private double vy;

  /** The radius within which neighbouring boids are considered. */
  private final double perceptionRadius;

  /** The maximum speed (the velocity magnitude is capped at this value). */
  private final double maxSpeed;

  /** The most recent state this boid broadcast, readable by the coordinator. */
  private BoidState lastState;

  /**
   * Creates a boid actor and subscribes it to {@link Topics#NEXT_BOID_STATES}. Participates in:
   * FR-002, FR-003, FR-004, FR-008.
   *
   * @param engine the engine this actor lives on
   * @param id the boid identity
   * @param x the initial horizontal position
   * @param y the initial vertical position
   * @param vx the initial horizontal velocity
   * @param vy the initial vertical velocity
   * @param perceptionRadius the perception radius
   * @param maxSpeed the maximum speed
   */
  Boid(
      final Engine engine,
      final int id,
      final double x,
      final double y,
      final double vx,
      final double vy,
      final double perceptionRadius,
      final double maxSpeed) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.vx = vx;
    this.vy = vy;
    this.perceptionRadius = perceptionRadius;
    this.maxSpeed = maxSpeed;
    delegate = ActorDelegate.createDelegate(engine, this);
    engine.subscribe(this, Topics.NEXT_BOID_STATES);
  }

  /**
   * Returns the actor delegate that drives this actor. Participates in: FR-002, FR-003, FR-004,
   * FR-008.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this boid. Participates in: FR-002, FR-003, FR-004, FR-008.
   *
   * @return the id
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Processes a received event: on {@link Topics#NEXT_BOID_STATES} it advances the boid and
   * broadcasts its new state. Participates in: FR-002, FR-003, FR-004, FR-008.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    if (Topics.NEXT_BOID_STATES.equals(event.getTopic())) {
      final int tick = (Integer) event.getParameters()[0];
      final FlockState previous = (FlockState) event.getParameters()[1];
      advance(tick, previous);
    }
  }

  /**
   * Advances the boid by one simulated tick: it computes the next velocity from the neighbouring
   * boids of the previous flock state, applies it (dt = 1) and wraps the position around the
   * toroidal world, then broadcasts the resulting {@link BoidState} on {@link Topics#BOID_STATE}.
   * Participates in: FR-002, FR-003, FR-004, FR-008.
   *
   * @param tick the current simulated tick
   * @param previous the flock state of the previous tick, whose boids are the neighbours
   */
  private void advance(final int tick, final FlockState previous) {
    final double[] nextVelocity =
        BoidModel.nextVelocity(
            x,
            y,
            vx,
            vy,
            previous.getBoids(),
            SEPARATION_WEIGHT,
            ALIGNMENT_WEIGHT,
            COHESION_WEIGHT,
            perceptionRadius,
            maxSpeed,
            previous.getWorldWidth(),
            previous.getWorldHeight());
    vx = nextVelocity[0];
    vy = nextVelocity[1];
    final double[] position =
        BoidModel.wrap(x + vx, y + vy, previous.getWorldWidth(), previous.getWorldHeight());
    x = position[0];
    y = position[1];
    lastState = new BoidState(tick, id, x, y, vx, vy);
    getEngine().signal(EventImpl.createEvent(Topics.BOID_STATE, this, lastState));
  }

  /**
   * Returns the most recent state this boid broadcast, or {@code null} if it has not broadcast one
   * yet. The state is stored before it is broadcast, so once the {@link
   * jpnco.simula.actors.Barrier} has fired for a tick every boid's stored state is current for that
   * tick and the coordinator can pull it to assemble the snapshot. Participates in: FR-002, FR-003,
   * FR-004, FR-008.
   *
   * @return the last broadcast state, may be {@code null}
   */
  BoidState getLastState() {
    return lastState;
  }

  /**
   * Returns the current horizontal position of the boid. Participates in: FR-002, FR-003, FR-004.
   *
   * @return the horizontal position
   */
  double getX() {
    return x;
  }

  /**
   * Returns the current vertical position of the boid. Participates in: FR-002, FR-003, FR-004.
   *
   * @return the vertical position
   */
  double getY() {
    return y;
  }

  /**
   * Returns the current horizontal velocity of the boid. Participates in: FR-002, FR-003, FR-004.
   *
   * @return the horizontal velocity
   */
  double getVx() {
    return vx;
  }

  /**
   * Returns the current vertical velocity of the boid. Participates in: FR-002, FR-003, FR-004.
   *
   * @return the vertical velocity
   */
  double getVy() {
    return vy;
  }
}
