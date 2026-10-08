package fr.jpnco.simula.samples.trafficlight;

import fr.jpnco.simula.engine.ExecutionMode;

/**
 * Parses the command-line arguments of the traffic-light demo into an execution mode and a display
 * choice. Extracted from {@link TrafficLightDemo} so the parsing rules are independently testable.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-001, FR-008, SC-001.
 */
public final class TrafficLightCli {

  /** The display choices of the demo. */
  public enum DisplayChoice {
    /** Renders the grid to the console. */
    CONSOLE,
    /** Renders the grid in a Swing window. */
    GUI
  }

  /** The command-line token that selects classic platform threads. */
  private static final String CLASSIC_ARG = "classic";

  /** The command-line token that selects the Swing GUI display. */
  private static final String GUI_ARG = "gui";

  /** Private constructor to prevent instantiation of this utility class. */
  private TrafficLightCli() {}

  /**
   * Resolves the execution mode from the command-line arguments. Participates in: FR-001, FR-008,
   * SC-001.
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
   * Resolves the display choice from the command-line arguments. Participates in: FR-001, FR-008,
   * SC-001.
   *
   * @param args the command-line arguments
   * @return {@link DisplayChoice#GUI} if an argument is {@code gui}, otherwise {@link
   *     DisplayChoice#CONSOLE}
   */
  public static DisplayChoice resolveDisplay(final String[] args) {
    for (final String arg : args) {
      if (GUI_ARG.equalsIgnoreCase(arg)) {
        return DisplayChoice.GUI;
      }
    }
    return DisplayChoice.CONSOLE;
  }
}
