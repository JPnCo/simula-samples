package fr.jpnco.simula.samples.boids;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.jpnco.simula.engine.ExecutionMode;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Verifies the behavioral-equivalence success criterion (SC-003 / FR-007, FR-008): the same seeded
 * scenario run under {@link ExecutionMode#VIRTUAL} (default) and {@link ExecutionMode#PLATFORM}
 * must produce an identical final outcome, in particular the same total distance travelled.
 */
class DeterminismTest {

  private static final int SIMULATED_SECONDS = 10;
  private static final String DISTANCE_PREFIX = "distance=";
  private static final FlockParameters PARAMS = FlockParameters.DEFAULT;

  private static double distanceInMode(final ExecutionMode mode) {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      BoidsDemo.runConsole(SIMULATED_SECONDS, PARAMS, mode);
    } finally {
      System.setOut(original);
    }
    final String output = buffer.toString();
    final Matcher matcher = Pattern.compile(DISTANCE_PREFIX + "([\\d.]+)").matcher(output);
    assertTrue(matcher.find(), "expected " + DISTANCE_PREFIX + "<n> in output");
    return Double.parseDouble(matcher.group(1));
  }

  @Test
  void virtual_and_platform_produce_identical_distance() {
    final double virtual = distanceInMode(ExecutionMode.VIRTUAL);
    final double platform = distanceInMode(ExecutionMode.PLATFORM);
    assertEquals(
        virtual,
        platform,
        1e-6,
        "the same seeded scenario must yield the same outcome in both execution modes");
  }

  @Test
  void virtual_mode_reports_the_expected_boid_count() {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      BoidsDemo.runConsole(SIMULATED_SECONDS, PARAMS, ExecutionMode.VIRTUAL);
    } finally {
      System.setOut(original);
    }
    assertTrue(buffer.toString().contains("boids=" + PARAMS.getBoidCount()));
  }
}
