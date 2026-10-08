package fr.jpnco.simula.samples.trafficlight.actors;

import fr.jpnco.simula.Actor;
import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.actors.Barrier;
import fr.jpnco.simula.actors.BarrierMode;
import fr.jpnco.simula.engine.ActorDelegate;
import fr.jpnco.simula.engine.EventImpl;
import fr.jpnco.simula.engine.IdBuilder;
import fr.jpnco.simula.samples.trafficlight.states.Direction;
import fr.jpnco.simula.samples.trafficlight.states.GridState;
import fr.jpnco.simula.samples.trafficlight.states.LightState;
import fr.jpnco.simula.samples.trafficlight.states.TrafficLightState;
import fr.jpnco.simula.samples.trafficlight.states.VehicleState;
import fr.jpnco.simula.samples.trafficlight.states.VehicleView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * The coordinator actor that pilots the traffic-light simulation and assembles its snapshots.
 *
 * <p>It owns the fixed fleet of {@link Vehicle} actors and the {@link CrossingTrafficLight} actors
 * (one per intersection that has a light; the four corners are always green and have none). The
 * simulation is driven by events: on each {@link Engine#TIME_EVENT} the coordinator opens a new
 * tick and broadcasts {@link Topics#NEXT_TRAFFIC_LIGHT_STATES} (asking each light to report its
 * future state) and {@link Topics#NEXT_VEHICLES_STATES} (carrying the light table of the previous
 * tick, so vehicles decide on a deterministic, already-known state).
 *
 * <p>The synchronization of the reports is delegated to two simula {@link Barrier} actors (in
 * {@code CYCLIC} mode with distinct-source counting): a lights barrier counts the {@link
 * Topics#TRAFFIC_LIGHT_STATE} reports and a vehicles barrier counts the {@link
 * Topics#VEHICLES_STATE} reports. When all lights of the tick have reported, the lights barrier
 * fires {@link Topics#LIGHTS_READY}; when all vehicles of the tick have reported, the vehicles
 * barrier fires {@link Topics#VEHICLES_READY}. Once the coordinator has seen both completions it
 * pulls the last state of every light and every vehicle (each actor stores its state before
 * broadcasting it, so it is current when the barrier fires), assembles an immutable {@link
 * GridState} and broadcasts it on {@link Topics#NEW_STATE}.
 *
 * <p>All randomness uses a fixed seed: each light draws its own green duration in [20, 30] seconds
 * and each vehicle its own seed, so the same scenario produces the same outcome in every execution
 * mode.
 *
 * <p>The grid has {@value #GRID_SIZE} by {@value #GRID_SIZE} cells (blocks) bounded by {@value
 * #INTERSECTIONS} by {@value #INTERSECTIONS} roads, one intersection at every crossing of a
 * vertical and a horizontal road. A fixed fleet of {@value #INITIAL_VEHICLES} vehicles travels
 * along the roads; a vehicle only enters the next intersection when its light is green for its
 * direction, and it stops at the intersection otherwise, and it may turn randomly at an
 * intersection. At the edge of the grid a vehicle cannot leave it: it must turn right or left, so
 * the fleet keeps circulating within the grid.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-003, FR-006, FR-008, FR-009, SC-001, SC-002, SC-003.
 */
public final class TrafficCoordinator implements Actor {

  /** The number of cells (blocks) along each side of the grid. */
  public static final int GRID_SIZE = 3;

  /** The number of roads (and thus intersections) along each side: one more than the cells. */
  public static final int INTERSECTIONS = GRID_SIZE + 1;

  /** The length of a road segment between two consecutive intersections, in metres. */
  public static final double SEGMENT_LENGTH = 100.0;

  /**
   * Half the width of an intersection crossing, in metres. A vehicle stops at the traffic light,
   * {@value #SEGMENT_LENGTH} minus this amount before the next intersection centre, rather than in
   * the middle of the crossing.
   */
  public static final double INTERSECTION_HALF = 5.0;

  /** The position, in metres along a segment, of the traffic light of the next intersection. */
  public static final double LIGHT_POSITION = SEGMENT_LENGTH - INTERSECTION_HALF;

  /** The length of the green segment drawn at an intersection, in metres. */
  public static final double GREEN_SEGMENT_METERS = 20.0;

  /** The probability that a vehicle changes direction at an intersection. */
  public static final double TURN_PROBABILITY = 0.25;

  /** The number of vehicles that populate the grid at the start of the demo. */
  public static final int INITIAL_VEHICLES = 12;

  /** The lowest possible vehicle speed, in kilometres per hour. */
  private static final double MIN_SPEED_KMH = 15.0;

  /** The highest possible vehicle speed, in kilometres per hour. */
  private static final double MAX_SPEED_KMH = 45.0;

  /** The conversion factor from kilometres per hour to metres per second. */
  private static final double KMH_TO_MPS = 3.6;

  /** The fixed seed that makes random movement reproducible across execution modes. */
  private static final long RANDOM_SEED = 20260924L;

  /** The lowest green duration (inclusive) a light can draw, in seconds. */
  private static final int GREEN_MIN = 20;

  /** The highest green duration (inclusive) a light can draw, in seconds. */
  private static final int GREEN_MAX = 30;

  /** The fixed orange duration of every light, in seconds. */
  private static final int ORANGE_DURATION = 3;

  /** The actor delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The unique identity of the coordinator. */
  private final Integer id;

  /** The number of simulated seconds the demo runs before stopping. */
  private final int durationSeconds;

  /** The light actors of the grid. */
  private final List<CrossingTrafficLight> lights = new ArrayList<>();

  /** The vehicle actors of the fleet. */
  private final List<Vehicle> vehicles = new ArrayList<>();

  /** The per-cell number of vehicle arrivals so far. */
  private final int[][] crossings = new int[INTERSECTIONS][INTERSECTIONS];

  /** The latch released once the demo completes. */
  private final CountDownLatch done = new CountDownLatch(1);

  /** The current simulated time. */
  private int simTime = 0;

  /** The final total number of crossings after the demo completes. */
  private int totalCrossings = 0;

  /** Whether every light of the current tick has reported (set by {@link Topics#LIGHTS_READY}). */
  private boolean lightsReady = false;

  /**
   * Whether every vehicle of the current tick has reported (set by {@link Topics#VEHICLES_READY}).
   */
  private boolean vehiclesReady = false;

  /** The light table of the most recently completed tick, given to the vehicles. */
  private volatile LightState[][][] lastLights = newLightTable();

  /** The barrier that fires {@link Topics#LIGHTS_READY} once all lights have reported. */
  private Barrier lightsBarrier;

  /** The barrier that fires {@link Topics#VEHICLES_READY} once all vehicles have reported. */
  private Barrier vehiclesBarrier;

  /** The latest immutable snapshot, broadcast after each completed tick. */
  private volatile GridState state;

  /**
   * Creates a coordinator actor and subscribes it to the {@link Engine#TIME_EVENT}, {@link
   * Topics#LIGHTS_READY} and {@link Topics#VEHICLES_READY} topics. Participates in: FR-002, FR-003,
   * FR-006, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @param engine the engine this actor lives on
   * @param durationSeconds the number of simulated seconds to run before stopping
   */
  public TrafficCoordinator(final Engine engine, final int durationSeconds) {
    this.durationSeconds = durationSeconds;
    id = IdBuilder.nextId();
    delegate = ActorDelegate.createDelegate(engine, this);
    engine.subscribe(this, Engine.TIME_EVENT);
    engine.subscribe(this, Topics.LIGHTS_READY);
    engine.subscribe(this, Topics.VEHICLES_READY);
  }

  /**
   * Returns the actor delegate that drives this actor. Participates in: FR-002, FR-003, FR-006,
   * FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this actor. Participates in: FR-002, FR-003, FR-006, FR-008,
   * FR-009, SC-001, SC-002, SC-003.
   *
   * @return the id
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Creates and starts the light and vehicle actors with a fixed seed, then creates and starts the
   * two synchronization barriers. Participates in: FR-002, FR-003, FR-006, FR-008, FR-009, SC-001,
   * SC-002, SC-003.
   */
  public void seed() {
    final Random random = new Random(RANDOM_SEED);
    for (int row = 0; row < INTERSECTIONS; row++) {
      for (int col = 0; col < INTERSECTIONS; col++) {
        if (isCorner(row, col)) {
          continue;
        }
        final int greenDuration = GREEN_MIN + random.nextInt(GREEN_MAX - GREEN_MIN + 1);
        final int phaseOffset = (row * INTERSECTIONS + col) % 60;
        final CrossingTrafficLight light =
            new CrossingTrafficLight(
                getEngine(), row, col, greenDuration, ORANGE_DURATION, phaseOffset);
        lights.add(light);
        getEngine().registerAndStart(light);
      }
    }
    for (int i = 0; i < INITIAL_VEHICLES; i++) {
      final double speedKmh = MIN_SPEED_KMH + random.nextDouble() * (MAX_SPEED_KMH - MIN_SPEED_KMH);
      final double speedMps = speedKmh / KMH_TO_MPS;
      final int row = random.nextInt(INTERSECTIONS);
      final int col = random.nextInt(INTERSECTIONS);
      final Vehicle vehicle =
          new Vehicle(
              getEngine(),
              i,
              row,
              col,
              randomOnGridDirection(random, row, col),
              speedMps,
              RANDOM_SEED + i);
      vehicles.add(vehicle);
      getEngine().registerAndStart(vehicle);
    }
    lightsBarrier =
        new Barrier(
            getEngine(),
            lights.size(),
            Topics.TRAFFIC_LIGHT_STATE,
            Topics.LIGHTS_READY,
            BarrierMode.CYCLIC,
            true);
    vehiclesBarrier =
        new Barrier(
            getEngine(),
            vehicles.size(),
            Topics.VEHICLES_STATE,
            Topics.VEHICLES_READY,
            BarrierMode.CYCLIC,
            true);
    getEngine().registerAndStart(lightsBarrier);
    getEngine().registerAndStart(vehiclesBarrier);
  }

  /**
   * Returns a uniformly random direction that keeps the vehicle on the grid from the given
   * intersection (its destination must lie inside the grid). Participates in: FR-003, FR-009.
   *
   * @param random the randomness source
   * @param row the row of the intersection
   * @param col the column of the intersection
   * @return a direction whose destination is on the grid
   */
  private Direction randomOnGridDirection(final Random random, final int row, final int col) {
    final Direction[] values = Direction.values();
    final Direction start = values[random.nextInt(values.length)];
    for (int i = 0; i < values.length; i++) {
      final Direction candidate = values[(start.ordinal() + i) % values.length];
      if (isOnGrid(row + candidate.rowDelta(), col + candidate.colDelta())) {
        return candidate;
      }
    }
    return start;
  }

  /**
   * Processes a received event, dispatching on its topic: {@link Engine#TIME_EVENT} opens a new
   * tick, {@link Topics#LIGHTS_READY} records that all lights reported and {@link
   * Topics#VEHICLES_READY} records that all vehicles reported. Participates in: FR-002, FR-003,
   * FR-006, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    final String topic = event.getTopic();
    if (Engine.TIME_EVENT.equals(topic)) {
      openTick();
    } else if (Topics.LIGHTS_READY.equals(topic)) {
      lightsReady = true;
      assembleIfComplete();
    } else if (Topics.VEHICLES_READY.equals(topic)) {
      vehiclesReady = true;
      assembleIfComplete();
    }
  }

  /**
   * Opens a new simulated tick: resets the completion flags and asks the lights and the vehicles to
   * report. Participates in: FR-002, FR-003, FR-006, FR-008, FR-009, SC-001, SC-002, SC-003.
   */
  private void openTick() {
    simTime++;
    lightsReady = false;
    vehiclesReady = false;
    getEngine().signal(EventImpl.createEvent(Topics.NEXT_TRAFFIC_LIGHT_STATES, this, simTime));
    getEngine()
        .signal(EventImpl.createEvent(Topics.NEXT_VEHICLES_STATES, this, simTime, lastLights));
  }

  /**
   * Assembles and broadcasts the {@link GridState} once the coordinator has seen both the lights
   * and the vehicles completion signals for the current tick, and stops the engine once the
   * configured duration is reached. Participates in: FR-002, FR-003, FR-006, FR-008, FR-009,
   * SC-001, SC-002, SC-003.
   */
  private void assembleIfComplete() {
    if (!lightsReady || !vehiclesReady) {
      return;
    }
    final LightState[][][] current = newLightTable();
    for (final CrossingTrafficLight light : lights) {
      final TrafficLightState lightState = light.getLastState();
      current[lightState.getRow()][lightState.getCol()][0] = lightState.getNs();
      current[lightState.getRow()][lightState.getCol()][1] = lightState.getEw();
    }
    lastLights = copyLights(current);
    state = buildState(current);
    getEngine().signal(EventImpl.createEvent(Topics.NEW_STATE, this, state));
    totalCrossings = totalCrossings();
    if (simTime >= durationSeconds) {
      getEngine().stop();
      done.countDown();
    }
  }

  /**
   * Builds an immutable snapshot of the current grid by pulling each vehicle's last reported state
   * and updating the crossing counters from the cells it entered. Participates in: FR-002, FR-003,
   * FR-006, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @param current the current-tick light table
   * @return the snapshot
   */
  private GridState buildState(final LightState[][][] current) {
    final List<VehicleView> views = new ArrayList<>(vehicles.size());
    for (final Vehicle vehicle : vehicles) {
      final VehicleState vehicleState = vehicle.getLastState();
      for (final int[] cell : vehicleState.getEnteredCells()) {
        crossings[cell[0]][cell[1]]++;
      }
      views.add(
          new VehicleView(
              vehicleState.getId(),
              vehicleState.getRow(),
              vehicleState.getCol(),
              vehicleState.getDirection(),
              vehicleState.getDistanceInSegment()));
    }
    views.sort(Comparator.comparingInt(VehicleView::getId));
    final LightState[][] northSouth = new LightState[INTERSECTIONS][INTERSECTIONS];
    final LightState[][] eastWest = new LightState[INTERSECTIONS][INTERSECTIONS];
    for (int row = 0; row < INTERSECTIONS; row++) {
      for (int col = 0; col < INTERSECTIONS; col++) {
        northSouth[row][col] = current[row][col][0];
        eastWest[row][col] = current[row][col][1];
      }
    }
    return new GridState(simTime, views, northSouth, eastWest, crossings);
  }

  /**
   * Returns the latest immutable snapshot of the grid, or {@code null} if none has been produced
   * yet. Participates in: FR-002, FR-003, FR-006, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @return the latest snapshot, may be {@code null}
   */
  public GridState snapshot() {
    return state;
  }

  /**
   * Returns the final total number of crossings after the demo completes. Participates in: FR-002,
   * FR-003, FR-006, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @return the total crossings
   */
  public int getTotalCrossings() {
    return totalCrossings;
  }

  /**
   * Returns the latch released once the demo completes. Participates in: FR-002, FR-003, FR-006,
   * FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @return the completion latch
   */
  public CountDownLatch getDoneLatch() {
    return done;
  }

  /**
   * Returns whether the given intersection is one of the four outer corners of the grid.
   * Participates in: FR-002, FR-003, FR-006, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @param row the row of the intersection
   * @param col the column of the intersection
   * @return {@code true} if the intersection is a corner
   */
  private static boolean isCorner(final int row, final int col) {
    final int last = INTERSECTIONS - 1;
    return (row == 0 || row == last) && (col == 0 || col == last);
  }

  /**
   * Returns whether the given cell lies within the grid bounds. Participates in: FR-002, FR-003,
   * FR-006, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @param row the row to check
   * @param col the column to check
   * @return {@code true} if the cell is inside the grid
   */
  private static boolean isOnGrid(final int row, final int col) {
    return row >= 0 && row < INTERSECTIONS && col >= 0 && col < INTERSECTIONS;
  }

  /**
   * Returns the total number of crossings counted so far. Participates in: FR-002, FR-003, FR-006,
   * FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @return the total crossings
   */
  private int totalCrossings() {
    int total = 0;
    for (final int[] row : crossings) {
      for (final int value : row) {
        total += value;
      }
    }
    return total;
  }

  /**
   * Returns a new light table with every cell set to green for both bands. Participates in: FR-002,
   * FR-003, FR-006, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @return the light table
   */
  private static LightState[][][] newLightTable() {
    final LightState[][][] table = new LightState[INTERSECTIONS][INTERSECTIONS][2];
    fillGreen(table);
    return table;
  }

  /**
   * Sets every cell of the given light table to green for both bands. Participates in: FR-002,
   * FR-003, FR-006, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @param table the table to fill
   */
  private static void fillGreen(final LightState[][][] table) {
    for (int row = 0; row < INTERSECTIONS; row++) {
      for (int col = 0; col < INTERSECTIONS; col++) {
        table[row][col][0] = LightState.GREEN;
        table[row][col][1] = LightState.GREEN;
      }
    }
  }

  /**
   * Returns a defensive copy of the given light table. Participates in: FR-002, FR-003, FR-006,
   * FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @param source the table to copy
   * @return the copy
   */
  private static LightState[][][] copyLights(final LightState[][][] source) {
    final LightState[][][] copy = new LightState[INTERSECTIONS][INTERSECTIONS][2];
    for (int row = 0; row < INTERSECTIONS; row++) {
      for (int col = 0; col < INTERSECTIONS; col++) {
        copy[row][col][0] = source[row][col][0];
        copy[row][col][1] = source[row][col][1];
      }
    }
    return copy;
  }
}
