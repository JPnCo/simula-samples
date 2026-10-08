package fr.jpnco.simula.samples.trafficlight;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.jpnco.simula.engine.ExecutionMode;
import fr.jpnco.simula.samples.trafficlight.actors.TrafficCoordinator;
import fr.jpnco.simula.samples.trafficlight.states.Direction;
import fr.jpnco.simula.samples.trafficlight.states.GridState;
import fr.jpnco.simula.samples.trafficlight.states.LightState;
import fr.jpnco.simula.samples.trafficlight.states.VehicleView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrafficLightDemoTest {

  private static GridState state() {
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
    final List<VehicleView> views = List.of(new VehicleView(0, 0, 1, Direction.EAST, 10.0));
    return new GridState(2, views, ns, ew, crossings);
  }

  @Test
  void formatOutcome_contains_mode_vehicles_and_crossings() {
    final TrafficCoordinator coordinator = mock(TrafficCoordinator.class);
    when(coordinator.snapshot()).thenReturn(state());
    when(coordinator.getTotalCrossings()).thenReturn(5);

    final String out = TrafficLightDemo.formatOutcome(ExecutionMode.VIRTUAL, coordinator);
    assertTrue(out.contains("OUTCOME (VIRTUAL)"));
    assertTrue(out.contains("vehicles=1"));
    assertTrue(out.contains("crossings=5"));
  }

  @Test
  void runConsole_prints_outcome_for_short_simulation() {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      TrafficLightDemo.runConsole(2, ExecutionMode.VIRTUAL);
    } finally {
      System.setOut(original);
    }
    assertTrue(buffer.toString().contains("=== OUTCOME (VIRTUAL) ==="));
    assertTrue(buffer.toString().contains("vehicles=12"));
  }
}
