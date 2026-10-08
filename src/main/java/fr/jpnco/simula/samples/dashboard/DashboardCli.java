package fr.jpnco.simula.samples.dashboard;

import fr.jpnco.simula.engine.ExecutionMode;
import fr.jpnco.simula.samples.dashboard.DashboardParameters.Display;
import fr.jpnco.simula.samples.dashboard.DashboardParameters.Target;
import java.nio.file.Path;

/**
 * Parses the command-line arguments of the dashboard demo into a {@link DashboardParameters}
 * configuration (FR-009). Extracted from {@link DashboardDemo} so the parsing rules are
 * independently testable.
 *
 * <p>The supported tokens are {@code trafficlight} or {@code boids} (target), {@code classic}
 * (platform mode), {@code gui} (window display) and {@code export=<path>}; any other token is
 * ignored and the defaults of {@link DashboardParameters#DEFAULT} are used for the parameters not
 * explicitly overridden (FR-009).
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-009, SC-001.
 */
public final class DashboardCli {

  /** The command-line token that selects the traffic-light target. */
  private static final String TRAFFICLIGHT_ARG = "trafficlight";

  /** The command-line token that selects the boids target. */
  private static final String BOIDS_ARG = "boids";

  /** The command-line token that selects classic platform threads. */
  private static final String CLASSIC_ARG = "classic";

  /** The command-line token that selects the Swing GUI display. */
  private static final String GUI_ARG = "gui";

  /** The command-line prefix that sets the export file path. */
  private static final String EXPORT_PREFIX = "export=";

  /** Private constructor to prevent instantiation of this utility class. */
  private DashboardCli() {}

  /**
   * Resolves the target simulation from the command-line arguments. Participates in: FR-009,
   * SC-001.
   *
   * @param args the command-line arguments
   * @return {@link Target#TRAFFICLIGHT} if an argument is {@code trafficlight}, otherwise {@link
   *     Target#BOIDS}
   */
  public static Target resolveTarget(final String[] args) {
    for (final String arg : args) {
      if (TRAFFICLIGHT_ARG.equalsIgnoreCase(arg)) {
        return Target.TRAFFICLIGHT;
      }
    }
    return Target.BOIDS;
  }

  /**
   * Resolves the execution mode from the command-line arguments. Participates in: FR-009, SC-001.
   *
   * @param args the command-line arguments
   * @return {@link ExecutionMode#PLATFORM} if an argument is {@code classic}, otherwise {@link
   *     ExecutionMode#VIRTUAL}
   */
  public static ExecutionMode resolveMode(final String[] args) {
    for (final String arg : args) {
      if (CLASSIC_ARG.equalsIgnoreCase(arg)) {
        return ExecutionMode.PLATFORM;
      }
    }
    return ExecutionMode.VIRTUAL;
  }

  /**
   * Resolves the display choice from the command-line arguments. Participates in: FR-009, SC-001.
   *
   * @param args the command-line arguments
   * @return {@link Display#GUI} if an argument is {@code gui}, otherwise {@link Display#CONSOLE}
   */
  public static Display resolveDisplay(final String[] args) {
    for (final String arg : args) {
      if (GUI_ARG.equalsIgnoreCase(arg)) {
        return Display.GUI;
      }
    }
    return Display.CONSOLE;
  }

  /**
   * Resolves the optional export file path from the command-line arguments, or {@code null} when no
   * {@code export=<path>} token is present. Participates in: FR-006, FR-009.
   *
   * @param args the command-line arguments
   * @return the export path, or {@code null} if export is not requested
   */
  public static Path resolveExportPath(final String[] args) {
    for (final String arg : args) {
      if (arg.startsWith(EXPORT_PREFIX)) {
        return Path.of(arg.substring(EXPORT_PREFIX.length()));
      }
    }
    return null;
  }

  /**
   * Resolves the full {@link DashboardParameters} from the command-line arguments, defaulting the
   * unset values from {@link DashboardParameters#DEFAULT}. Participates in: FR-006, FR-009, SC-001.
   *
   * @param args the command-line arguments
   * @return the resolved parameters
   */
  public static DashboardParameters resolveParameters(final String[] args) {
    final Target target = resolveTarget(args);
    final ExecutionMode mode = resolveMode(args);
    final Display display = resolveDisplay(args);
    final Path exportPath = resolveExportPath(args);
    return new DashboardParameters(target, mode, display, exportPath);
  }
}
