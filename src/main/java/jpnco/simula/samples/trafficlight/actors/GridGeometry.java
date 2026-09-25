package jpnco.simula.samples.trafficlight.actors;

/**
 * Computes the pixel layout metrics of the traffic-light grid for the {@link TrafficLightGui}
 * panel. The geometry is extracted from the Swing painting code so it is independently testable.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-007, SC-004.
 */
public final class GridGeometry {

  /** The empty margin, in pixels, left around the grid on every side. */
  private static final int MARGIN = 10;

  /** The physical width of a road lane, in metres. */
  private static final double LANE_WIDTH_METRES = 10.0;

  /** The physical width of a vehicle, in metres. */
  private static final double VEHICLE_WIDTH_METRES = 2.0;

  /** The diameter of a traffic light circle, in pixels. */
  private static final int LIGHT_DIAMETER_PX = 4;

  /** The minimum lane thickness in pixels. */
  private static final int MIN_LANE_PX = 4;

  /** The minimum vehicle diameter in pixels. */
  private static final int MIN_VEHICLE_DIAMETER_PX = 2;

  /** The size of one grid cell in pixels. */
  private final int cell;

  /** The pixel scale factor (pixels per metre). */
  private final double pixelsPerMetre;

  /** The thickness of a road lane in pixels. */
  private final int lane;

  /** The total extent (grid plus one lane) in pixels. */
  private final int extent;

  /** The horizontal offset of the grid origin in pixels. */
  private final int offsetX;

  /** The vertical offset of the grid origin in pixels. */
  private final int offsetY;

  /**
   * Computes the layout metrics for a panel of the given size. Participates in: FR-007, SC-004.
   *
   * @param width the panel width in pixels
   * @param height the panel height in pixels
   */
  private GridGeometry(final int width, final int height) {
    final int available = Math.min(width, height) - 2 * MARGIN;
    final double cellDouble =
        available
            / (TrafficCoordinator.GRID_SIZE
                + LANE_WIDTH_METRES / TrafficCoordinator.SEGMENT_LENGTH);
    cell = (int) cellDouble;
    pixelsPerMetre = (double) cell / TrafficCoordinator.SEGMENT_LENGTH;
    lane = Math.max(MIN_LANE_PX, (int) Math.round(LANE_WIDTH_METRES * pixelsPerMetre));
    extent = cell * TrafficCoordinator.GRID_SIZE + lane;
    offsetX = (width - extent) / 2 + lane / 2;
    offsetY = (height - extent) / 2 + lane / 2;
  }

  /**
   * Computes the layout metrics for a panel of the given size. Participates in: FR-007, SC-004.
   *
   * @param width the panel width in pixels
   * @param height the panel height in pixels
   * @return the computed geometry
   */
  public static GridGeometry of(final int width, final int height) {
    return new GridGeometry(width, height);
  }

  /** Returns the size of one grid cell in pixels. Participates in: FR-007, SC-004. */
  public int cell() {
    return cell;
  }

  /** Returns the pixel scale factor (pixels per metre). Participates in: FR-007, SC-004. */
  public double pixelsPerMetre() {
    return pixelsPerMetre;
  }

  /** Returns the thickness of a road lane in pixels. Participates in: FR-007, SC-004. */
  public int lane() {
    return lane;
  }

  /** Returns the total extent (grid plus one lane) in pixels. Participates in: FR-007, SC-004. */
  public int extent() {
    return extent;
  }

  /**
   * Returns the horizontal offset of the grid origin in pixels. Participates in: FR-007, SC-004.
   */
  public int offsetX() {
    return offsetX;
  }

  /** Returns the vertical offset of the grid origin in pixels. Participates in: FR-007, SC-004. */
  public int offsetY() {
    return offsetY;
  }

  /** Returns the thickness of a lane marking in pixels. Participates in: FR-007, SC-004. */
  public int marking() {
    return Math.max(1, (int) Math.round(0.5 * pixelsPerMetre));
  }

  /** Returns the diameter of a vehicle circle in pixels. Participates in: FR-007, SC-004. */
  public int vehicleDiameter() {
    return Math.max(
        MIN_VEHICLE_DIAMETER_PX, (int) Math.round(VEHICLE_WIDTH_METRES * pixelsPerMetre));
  }

  /** Returns the radius of a traffic light circle in pixels. Participates in: FR-007, SC-004. */
  public int lightRadius() {
    return LIGHT_DIAMETER_PX / 2;
  }

  /**
   * Returns the lateral offset of a vehicle within its lane, in pixels. Participates in: FR-007,
   * SC-004.
   */
  public double lateral() {
    return lane / 4.0;
  }
}
