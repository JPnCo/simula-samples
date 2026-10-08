package fr.jpnco.simula.samples.trafficlight.states;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An immutable report of one vehicle's position and movement at one simulated tick, broadcast by a
 * {@link fr.jpnco.simula.samples.trafficlight.actors.Vehicle} on the {@link
 * fr.jpnco.simula.samples.trafficlight.actors.Topics#VEHICLES_STATE} topic. It also carries the
 * cells the vehicle entered during the tick so the coordinator can update its crossing counters.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-003, FR-006.
 */
public final class VehicleState {

  /** The simulated tick this state belongs to (used to detect out-of-order events). */
  private final int tick;

  /** The vehicle identity. */
  private final int id;

  /** The vehicle row. */
  private final int row;

  /** The vehicle column. */
  private final int col;

  /** The vehicle direction. */
  private final Direction direction;

  /** The distance travelled into the current segment, in metres. */
  private final double distanceInSegment;

  /** The cells (row, col pairs) the vehicle entered during this tick. */
  private final List<int[]> enteredCells;

  /**
   * Creates an immutable vehicle report.
   *
   * @param tick the simulated tick
   * @param id the vehicle identity
   * @param row the vehicle row
   * @param col the vehicle column
   * @param direction the vehicle direction
   * @param distanceInSegment the distance into the current segment
   * @param enteredCells the cells entered during the tick
   */
  public VehicleState(
      final int tick,
      final int id,
      final int row,
      final int col,
      final Direction direction,
      final double distanceInSegment,
      final List<int[]> enteredCells) {
    this.tick = tick;
    this.id = id;
    this.row = row;
    this.col = col;
    this.direction = direction;
    this.distanceInSegment = distanceInSegment;
    this.enteredCells = Collections.unmodifiableList(new ArrayList<>(enteredCells));
  }

  /**
   * Returns the simulated tick this state belongs to. Participates in: FR-003, FR-006.
   *
   * @return the tick
   */
  public int getTick() {
    return tick;
  }

  /**
   * Returns the vehicle identity. Participates in: FR-003, FR-006.
   *
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the vehicle row. Participates in: FR-003, FR-006.
   *
   * @return the row
   */
  public int getRow() {
    return row;
  }

  /**
   * Returns the vehicle column. Participates in: FR-003, FR-006.
   *
   * @return the column
   */
  public int getCol() {
    return col;
  }

  /**
   * Returns the vehicle direction. Participates in: FR-003, FR-006.
   *
   * @return the direction
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Returns the distance travelled into the current segment. Participates in: FR-003, FR-006.
   *
   * @return the distance in the segment
   */
  public double getDistanceInSegment() {
    return distanceInSegment;
  }

  /**
   * Returns the cells (row, col pairs) the vehicle entered during this tick. Participates in:
   * FR-003, FR-006.
   *
   * @return the entered cells
   */
  public List<int[]> getEnteredCells() {
    return enteredCells;
  }
}
