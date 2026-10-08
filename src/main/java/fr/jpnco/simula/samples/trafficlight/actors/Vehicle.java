package fr.jpnco.simula.samples.trafficlight.actors;

import fr.jpnco.simula.Actor;
import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.engine.ActorDelegate;
import fr.jpnco.simula.engine.EventImpl;
import fr.jpnco.simula.engine.IdBuilder;
import fr.jpnco.simula.samples.trafficlight.states.Direction;
import fr.jpnco.simula.samples.trafficlight.states.LightState;
import fr.jpnco.simula.samples.trafficlight.states.VehicleState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An autonomous {@link Actor} representing a single vehicle travelling on the grid. It holds its
 * own identity, current cell, direction, fixed speed and the distance already travelled into the
 * current segment. On each {@link Topics#NEXT_VEHICLES_STATES} event it receives the light table of
 * the previous tick, advances itself (respecting the traffic light of the intersection it is
 * approaching, stopping at the light when it is red, and choosing its next direction) and
 * broadcasts its new {@link VehicleState}. Vehicles persist for the whole demo and keep circulating
 * within the grid: at an edge they turn right or left rather than leaving it.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-003, FR-004, FR-005, FR-009.
 */
final class Vehicle implements Actor {

  /** The delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The business identity of the vehicle, carried in its {@link VehicleState} and views. */
  private final Integer vehicleId;

  /** The unique identity of the actor, taken from the framework {@link IdBuilder}. */
  private final Integer actorId;

  /** The fixed speed of the vehicle in metres per second (chosen between 15 and 45 km/h). */
  private final double speed;

  /** The vehicle's own randomness source, used for direction choice. */
  private final Random random;

  /** The current row of the vehicle (the origin of the segment it is on). */
  private int row;

  /** The current column of the vehicle (the origin of the segment it is on). */
  private int col;

  /** The current direction of the vehicle. */
  private Direction direction;

  /** The distance already travelled into the current segment, in metres. */
  private double distanceInSegment;

  /** The most recent state this vehicle broadcast, readable by the coordinator. */
  private VehicleState lastState;

  /**
   * Creates a vehicle actor and subscribes it to {@link Topics#NEXT_VEHICLES_STATES}. Participates
   * in: FR-003, FR-004, FR-005, FR-009.
   *
   * @param engine the engine this actor lives on
   * @param id the vehicle business identity, carried in the states and views it produces
   * @param row the initial row
   * @param col the initial column
   * @param direction the initial direction
   * @param speed the fixed speed in metres per second
   * @param seed the randomness seed
   */
  Vehicle(
      final Engine engine,
      final int id,
      final int row,
      final int col,
      final Direction direction,
      final double speed,
      final long seed) {
    vehicleId = id;
    actorId = IdBuilder.nextId();
    this.row = row;
    this.col = col;
    this.direction = direction;
    this.speed = speed;
    random = new Random(seed);
    delegate = ActorDelegate.createDelegate(engine, this);
    engine.subscribe(this, Topics.NEXT_VEHICLES_STATES);
  }

  /**
   * Returns the actor delegate that drives this actor. Participates in: FR-003, FR-004, FR-005,
   * FR-009.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this actor, distinct from the business {@code vehicleId} carried
   * in its states. Participates in: FR-003, FR-004, FR-005, FR-009.
   *
   * @return the actor id
   */
  @Override
  public Integer getId() {
    return actorId;
  }

  /**
   * Processes a received event: on {@link Topics#NEXT_VEHICLES_STATES} it advances the vehicle and
   * broadcasts its new state. Participates in: FR-003, FR-004, FR-005, FR-009.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    if (Topics.NEXT_VEHICLES_STATES.equals(event.getTopic())) {
      final int tick = (Integer) event.getParameters()[0];
      @SuppressWarnings("unchecked")
      final LightState[][][] lights = (LightState[][][]) event.getParameters()[1];
      advance(tick, lights);
    }
  }

  /**
   * Advances the vehicle by one simulated second of travel at its fixed speed. The vehicle moves
   * along its current segment and, when it reaches the traffic light at the entrance of the next
   * intersection, it may cross only if the light of its approach direction is green for the band it
   * uses (read from the provided light table); otherwise it stops at the light, not in the middle
   * of the crossing, and waits for green. Once it crosses it chooses its next direction (see {@link
   * #chooseNextDirection}). At the edge of the grid the vehicle must turn right or left (in a
   * corner there is only one possibility), otherwise it turns randomly with probability {@value
   * TrafficCoordinator#TURN_PROBABILITY}; a chosen turn that would leave the grid is not allowed
   * and falls back to continuing straight. The result is broadcast on {@link
   * Topics#VEHICLES_STATE}. Participates in: FR-003, FR-004, FR-005, FR-009.
   *
   * @param tick the current simulated tick
   * @param lights the light table of the previous tick, indexed {@code [row][col][0=ns,1=ew]}
   */
  private void advance(final int tick, final LightState[][][] lights) {
    final List<int[]> entered = new ArrayList<>();
    final double before = distanceInSegment;
    distanceInSegment += speed;
    final double after = distanceInSegment;
    if (before <= TrafficCoordinator.LIGHT_POSITION && after >= TrafficCoordinator.LIGHT_POSITION) {
      if (!isGreen(destinationRow(), destinationCol(), direction, lights)) {
        distanceInSegment = TrafficCoordinator.LIGHT_POSITION;
        broadcast(tick, entered);
        return;
      }
    }
    while (distanceInSegment >= TrafficCoordinator.SEGMENT_LENGTH) {
      final Direction nextDirection = chooseNextDirection();
      final int destRow = destinationRow();
      final int destCol = destinationCol();
      final double overrun = distanceInSegment - TrafficCoordinator.SEGMENT_LENGTH;
      row = destRow;
      col = destCol;
      direction = nextDirection;
      distanceInSegment = overrun;
      entered.add(new int[] {destRow, destCol});
    }
    broadcast(tick, entered);
  }

  /**
   * Broadcasts this vehicle's current state on {@link Topics#VEHICLES_STATE}. Participates in:
   * FR-003, FR-004, FR-005, FR-009.
   *
   * @param tick the current simulated tick
   * @param entered the cells entered during this tick
   */
  private void broadcast(final int tick, final List<int[]> entered) {
    lastState = new VehicleState(tick, vehicleId, row, col, direction, distanceInSegment, entered);
    getEngine().signal(EventImpl.createEvent(Topics.VEHICLES_STATE, this, lastState));
  }

  /**
   * Returns the most recent state this vehicle broadcast, or {@code null} if it has not broadcast
   * one yet. The state is stored before it is broadcast, so once the vehicles {@link
   * fr.jpnco.simula.actors.Barrier} has fired for a tick every vehicle's stored state is current
   * for that tick and the coordinator can pull it to assemble the snapshot. Participates in:
   * FR-003, FR-004, FR-005, FR-009.
   *
   * @return the last broadcast state, may be {@code null}
   */
  VehicleState getLastState() {
    return lastState;
  }

  /**
   * Returns whether the light band used by the given approach direction is green at the given cell
   * according to the light table. Participates in: FR-003, FR-004, FR-005, FR-009.
   *
   * @param row the row of the intersection
   * @param col the column of the intersection
   * @param d the approach direction
   * @param lights the light table
   * @return {@code true} if the band of that direction is green
   */
  private static boolean isGreen(
      final int row, final int col, final Direction d, final LightState[][][] lights) {
    return lights[row][col][d.isVertical() ? 0 : 1] == LightState.GREEN;
  }

  /**
   * Chooses the direction the vehicle takes when arriving at an intersection. A vehicle can never
   * reverse: it may only continue straight, turn right or turn left.
   *
   * <p>The vehicle first checks whether it must turn: if continuing straight past the intersection
   * it is approaching would leave the grid (the vehicle is on an edge), it is forced to turn right
   * or left, and in a corner only one of the two sides keeps it on the grid, so there is a single
   * possibility. Otherwise, with probability {@value TrafficCoordinator#TURN_PROBABILITY} a random
   * direction is chosen among straight, right and left; if that direction would leave the grid it
   * is discarded and the vehicle continues straight. Participates in: FR-003, FR-004, FR-005,
   * FR-009.
   *
   * @return the direction to take next
   */
  private Direction chooseNextDirection() {
    final int destRow = destinationRow();
    final int destCol = destinationCol();
    final boolean canContinue =
        isOnGrid(destRow + direction.rowDelta(), destCol + direction.colDelta());
    if (!canContinue) {
      return forcedTurn(destRow, destCol, direction);
    }
    final Direction[] choices = {direction, direction.turnRight(), direction.turnLeft()};
    if (random.nextDouble() < TrafficCoordinator.TURN_PROBABILITY) {
      final Direction candidate = choices[random.nextInt(choices.length)];
      if (isOnGrid(destRow + candidate.rowDelta(), destCol + candidate.colDelta())) {
        return candidate;
      }
    }
    return direction;
  }

  /**
   * Returns the forced turning direction for a vehicle arriving at the given destination
   * intersection that cannot continue straight past it. It tries the right turn first, then the
   * left turn; at a corner only one of the two keeps the vehicle on the grid, so that single
   * possibility is returned. Participates in: FR-003, FR-004, FR-005, FR-009.
   *
   * @param destRow the row of the destination intersection
   * @param destCol the column of the destination intersection
   * @param direction the current direction
   * @return the turn that keeps the vehicle on the grid
   */
  private Direction forcedTurn(final int destRow, final int destCol, final Direction direction) {
    final Direction right = direction.turnRight();
    if (isOnGrid(destRow + right.rowDelta(), destCol + right.colDelta())) {
      return right;
    }
    return direction.turnLeft();
  }

  /**
   * Returns whether the given cell lies within the grid bounds. Participates in: FR-003, FR-004,
   * FR-005, FR-009.
   *
   * @param row the row to check
   * @param col the column to check
   * @return {@code true} if the cell is inside the grid
   */
  private static boolean isOnGrid(final int row, final int col) {
    return row >= 0
        && row < TrafficCoordinator.INTERSECTIONS
        && col >= 0
        && col < TrafficCoordinator.INTERSECTIONS;
  }

  /**
   * Returns the row of the intersection the vehicle is approaching (the destination reached by
   * continuing in its current direction). Participates in: FR-003, FR-004, FR-005, FR-009.
   *
   * @return the destination row
   */
  private int destinationRow() {
    return row + direction.rowDelta();
  }

  /**
   * Returns the column of the intersection the vehicle is approaching (the destination reached by
   * continuing in its current direction). Participates in: FR-003, FR-004, FR-005, FR-009.
   *
   * @return the destination column
   */
  private int destinationCol() {
    return col + direction.colDelta();
  }

  /**
   * Returns the fixed speed of the vehicle in metres per second. Participates in: FR-003, FR-004,
   * FR-005, FR-009.
   *
   * @return the speed
   */
  double getSpeed() {
    return speed;
  }

  /**
   * Returns the current row of the vehicle. Participates in: FR-003, FR-004, FR-005, FR-009.
   *
   * @return the row
   */
  int getRow() {
    return row;
  }

  /**
   * Returns the current column of the vehicle. Participates in: FR-003, FR-004, FR-005, FR-009.
   *
   * @return the column
   */
  int getCol() {
    return col;
  }

  /**
   * Returns the current direction of the vehicle. Participates in: FR-003, FR-004, FR-005, FR-009.
   *
   * @return the direction
   */
  Direction getDirection() {
    return direction;
  }

  /**
   * Returns the distance already travelled into the current segment, in metres. Participates in:
   * FR-003, FR-004, FR-005, FR-009.
   *
   * @return the distance in the segment
   */
  double getDistanceInSegment() {
    return distanceInSegment;
  }
}
