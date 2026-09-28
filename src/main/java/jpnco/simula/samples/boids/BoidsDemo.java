package jpnco.simula.samples.boids;

import java.util.concurrent.TimeUnit;
import jpnco.simula.actors.Logger;
import jpnco.simula.actors.Logger.Level;
import jpnco.simula.engine.EngineImpl;
import jpnco.simula.engine.ExecutionMode;
import jpnco.simula.samples.boids.actors.BoidsCoordinator;
import jpnco.simula.samples.boids.actors.BoidsGui;
import jpnco.simula.samples.boids.actors.BoidsMonitor;
import jpnco.simula.samples.boids.states.FlockState;

/**
 * Runnable illustration of the simula framework: a bounded 2D world of autonomous boid agents that
 * move according to the classic Reynolds flocking rules (separation, alignment, cohesion).
 *
 * <p>A {@link BoidsCoordinator} actor pilots the ticks and owns the flock; each {@code Boid} is an
 * autonomous actor; a {@link jpnco.simula.actors.Barrier} synchronizes the per-tick reports. The
 * world is toroidal (a boid reaching an edge wraps around to the opposite edge), the same scenario
 * can be run under {@link ExecutionMode#VIRTUAL} (default) or {@link ExecutionMode#PLATFORM}, and
 * all randomness is seeded so the same run produces the same outcome in both modes.
 *
 * <p>Usage: {@code BoidsDemo [mode] [display] [parameters...]} where {@code mode} is {@code
 * virtual} (default) or {@code classic}, {@code display} is {@code console} (default) or {@code
 * gui}, and the optional parameters are {@code --boids=<n>}, {@code --perception-radius=<n>} and
 * {@code --max-speed=<n>}. The console display runs for {@value #SIMULATED_SECONDS} simulated
 * seconds then stops and prints the final outcome; the GUI display opens a Swing window that runs
 * until it is closed.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-001, FR-005, FR-007, FR-008, FR-009, SC-001, SC-002, SC-003.
 */
public final class BoidsDemo {

  /** The time factor (simulated seconds per real second) of the root engine. */
  private static final int TIME_FACTOR = 1;

  /** The name of the root engine. */
  private static final String ROOT_NAME = "root";

  /** The number of simulated seconds the console demo runs before stopping. */
  private static final int SIMULATED_SECONDS = 120;

  /** The milliseconds to let the last events drain after the root engine stops. */
  private static final long DRAIN_MILLIS = 1500;

  /** The seconds to await the completion latch before giving up. */
  private static final long AWAIT_TIMEOUT_SECONDS = 120;

  /** A duration that never ends, used when the GUI keeps the simulation running until it closes. */
  private static final int UNBOUNDED_DURATION = Integer.MAX_VALUE;

  /** Private constructor to prevent instantiation of this entry point. */
  private BoidsDemo() {}

  /**
   * Entry point of the flocking demo. Participates in: FR-001, FR-005, FR-007, FR-008, FR-009,
   * SC-001, SC-002, SC-003.
   *
   * @param args optional tokens: {@code classic} selects {@link ExecutionMode#PLATFORM} (default
   *     {@link ExecutionMode#VIRTUAL}); {@code gui} selects the Swing display (default console);
   *     and {@code --boids=}, {@code --perception-radius=}, {@code --max-speed=} override the
   *     parameters
   */
  public static void main(final String[] args) {
    final ExecutionMode mode = BoidsCli.resolveMode(args);
    final boolean gui = BoidsCli.resolveDisplay(args) == BoidsCli.DisplayChoice.GUI;
    final FlockParameters params = BoidsCli.resolveParameters(args);
    Logger.forceLevel(Level.ERROR);

    if (gui) {
      runGui(mode, params);
    } else {
      runConsole(SIMULATED_SECONDS, params, mode);
      System.exit(0);
    }
  }

  /**
   * Runs the console display of the demo: builds the root engine, seeds and starts the coordinator
   * and the monitor, awaits completion and prints the outcome. Participates in: FR-001, FR-005,
   * FR-007, FR-008, FR-009, SC-001, SC-002, SC-003.
   *
   * @param durationSeconds the number of simulated seconds to run before stopping
   * @param params the flocking parameters
   * @param mode the execution mode
   */
  static void runConsole(
      final int durationSeconds, final FlockParameters params, final ExecutionMode mode) {
    final EngineImpl root = new EngineImpl(ROOT_NAME, TIME_FACTOR, mode);
    final BoidsCoordinator coordinator = new BoidsCoordinator(root, params, durationSeconds);
    coordinator.seed();

    final BoidsMonitor display = new BoidsMonitor(root);

    root.registerAndStart(coordinator);
    root.registerAndStart(display);
    root.start();

    awaitCompletion(coordinator);
    System.out.print(formatOutcome(mode, coordinator));
  }

  /**
   * Runs the GUI display of the demo: builds the root engine, seeds and starts the coordinator and
   * the GUI display, and shows the window. Participates in: FR-001, FR-006, SC-004.
   *
   * @param mode the execution mode
   * @param params the flocking parameters
   */
  private static void runGui(final ExecutionMode mode, final FlockParameters params) {
    final EngineImpl root = new EngineImpl(ROOT_NAME, TIME_FACTOR, mode);
    final BoidsCoordinator coordinator = new BoidsCoordinator(root, params, UNBOUNDED_DURATION);
    coordinator.seed();

    final BoidsGui display = new BoidsGui(root);

    root.registerAndStart(coordinator);
    root.registerAndStart(display);
    root.start();

    display.showWindow();
  }

  /**
   * Awaits the completion of the simulation. Participates in: FR-001, FR-005, SC-001, SC-002.
   *
   * @param coordinator the coordinator whose completion latch is awaited
   */
  private static void awaitCompletion(final BoidsCoordinator coordinator) {
    try {
      if (!coordinator.getDoneLatch().await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        System.err.println("Simulation did not complete within the timeout.");
      }
      Thread.sleep(DRAIN_MILLIS);
    } catch (final InterruptedException exc) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Formats the final outcome summary of the simulation. Participates in: FR-001, FR-005, FR-007,
   * FR-008, SC-002, SC-003.
   *
   * @param mode the execution mode that was used
   * @param coordinator the coordinator holding the final totals and flock state
   * @return the formatted outcome text
   */
  static String formatOutcome(final ExecutionMode mode, final BoidsCoordinator coordinator) {
    final StringBuilder out = new StringBuilder("\n=== OUTCOME (").append(mode).append(") ===\n");
    final FlockState state = coordinator.snapshot();
    out.append("boids=").append(state.getBoidCount());
    out.append(", distance=").append(coordinator.getTotalDistance());
    out.append('\n');
    return out.toString();
  }
}
