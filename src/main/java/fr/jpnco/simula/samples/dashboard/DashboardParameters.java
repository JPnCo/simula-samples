package fr.jpnco.simula.samples.dashboard;

import fr.jpnco.simula.engine.ExecutionMode;
import java.nio.file.Path;

/**
 * The immutable configuration of a dashboard run (FR-009).
 *
 * <p>It captures which existing simulation to attach to ({@link Target}), the execution mode, the
 * display choice and the optional export file path. {@link #DEFAULT} carries the sensible defaults
 * used when no command-line override is supplied.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-009, SC-001.
 */
public final class DashboardParameters {

  /** The existing simulation the dashboard attaches to. */
  public enum Target {
    /** The traffic-light grid sample. */
    TRAFFICLIGHT,
    /** The boids flocking sample. */
    BOIDS
  }

  /** The display used to present the collected time series. */
  public enum Display {
    /** Prints the series to the console. */
    CONSOLE,
    /** Plots the series in a Swing window. */
    GUI
  }

  /** The default target simulation, {@link Target#BOIDS}. */
  private static final Target DEFAULT_TARGET = Target.BOIDS;

  /** The default display choice, {@link Display#CONSOLE}. */
  private static final Display DEFAULT_DISPLAY = Display.CONSOLE;

  /** The default execution mode, {@link ExecutionMode#VIRTUAL}. */
  private static final ExecutionMode DEFAULT_MODE = ExecutionMode.VIRTUAL;

  /** The sensible default parameters used when none are configured. */
  public static final DashboardParameters DEFAULT =
      new DashboardParameters(DEFAULT_TARGET, DEFAULT_MODE, DEFAULT_DISPLAY, null);

  /** The simulation the dashboard attaches to. */
  private final Target target;

  /** The execution mode of the simulation. */
  private final ExecutionMode mode;

  /** The display used to present the collected series. */
  private final Display display;

  /** The optional export file path, or {@code null} when export is disabled. */
  private final Path exportPath;

  /**
   * Creates an immutable dashboard configuration. Participates in: FR-009, SC-001.
   *
   * @param target the simulation to attach to
   * @param mode the execution mode
   * @param display the display choice
   * @param exportPath the optional export file path, or {@code null} for none
   */
  public DashboardParameters(
      final Target target, final ExecutionMode mode, final Display display, final Path exportPath) {
    this.target = target;
    this.mode = mode;
    this.display = display;
    this.exportPath = exportPath;
  }

  /**
   * Returns the simulation the dashboard attaches to. Participates in: FR-009, SC-001.
   *
   * @return the target
   */
  public Target getTarget() {
    return target;
  }

  /**
   * Returns the execution mode of the simulation. Participates in: FR-009, SC-001.
   *
   * @return the mode
   */
  public ExecutionMode getMode() {
    return mode;
  }

  /**
   * Returns the display used to present the collected series. Participates in: FR-009, SC-001.
   *
   * @return the display choice
   */
  public Display getDisplay() {
    return display;
  }

  /**
   * Returns the optional export file path, or {@code null} when export is disabled. Participates
   * in: FR-006, FR-009.
   *
   * @return the export path, may be {@code null}
   */
  public Path getExportPath() {
    return exportPath;
  }
}
