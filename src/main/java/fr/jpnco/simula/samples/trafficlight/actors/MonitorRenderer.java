package fr.jpnco.simula.samples.trafficlight.actors;

import fr.jpnco.simula.samples.trafficlight.states.GridState;
import fr.jpnco.simula.samples.trafficlight.states.LightState;
import fr.jpnco.simula.samples.trafficlight.states.VehicleView;

/**
 * Renders a {@link GridState} as a console text grid for the {@link TrafficMonitor} display actor.
 * The rendering logic is extracted here so it is independently testable.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-006.
 */
public final class MonitorRenderer {

  /** The marker used to render a north-south green light. */
  private static final char NS_GREEN = '|';

  /** The marker used to render an east-west green light. */
  private static final char EW_GREEN = '-';

  /** The marker used to render a north-south orange light. */
  private static final char NS_ORANGE = ':';

  /** The marker used to render an east-west orange light. */
  private static final char EW_ORANGE = '~';

  /** The marker used to render an intersection with no vehicle present. */
  private static final char EMPTY = '.';

  /** Private constructor to prevent instantiation of this utility class. */
  private MonitorRenderer() {}

  /**
   * Renders the given grid state as a text grid prefixed with the simulated time. Participates in:
   * FR-006.
   *
   * @param state the state to render
   * @return the rendered grid text
   */
  public static String render(final GridState state) {
    final int[][] occupancy =
        new int[TrafficCoordinator.INTERSECTIONS][TrafficCoordinator.INTERSECTIONS];
    for (final VehicleView vehicle : state.getVehicles()) {
      occupancy[vehicle.getRow()][vehicle.getCol()]++;
    }
    final StringBuilder grid = new StringBuilder("t=").append(state.getSimTime()).append('\n');
    for (int row = 0; row < TrafficCoordinator.INTERSECTIONS; row++) {
      for (int col = 0; col < TrafficCoordinator.INTERSECTIONS; col++) {
        final char light = lightMarker(state, row, col);
        final int vehiclesHere = occupancy[row][col];
        grid.append(light);
        if (vehiclesHere > 0) {
          grid.append(vehiclesHere);
        } else {
          grid.append(EMPTY);
        }
        grid.append(' ');
      }
      grid.append('\n');
    }
    return grid.toString();
  }

  /**
   * Returns the marker character representing the dominant light at the given intersection.
   * Participates in: FR-006.
   *
   * @param state the state
   * @param row the row
   * @param col the column
   * @return the marker character
   */
  static char lightMarker(final GridState state, final int row, final int col) {
    final LightState ns = state.lightState(row, col, true);
    if (ns == LightState.GREEN) {
      return NS_GREEN;
    }
    if (ns == LightState.ORANGE) {
      return NS_ORANGE;
    }
    final LightState ew = state.lightState(row, col, false);
    if (ew == LightState.GREEN) {
      return EW_GREEN;
    }
    if (ew == LightState.ORANGE) {
      return EW_ORANGE;
    }
    return NS_GREEN;
  }
}
