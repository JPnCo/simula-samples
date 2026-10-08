package fr.jpnco.simula.samples.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.jpnco.simula.engine.ExecutionMode;
import fr.jpnco.simula.samples.dashboard.DashboardParameters.Display;
import fr.jpnco.simula.samples.dashboard.DashboardParameters.Target;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class DashboardDemoTest {

  @Test
  void subscribeTopic_returns_the_target_new_state_topic() {
    assertEquals(
        fr.jpnco.simula.samples.boids.actors.Topics.NEW_STATE,
        DashboardDemo.subscribeTopic(Target.BOIDS));
    assertEquals(
        fr.jpnco.simula.samples.trafficlight.actors.Topics.NEW_STATE,
        DashboardDemo.subscribeTopic(Target.TRAFFICLIGHT));
  }

  @Test
  void runConsole_records_and_prints_a_summary_for_a_short_boids_run() {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      DashboardDemo.runConsole(
          2, new DashboardParameters(Target.BOIDS, ExecutionMode.VIRTUAL, Display.CONSOLE, null));
    } finally {
      System.setOut(original);
    }
    assertTrue(buffer.toString().contains("samples="));
  }

  @Test
  void runConsole_works_against_the_trafficlight_target() {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      DashboardDemo.runConsole(
          2,
          new DashboardParameters(
              Target.TRAFFICLIGHT, ExecutionMode.VIRTUAL, Display.CONSOLE, null));
    } finally {
      System.setOut(original);
    }
    assertTrue(buffer.toString().contains("samples="));
  }
}
