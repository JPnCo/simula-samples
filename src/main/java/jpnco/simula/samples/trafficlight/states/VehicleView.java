package jpnco.simula.samples.trafficlight.states;

/**
 * An immutable view of a vehicle's position and direction at one instant, carried inside a {@link
 * GridState} so displays can read it safely from another thread.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-006, FR-007, SC-004.
 */
public final class VehicleView {

  /** The vehicle identity. */
  private final int id;

  /** The vehicle row. */
  private final int row;

  /** The vehicle column. */
  private final int col;

  /** The vehicle direction. */
  private final Direction direction;

  /** The distance already travelled into the current segment, in metres. */
  private final double distanceInSegment;

  /**
   * Creates an immutable vehicle view.
   *
   * @param id the vehicle identity
   * @param row the vehicle row
   * @param col the vehicle column
   * @param direction the vehicle direction
   * @param distanceInSegment the distance into the current segment
   */
  public VehicleView(
      final int id,
      final int row,
      final int col,
      final Direction direction,
      final double distanceInSegment) {
    this.id = id;
    this.row = row;
    this.col = col;
    this.direction = direction;
    this.distanceInSegment = distanceInSegment;
  }

  /**
   * Returns the vehicle identity. Participates in: FR-006, FR-007, SC-004.
   *
   * @return the vehicle id
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the vehicle row. Participates in: FR-006, FR-007, SC-004.
   *
   * @return the row
   */
  public int getRow() {
    return row;
  }

  /**
   * Returns the vehicle column. Participates in: FR-006, FR-007, SC-004.
   *
   * @return the column
   */
  public int getCol() {
    return col;
  }

  /**
   * Returns the vehicle direction. Participates in: FR-006, FR-007, SC-004.
   *
   * @return the direction
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Returns the distance already travelled into the current segment, in metres. Participates in:
   * FR-006, FR-007, SC-004.
   *
   * @return the distance in the segment
   */
  public double getDistanceInSegment() {
    return distanceInSegment;
  }
}
