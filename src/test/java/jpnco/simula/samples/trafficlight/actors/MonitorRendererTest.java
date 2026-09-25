package jpnco.simula.samples.trafficlight.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import jpnco.simula.samples.trafficlight.states.Direction;
import jpnco.simula.samples.trafficlight.states.GridState;
import jpnco.simula.samples.trafficlight.states.LightState;
import jpnco.simula.samples.trafficlight.states.VehicleView;
import org.junit.jupiter.api.Test;

class MonitorRendererTest {

  private static GridState allGreenState(final int simTime) {
    final int n = TrafficCoordinator.INTERSECTIONS;
    final LightState[][] ns = new LightState[n][n];
    final LightState[][] ew = new LightState[n][n];
    final int[][] crossings = new int[n][n];
    for (int r = 0; r < n; r++) {
      for (int c = 0; c < n; c++) {
        ns[r][c] = LightState.GREEN;
        ew[r][c] = LightState.RED;
      }
    }
    return new GridState(
        simTime, List.of(new VehicleView(0, 0, 1, Direction.EAST, 10.0)), ns, ew, crossings);
  }

  @Test
  void lightMarker_prefers_north_south_then_east_west() {
    final GridState state = allGreenState(1);
    assertEquals('|', MonitorRenderer.lightMarker(state, 0, 0));
  }

  @Test
  void lightMarker_reports_east_west_when_north_south_not_active() {
    final int n = TrafficCoordinator.INTERSECTIONS;
    final LightState[][] ns = new LightState[n][n];
    final LightState[][] ew = new LightState[n][n];
    final int[][] crossings = new int[n][n];
    for (int r = 0; r < n; r++) {
      for (int c = 0; c < n; c++) {
        ns[r][c] = LightState.RED;
        ew[r][c] = LightState.GREEN;
      }
    }
    final GridState state = new GridState(1, List.of(), ns, ew, crossings);
    assertEquals('-', MonitorRenderer.lightMarker(state, 0, 0));
  }

  @Test
  void render_produces_time_line_and_occupancy() {
    final String out = MonitorRenderer.render(allGreenState(3));
    final String[] lines = out.split("\n", -1);
    assertEquals("t=3", lines[0]);
    // Row 0 contains the vehicle at column 1.
    assertEquals("|. |1 |. |. ", lines[1]);
  }

  private static GridState grid(final LightState nsLight, final LightState ewLight) {
    final int n = TrafficCoordinator.INTERSECTIONS;
    final LightState[][] ns = new LightState[n][n];
    final LightState[][] ew = new LightState[n][n];
    final int[][] crossings = new int[n][n];
    for (int r = 0; r < n; r++) {
      for (int c = 0; c < n; c++) {
        ns[r][c] = nsLight;
        ew[r][c] = ewLight;
      }
    }
    return new GridState(1, List.of(), ns, ew, crossings);
  }

  @Test
  void lightMarker_reports_north_south_orange() {
    assertEquals(':', MonitorRenderer.lightMarker(grid(LightState.ORANGE, LightState.RED), 0, 0));
  }

  @Test
  void lightMarker_reports_east_west_orange() {
    assertEquals('~', MonitorRenderer.lightMarker(grid(LightState.RED, LightState.ORANGE), 0, 0));
  }

  @Test
  void lightMarker_falls_back_to_north_south_when_all_red() {
    assertEquals('|', MonitorRenderer.lightMarker(grid(LightState.RED, LightState.RED), 0, 0));
  }
}
