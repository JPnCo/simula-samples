package jpnco.simula.samples.trafficlight.states;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An immutable snapshot of the grid at one simulated instant, broadcast by the {@link
 * jpnco.simula.samples.trafficlight.actors.TrafficCoordinator} after every completed tick. It is
 * read by the console and GUI displays, which may run on a different thread than the coordinator.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-006, FR-007, SC-004.
 */
public final class GridState {

  /** The simulated time at which this snapshot was taken. */
  private final int simTime;

  /** The number of vehicles in the fleet. */
  private final int vehicleCount;

  /** The positions and directions of the vehicles at this instant. */
  private final List<VehicleView> vehicles;

  /** The state of the north-south light band of each intersection at this instant. */
  private final LightState[][] northSouth;

  /** The state of the east-west light band of each intersection at this instant. */
  private final LightState[][] eastWest;

  /** The per-cell number of vehicle arrivals so far. */
  private final int[][] crossings;

  /**
   * Creates an immutable snapshot, defensively copying the vehicles and the light and crossing
   * tables. Participates in: FR-006, FR-007, SC-004.
   *
   * @param simTime the simulated time
   * @param vehicles the vehicle views
   * @param northSouth the north-south light bands
   * @param eastWest the east-west light bands
   * @param crossings the per-cell crossing counts
   */
  public GridState(
      final int simTime,
      final List<VehicleView> vehicles,
      final LightState[][] northSouth,
      final LightState[][] eastWest,
      final int[][] crossings) {
    this.simTime = simTime;
    this.vehicleCount = vehicles.size();
    this.vehicles = Collections.unmodifiableList(new ArrayList<>(vehicles));
    this.northSouth = copy(northSouth);
    this.eastWest = copy(eastWest);
    this.crossings = copy(crossings);
  }

  /**
   * Returns the simulated time of this snapshot. Participates in: FR-006, FR-007, SC-004.
   *
   * @return the simulated time
   */
  public int getSimTime() {
    return simTime;
  }

  /**
   * Returns the number of vehicles in the fleet. Participates in: FR-006, FR-007, SC-004.
   *
   * @return the vehicle count
   */
  public int getVehicleCount() {
    return vehicleCount;
  }

  /**
   * Returns an unmodifiable view of the vehicles. Participates in: FR-006, FR-007, SC-004.
   *
   * @return the vehicles
   */
  public List<VehicleView> getVehicles() {
    return vehicles;
  }

  /**
   * Returns whether the intersection at the given cell lets north-south traffic flow. Participates
   * in: FR-006, FR-007, SC-004.
   *
   * @param row the row
   * @param col the column
   * @return {@code true} if north-south traffic may flow at this cell
   */
  public boolean isNorthSouthGreen(final int row, final int col) {
    return northSouth[row][col] == LightState.GREEN;
  }

  /**
   * Returns the state of the light band used by a vehicle travelling in the given direction at the
   * given cell: the north-south band for a vertical direction, the east-west band otherwise.
   * Participates in: FR-006, FR-007, SC-004.
   *
   * @param row the row
   * @param col the column
   * @param vertical whether the direction uses the north-south band
   * @return the light state for that band
   */
  public LightState lightState(final int row, final int col, final boolean vertical) {
    return vertical ? northSouth[row][col] : eastWest[row][col];
  }

  /**
   * Returns the number of vehicle arrivals at the given cell so far. Participates in: FR-006,
   * FR-007, SC-004.
   *
   * @param row the row
   * @param col the column
   * @return the crossing count
   */
  public int crossingsAt(final int row, final int col) {
    return crossings[row][col];
  }

  /**
   * Returns a defensive copy of the given light band table. Participates in: FR-006, FR-007,
   * SC-004.
   *
   * @param source the table to copy
   * @return the copy
   */
  private static LightState[][] copy(final LightState[][] source) {
    final LightState[][] copy = new LightState[source.length][];
    for (int i = 0; i < source.length; i++) {
      copy[i] = source[i].clone();
    }
    return copy;
  }

  /**
   * Returns a defensive copy of the given crossing-count table. Participates in: FR-006, FR-007,
   * SC-004.
   *
   * @param source the table to copy
   * @return the copy
   */
  private static int[][] copy(final int[][] source) {
    final int[][] copy = new int[source.length][];
    for (int i = 0; i < source.length; i++) {
      copy[i] = source[i].clone();
    }
    return copy;
  }
}
