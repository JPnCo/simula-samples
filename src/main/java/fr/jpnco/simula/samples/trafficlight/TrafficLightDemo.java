package fr.jpnco.simula.samples.trafficlight;

import fr.jpnco.simula.actors.Logger;
import fr.jpnco.simula.actors.Logger.Level;
import fr.jpnco.simula.engine.EngineImpl;
import fr.jpnco.simula.engine.ExecutionMode;
import fr.jpnco.simula.samples.trafficlight.actors.TrafficCoordinator;
import fr.jpnco.simula.samples.trafficlight.actors.TrafficLightGui;
import fr.jpnco.simula.samples.trafficlight.actors.TrafficMonitor;
import fr.jpnco.simula.samples.trafficlight.states.GridState;
import java.util.concurrent.TimeUnit;

/**
 * Runnable illustration of the simula framework: a closed-loop grid of intersections with a traffic
 * light at each one and vehicles that travel the grid and may turn randomly.
 *
 * <p>The grid is {@value TrafficCoordinator#GRID_SIZE} by {@value TrafficCoordinator#GRID_SIZE}
 * cells, bounded by {@value TrafficCoordinator#INTERSECTIONS} by {@value
 * TrafficCoordinator#INTERSECTIONS} roads with an intersection at every crossing. Each intersection
 * has a light that alternates between letting north-south and east-west traffic flow; a vehicle
 * only advances when its current intersection's light is green for its direction, and it may change
 * direction randomly at an intersection. At the edge of the grid a vehicle cannot leave it and must
 * turn right or left, so the fixed fleet of {@value TrafficCoordinator#INITIAL_VEHICLES} vehicles
 * keeps circulating until the demo stops.
 *
 * <p>The same scenario can be run under {@link ExecutionMode#VIRTUAL} (default) or {@link
 * ExecutionMode#PLATFORM}. All movement randomness uses a fixed seed, so the same run produces the
 * same outcome in both modes, demonstrating behavioral equivalence.
 *
 * <p>Usage: {@code TrafficLightDemo [mode] [display]} where {@code mode} is {@code virtual}
 * (default) or {@code classic}, and {@code display} is {@code console} (default) or {@code gui}.
 * The console display runs for {@value #SIMULATED_SECONDS} simulated seconds then stops and prints
 * the final outcome; the GUI display opens a Swing window that runs until it is closed.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-001, FR-006, FR-008, SC-001, SC-002, SC-003, SC-005.
 */
public final class TrafficLightDemo {

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
  private TrafficLightDemo() {}

  /**
   * Entry point of the grid traffic-light demo. Participates in: FR-001, FR-006, FR-008, SC-001,
   * SC-002, SC-003, SC-005.
   *
   * @param args optional tokens: {@code classic} selects {@link ExecutionMode#PLATFORM} (default
   *     {@link ExecutionMode#VIRTUAL}); {@code gui} selects the Swing display (default console)
   */
  public static void main(final String[] args) {
    final ExecutionMode mode = TrafficLightCli.resolveMode(args);
    final boolean gui = TrafficLightCli.resolveDisplay(args) == TrafficLightCli.DisplayChoice.GUI;
    Logger.forceLevel(Level.ERROR);

    if (gui) {
      runGui(mode);
    } else {
      runConsole(SIMULATED_SECONDS, mode);
      System.exit(0);
    }
  }

  /**
   * Runs the console display of the demo: builds the root engine, seeds and starts the coordinator
   * and the monitor, awaits completion and prints the outcome. Participates in: FR-001, FR-006,
   * FR-008, SC-001, SC-002, SC-003, SC-005.
   *
   * @param durationSeconds the number of simulated seconds to run before stopping
   * @param mode the execution mode
   */
  static void runConsole(final int durationSeconds, final ExecutionMode mode) {
    final EngineImpl root = new EngineImpl(ROOT_NAME, TIME_FACTOR, mode);
    final TrafficCoordinator coordinator = new TrafficCoordinator(root, durationSeconds);
    coordinator.seed();

    final TrafficMonitor display = new TrafficMonitor(root);

    root.registerAndStart(coordinator);
    root.registerAndStart(display);
    root.start();

    awaitCompletion(coordinator);
    printOutcome(mode, coordinator);
  }

  /**
   * Runs the GUI display of the demo: builds the root engine, seeds and starts the coordinator and
   * the GUI display, and shows the window. Participates in: FR-001, FR-007, SC-004.
   *
   * @param mode the execution mode
   */
  private static void runGui(final ExecutionMode mode) {
    final EngineImpl root = new EngineImpl(ROOT_NAME, TIME_FACTOR, mode);
    final TrafficCoordinator coordinator = new TrafficCoordinator(root, UNBOUNDED_DURATION);
    coordinator.seed();

    final TrafficLightGui display = new TrafficLightGui(root);

    root.registerAndStart(coordinator);
    root.registerAndStart(display);
    root.start();

    display.showWindow();
  }

  /**
   * Awaits the completion of the simulation. Participates in: FR-001, FR-006, SC-001, SC-002.
   *
   * @param coordinator the coordinator whose completion latch is awaited
   */
  private static void awaitCompletion(final TrafficCoordinator coordinator) {
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
   * Prints the final outcome summary of the simulation. Participates in: FR-001, FR-006, SC-001,
   * SC-002.
   *
   * @param mode the execution mode that was used
   * @param coordinator the coordinator holding the final totals and fleet state
   */
  private static void printOutcome(final ExecutionMode mode, final TrafficCoordinator coordinator) {
    System.out.print(formatOutcome(mode, coordinator));
  }

  /**
   * Formats the final outcome summary of the simulation. Participates in: FR-001, FR-006, SC-001,
   * SC-002.
   *
   * @param mode the execution mode that was used
   * @param coordinator the coordinator holding the final totals and fleet state
   * @return the formatted outcome text
   */
  static String formatOutcome(final ExecutionMode mode, final TrafficCoordinator coordinator) {
    final StringBuilder out = new StringBuilder("\n=== OUTCOME (").append(mode).append(") ===\n");
    final GridState state = coordinator.snapshot();
    out.append("vehicles=").append(state.getVehicleCount());
    out.append(", crossings=").append(coordinator.getTotalCrossings());
    out.append('\n');
    return out.toString();
  }
}
