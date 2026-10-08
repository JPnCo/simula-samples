package fr.jpnco.simula.samples.boids;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.jpnco.simula.engine.ExecutionMode;
import fr.jpnco.simula.samples.boids.actors.BoidsCoordinator;
import fr.jpnco.simula.samples.boids.states.BoidView;
import fr.jpnco.simula.samples.boids.states.FlockState;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.Test;

class BoidsDemoTest {

  private static FlockState state() {
    return new FlockState(2, List.of(new BoidView(0, 10.0, 20.0, 1.0, 1.0)), 800.0, 600.0);
  }

  @Test
  void formatOutcome_contains_mode_boids_and_distance() {
    final BoidsCoordinator coordinator = mock(BoidsCoordinator.class);
    when(coordinator.snapshot()).thenReturn(state());
    when(coordinator.getTotalDistance()).thenReturn(12.5);

    final String out = BoidsDemo.formatOutcome(ExecutionMode.VIRTUAL, coordinator);
    assertTrue(out.contains("OUTCOME (VIRTUAL)"));
    assertTrue(out.contains("boids=1"));
    assertTrue(out.contains("distance=12.5"));
  }

  @Test
  void runConsole_prints_outcome_for_short_simulation() {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      BoidsDemo.runConsole(
          2, new FlockParameters(8, 1.0, 1.0, 1.0, 40.0, 4.0, 800.0, 600.0), ExecutionMode.VIRTUAL);
    } finally {
      System.setOut(original);
    }
    assertTrue(buffer.toString().contains("=== OUTCOME (VIRTUAL) ==="));
    assertTrue(buffer.toString().contains("boids=8"));
  }
}
