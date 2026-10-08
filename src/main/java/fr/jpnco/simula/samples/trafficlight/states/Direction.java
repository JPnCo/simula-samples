package fr.jpnco.simula.samples.trafficlight.states;

/**
 * A cardinal direction of travel on the grid: north, south, east or west, each with a row and
 * column delta. A vehicle travels segment by segment; at an edge it cannot leave the grid and must
 * turn right or left.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-003, FR-005, FR-009.
 */
public enum Direction {

  /** Moving one cell upward (decreasing row). */
  NORTH(-1, 0),

  /** Moving one cell downward (increasing row). */
  SOUTH(1, 0),

  /** Moving one cell right (increasing column). */
  EAST(0, 1),

  /** Moving one cell left (decreasing column). */
  WEST(0, -1);

  /** The row delta of this direction. */
  private final int rowDelta;

  /** The column delta of this direction. */
  private final int colDelta;

  /**
   * Creates a direction with the given deltas.
   *
   * @param rowDelta the row delta
   * @param colDelta the column delta
   */
  Direction(final int rowDelta, final int colDelta) {
    this.rowDelta = rowDelta;
    this.colDelta = colDelta;
  }

  /**
   * Returns the row delta of this direction. Participates in: FR-003, FR-005, FR-009.
   *
   * @return the row delta
   */
  public int rowDelta() {
    return rowDelta;
  }

  /**
   * Returns the column delta of this direction. Participates in: FR-003, FR-005, FR-009.
   *
   * @return the column delta
   */
  public int colDelta() {
    return colDelta;
  }

  /**
   * Returns whether this direction travels north or south, i.e. uses the north-south band of a
   * traffic light. Participates in: FR-003, FR-005, FR-009.
   *
   * @return {@code true} for a vertical (north/south) direction
   */
  public boolean isVertical() {
    return this == NORTH || this == SOUTH;
  }

  /**
   * Returns the direction obtained by turning right (clockwise) from this one. Participates in:
   * FR-003, FR-005, FR-009.
   *
   * @return the clockwise neighbour direction
   */
  public Direction turnRight() {
    return switch (this) {
      case NORTH -> EAST;
      case EAST -> SOUTH;
      case SOUTH -> WEST;
      case WEST -> NORTH;
    };
  }

  /**
   * Returns the direction obtained by turning left (counter-clockwise) from this one. Participates
   * in: FR-003, FR-005, FR-009.
   *
   * @return the counter-clockwise neighbour direction
   */
  public Direction turnLeft() {
    return switch (this) {
      case NORTH -> WEST;
      case WEST -> SOUTH;
      case SOUTH -> EAST;
      case EAST -> NORTH;
    };
  }
}
