package fr.jpnco.simula.samples.trafficlight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.jpnco.simula.engine.ExecutionMode;
import fr.jpnco.simula.samples.trafficlight.actors.TrafficCoordinator;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Verifies the behavioral-equivalence success criterion (SC-003 / FR-008): the same seeded scenario
 * run under {@link ExecutionMode#VIRTUAL} (default) and {@link ExecutionMode#PLATFORM} (classic)
 * must produce an identical final outcome, in particular the same total number of crossings.
 */
class DeterminismTest {

  /** The number of simulated seconds used to compare the two execution modes. */
  private static final int SIMULATED_SECONDS = 10;

  /** The prefix of the crossings field in the printed outcome. */
  private static final String CROSSINGS_PREFIX = "crossings=";

  /**
   * Runs the console demo in the given mode, captures the printed outcome and returns the total
   * number of crossings.
   *
   * @param mode the execution mode to run
   * @return the printed total crossings
   */
  private static int crossingsInMode(final ExecutionMode mode) {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      TrafficLightDemo.runConsole(SIMULATED_SECONDS, mode);
    } finally {
      System.setOut(original);
    }
    final String output = buffer.toString();
    final Matcher matcher = Pattern.compile(CROSSINGS_PREFIX + "(\\d+)").matcher(output);
    assertTrue(matcher.find(), "expected " + CROSSINGS_PREFIX + "<n> in output");
    return Integer.parseInt(matcher.group(1));
  }

  @Test
  void virtual_and_platform_produce_identical_crossings() {
    final int virtualCrossings = crossingsInMode(ExecutionMode.VIRTUAL);
    final int platformCrossings = crossingsInMode(ExecutionMode.PLATFORM);
    assertEquals(
        virtualCrossings,
        platformCrossings,
        "the same seeded scenario must yield the same outcome in both execution modes");
  }

  @Test
  void virtual_mode_reports_the_expected_vehicle_count() {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      TrafficLightDemo.runConsole(SIMULATED_SECONDS, ExecutionMode.VIRTUAL);
    } finally {
      System.setOut(original);
    }
    assertTrue(buffer.toString().contains("vehicles=" + TrafficCoordinator.INITIAL_VEHICLES));
  }
}
