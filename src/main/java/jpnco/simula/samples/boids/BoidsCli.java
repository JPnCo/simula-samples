package jpnco.simula.samples.boids;

import jpnco.simula.engine.ExecutionMode;

/**
 * Parses the command-line arguments of the boids demo into an execution mode, a display choice and
 * a {@link FlockParameters} configuration. Extracted from {@link BoidsDemo} so the parsing rules
 * are independently testable.
 *
 * <p>The supported overrides are {@code --boids=<n>}, {@code --perception-radius=<n>}, {@code
 * --max-speed=<n>}, {@code --separation-weight=<w>}, {@code --alignment-weight=<w>} and {@code
 * --cohesion-weight=<w>}; any other token is ignored and the defaults of {@link
 * FlockParameters#DEFAULT} are used for the parameters not explicitly overridden (FR-009).
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-001, FR-007, FR-008, FR-009, SC-001.
 */
public final class BoidsCli {

  /** The display choices of the demo. */
  public enum DisplayChoice {
    /** Renders the world to the console. */
    CONSOLE,
    /** Renders the world in a Swing window. */
    GUI
  }

  /** The command-line token that selects classic platform threads. */
  private static final String CLASSIC_ARG = "classic";

  /** The command-line token that selects the Swing GUI display. */
  private static final String GUI_ARG = "gui";

  /** The command-line prefix that overrides the number of boids. */
  private static final String BOIDS_PREFIX = "--boids=";

  /** The command-line prefix that overrides the perception radius. */
  private static final String PERCEPTION_RADIUS_PREFIX = "--perception-radius=";

  /** The command-line prefix that overrides the maximum speed. */
  private static final String MAX_SPEED_PREFIX = "--max-speed=";

  /** The command-line prefix that overrides the separation weight. */
  private static final String SEPARATION_WEIGHT_PREFIX = "--separation-weight=";

  /** The command-line prefix that overrides the alignment weight. */
  private static final String ALIGNMENT_WEIGHT_PREFIX = "--alignment-weight=";

  /** The command-line prefix that overrides the cohesion weight. */
  private static final String COHESION_WEIGHT_PREFIX = "--cohesion-weight=";

  /** Private constructor to prevent instantiation of this utility class. */
  private BoidsCli() {}

  /**
   * Resolves the execution mode from the command-line arguments. Participates in: FR-001, FR-007,
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
   * Resolves the display choice from the command-line arguments. Participates in: FR-001, FR-006,
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

  /**
   * Resolves the {@link FlockParameters} from the command-line arguments, applying any {@code
   * --boids=}, {@code --perception-radius=}, {@code --max-speed=}, {@code --separation-weight=},
   * {@code --alignment-weight=} or {@code --cohesion-weight=} override and defaulting the rest from
   * {@link FlockParameters#DEFAULT}. Participates in: FR-008, FR-009.
   *
   * @param args the command-line arguments
   * @return the resolved parameters
   */
  public static FlockParameters resolveParameters(final String[] args) {
    final FlockParameters defaults = FlockParameters.DEFAULT;
    int boidCount = defaults.getBoidCount();
    double separationWeight = defaults.getSeparationWeight();
    double alignmentWeight = defaults.getAlignmentWeight();
    double cohesionWeight = defaults.getCohesionWeight();
    double perceptionRadius = defaults.getPerceptionRadius();
    double maxSpeed = defaults.getMaxSpeed();
    for (final String arg : args) {
      if (arg.startsWith(BOIDS_PREFIX)) {
        boidCount = Integer.parseInt(arg.substring(BOIDS_PREFIX.length()));
      } else if (arg.startsWith(SEPARATION_WEIGHT_PREFIX)) {
        separationWeight = Double.parseDouble(arg.substring(SEPARATION_WEIGHT_PREFIX.length()));
      } else if (arg.startsWith(ALIGNMENT_WEIGHT_PREFIX)) {
        alignmentWeight = Double.parseDouble(arg.substring(ALIGNMENT_WEIGHT_PREFIX.length()));
      } else if (arg.startsWith(COHESION_WEIGHT_PREFIX)) {
        cohesionWeight = Double.parseDouble(arg.substring(COHESION_WEIGHT_PREFIX.length()));
      } else if (arg.startsWith(PERCEPTION_RADIUS_PREFIX)) {
        perceptionRadius = Double.parseDouble(arg.substring(PERCEPTION_RADIUS_PREFIX.length()));
      } else if (arg.startsWith(MAX_SPEED_PREFIX)) {
        maxSpeed = Double.parseDouble(arg.substring(MAX_SPEED_PREFIX.length()));
      }
    }
    return new FlockParameters(
        boidCount,
        separationWeight,
        alignmentWeight,
        cohesionWeight,
        perceptionRadius,
        maxSpeed,
        defaults.getWorldWidth(),
        defaults.getWorldHeight());
  }
}
