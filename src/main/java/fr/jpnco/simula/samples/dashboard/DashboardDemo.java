package fr.jpnco.simula.samples.dashboard;

import fr.jpnco.simula.actors.Logger;
import fr.jpnco.simula.actors.Logger.Level;
import fr.jpnco.simula.engine.EngineImpl;
import fr.jpnco.simula.samples.boids.FlockParameters;
import fr.jpnco.simula.samples.boids.actors.BoidsCoordinator;
import fr.jpnco.simula.samples.boids.actors.Topics;
import fr.jpnco.simula.samples.dashboard.DashboardParameters.Display;
import fr.jpnco.simula.samples.dashboard.DashboardParameters.Target;
import fr.jpnco.simula.samples.dashboard.actors.Dashboard;
import fr.jpnco.simula.samples.dashboard.actors.DashboardGui;
import fr.jpnco.simula.samples.dashboard.actors.DashboardMonitor;
import fr.jpnco.simula.samples.dashboard.export.CsvExporter;
import fr.jpnco.simula.samples.dashboard.model.TimeSeries;
import fr.jpnco.simula.samples.trafficlight.actors.TrafficCoordinator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Runnable illustration of the simula framework: a dashboard that attaches to an existing
 * simulation (the traffic-light or boids sample) as an external observer and collects a time series
 * from its {@code new-state} events (FR-001, FR-002).
 *
 * <p>The dashboard subscribes to the {@code new-state} topic of the selected target on the same
 * root engine, records one sample per tick (keyed by the simulated time carried by each immutable
 * snapshot), and presents the collected series through a console summary, a Swing plot, or an
 * optional CSV export. It works with both target samples without modification (FR-007).
 *
 * <p>Usage: {@code DashboardDemo [target] [mode] [display] [export=<path>]} where {@code target} is
 * {@code boids} (default) or {@code trafficlight}, {@code mode} is {@code virtual} (default) or
 * {@code classic}, {@code display} is {@code console} (default) or {@code gui}, and {@code
 * export=<path>} enables writing the collected series to a file.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-001, FR-002, FR-004, FR-005, FR-006, FR-007, FR-008, FR-009, SC-001, SC-002,
 * SC-003, SC-004, SC-005.
 */
public final class DashboardDemo {

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

  /** The default boids parameters used to build the target simulation. */
  private static final FlockParameters DEFAULT_BOIDS_PARAMETERS = FlockParameters.DEFAULT;

  /** Private constructor to prevent instantiation of this entry point. */
  private DashboardDemo() {}

  /**
   * Entry point of the dashboard demo. Participates in: FR-001, FR-004, FR-005, FR-006, FR-007,
   * FR-008, FR-009, SC-001, SC-002, SC-003, SC-004, SC-005.
   *
   * @param args optional tokens: {@code trafficlight} or {@code boids} target, {@code classic}
   *     mode, {@code gui} display and {@code export=<path>}
   */
  public static void main(final String[] args) {
    final DashboardParameters params = DashboardCli.resolveParameters(args);
    Logger.forceLevel(Level.ERROR);

    if (params.getDisplay() == Display.GUI) {
      runGui(params);
    } else {
      runConsole(SIMULATED_SECONDS, params);
      System.exit(0);
    }
  }

  /**
   * Runs the console display of the demo: builds the selected target simulation on its own root
   * engine, attaches the {@link Dashboard} and the {@link DashboardMonitor}, awaits completion,
   * prints the summary and exports the series if requested. Participates in: FR-001, FR-002,
   * FR-004, FR-006, FR-007, FR-008, FR-009, SC-001, SC-002, SC-003, SC-005.
   *
   * @param durationSeconds the number of simulated seconds to run before stopping
   * @param params the dashboard configuration
   */
  static void runConsole(final int durationSeconds, final DashboardParameters params) {
    final EngineImpl root = new EngineImpl(ROOT_NAME, TIME_FACTOR, params.getMode());
    final String topic = subscribeTopic(params.getTarget());
    final Dashboard dashboard = new Dashboard(root, topic);
    final DashboardMonitor monitor = new DashboardMonitor(root, topic);
    final CountDownLatch done = seedCoordinator(root, params.getTarget(), durationSeconds);

    root.registerAndStart(dashboard);
    root.registerAndStart(monitor);
    root.start();

    awaitCompletion(done);
    monitor.printSummary(dashboard.snapshot());
    exportIfRequested(params, dashboard.snapshot());
  }

  /**
   * Runs the GUI display of the demo: builds the selected target simulation with an unbounded
   * duration, attaches the {@link Dashboard} and the {@link DashboardGui}, and shows the window.
   * Participates in: FR-001, FR-002, FR-005, FR-007, FR-008, FR-009, SC-004.
   *
   * @param params the dashboard configuration
   */
  private static void runGui(final DashboardParameters params) {
    final EngineImpl root = new EngineImpl(ROOT_NAME, TIME_FACTOR, params.getMode());
    final String topic = subscribeTopic(params.getTarget());
    final Dashboard dashboard = new Dashboard(root, topic);
    final DashboardGui display = new DashboardGui(root, dashboard);
    seedCoordinator(root, params.getTarget(), UNBOUNDED_DURATION);

    root.registerAndStart(dashboard);
    root.registerAndStart(display);
    root.start();

    display.showWindow();
  }

  /**
   * Seeds and starts the selected target's coordinator on the given engine. Participates in:
   * FR-001, FR-007, FR-009.
   *
   * @param engine the engine the coordinator lives on
   * @param target the target simulation
   * @param durationSeconds the simulated duration
   * @return the coordinator's completion latch
   */
  private static CountDownLatch seedCoordinator(
      final EngineImpl engine, final Target target, final int durationSeconds) {
    if (target == Target.TRAFFICLIGHT) {
      final TrafficCoordinator coordinator = new TrafficCoordinator(engine, durationSeconds);
      coordinator.seed();
      engine.registerAndStart(coordinator);
      return coordinator.getDoneLatch();
    }
    final BoidsCoordinator coordinator =
        new BoidsCoordinator(engine, DEFAULT_BOIDS_PARAMETERS, durationSeconds);
    coordinator.seed();
    engine.registerAndStart(coordinator);
    return coordinator.getDoneLatch();
  }

  /**
   * Returns the {@code new-state} topic of the given target sample. Participates in: FR-002,
   * FR-007, FR-009.
   *
   * @param target the target simulation
   * @return the {@code new-state} topic string
   */
  static String subscribeTopic(final Target target) {
    return target == Target.TRAFFICLIGHT
        ? fr.jpnco.simula.samples.trafficlight.actors.Topics.NEW_STATE
        : Topics.NEW_STATE;
  }

  /**
   * Awaits the completion of the simulation. Participates in: FR-004, FR-008, SC-002.
   *
   * @param done the coordinator's completion latch
   */
  private static void awaitCompletion(final CountDownLatch done) {
    try {
      if (!done.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        System.err.println("Simulation did not complete within the timeout.");
      }
      Thread.sleep(DRAIN_MILLIS);
    } catch (final InterruptedException exc) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Writes the collected series to the configured export file path, when one was requested.
   * Participates in: FR-006, FR-008, SC-005.
   *
   * @param params the dashboard configuration
   * @param series the collected series
   */
  private static void exportIfRequested(final DashboardParameters params, final TimeSeries series) {
    if (params.getExportPath() != null) {
      CsvExporter.write(params.getExportPath(), series);
      System.out.println("Exported " + series.size() + " samples to " + params.getExportPath());
    }
  }
}
