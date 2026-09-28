package jpnco.simula.samples.boids;

/**
 * The configurable parameters of the flocking simulation (FR-009). The values are immutable and
 * exposed through getters. {@link #DEFAULT} carries the sensible defaults used when no command-line
 * override is supplied.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-003, FR-009.
 */
public final class FlockParameters {

  /** The default number of boids in the flock. */
  private static final int DEFAULT_BOID_COUNT = 40;

  /** The default separation weight. */
  private static final double DEFAULT_SEPARATION_WEIGHT = 1.0;

  /** The default alignment weight. */
  private static final double DEFAULT_ALIGNMENT_WEIGHT = 1.0;

  /** The default cohesion weight. */
  private static final double DEFAULT_COHESION_WEIGHT = 1.0;

  /** The default perception radius, in world units. */
  private static final double DEFAULT_PERCEPTION_RADIUS = 40.0;

  /** The default maximum speed, in world units per tick. */
  private static final double DEFAULT_MAX_SPEED = 4.0;

  /** The default world width, in world units. */
  private static final double DEFAULT_WORLD_WIDTH = 800.0;

  /** The default world height, in world units. */
  private static final double DEFAULT_WORLD_HEIGHT = 600.0;

  /** The sensible default parameters used when none are configured. */
  public static final FlockParameters DEFAULT =
      new FlockParameters(
          DEFAULT_BOID_COUNT,
          DEFAULT_SEPARATION_WEIGHT,
          DEFAULT_ALIGNMENT_WEIGHT,
          DEFAULT_COHESION_WEIGHT,
          DEFAULT_PERCEPTION_RADIUS,
          DEFAULT_MAX_SPEED,
          DEFAULT_WORLD_WIDTH,
          DEFAULT_WORLD_HEIGHT);

  /** The number of boids in the flock. */
  private final int boidCount;

  /** The weight of the separation rule. */
  private final double separationWeight;

  /** The weight of the alignment rule. */
  private final double alignmentWeight;

  /** The weight of the cohesion rule. */
  private final double cohesionWeight;

  /** The radius within which neighbours are considered. */
  private final double perceptionRadius;

  /** The maximum speed, in world units per tick. */
  private final double maxSpeed;

  /** The world width, in world units. */
  private final double worldWidth;

  /** The world height, in world units. */
  private final double worldHeight;

  /**
   * Creates an immutable set of flocking parameters. Participates in: FR-002, FR-003, FR-009.
   *
   * @param boidCount the number of boids
   * @param separationWeight the separation weight
   * @param alignmentWeight the alignment weight
   * @param cohesionWeight the cohesion weight
   * @param perceptionRadius the perception radius
   * @param maxSpeed the maximum speed
   * @param worldWidth the world width
   * @param worldHeight the world height
   */
  public FlockParameters(
      final int boidCount,
      final double separationWeight,
      final double alignmentWeight,
      final double cohesionWeight,
      final double perceptionRadius,
      final double maxSpeed,
      final double worldWidth,
      final double worldHeight) {
    this.boidCount = boidCount;
    this.separationWeight = separationWeight;
    this.alignmentWeight = alignmentWeight;
    this.cohesionWeight = cohesionWeight;
    this.perceptionRadius = perceptionRadius;
    this.maxSpeed = maxSpeed;
    this.worldWidth = worldWidth;
    this.worldHeight = worldHeight;
  }

  /**
   * Returns the number of boids in the flock. Participates in: FR-002, FR-003, FR-009.
   *
   * @return the boid count
   */
  public int getBoidCount() {
    return boidCount;
  }

  /**
   * Returns the weight of the separation rule. Participates in: FR-003, FR-009.
   *
   * @return the separation weight
   */
  public double getSeparationWeight() {
    return separationWeight;
  }

  /**
   * Returns the weight of the alignment rule. Participates in: FR-003, FR-009.
   *
   * @return the alignment weight
   */
  public double getAlignmentWeight() {
    return alignmentWeight;
  }

  /**
   * Returns the weight of the cohesion rule. Participates in: FR-003, FR-009.
   *
   * @return the cohesion weight
   */
  public double getCohesionWeight() {
    return cohesionWeight;
  }

  /**
   * Returns the radius within which neighbours are considered. Participates in: FR-003, FR-009.
   *
   * @return the perception radius
   */
  public double getPerceptionRadius() {
    return perceptionRadius;
  }

  /**
   * Returns the maximum speed, in world units per tick. Participates in: FR-003, FR-009.
   *
   * @return the maximum speed
   */
  public double getMaxSpeed() {
    return maxSpeed;
  }

  /**
   * Returns the world width, in world units. Participates in: FR-002, FR-004, FR-009.
   *
   * @return the world width
   */
  public double getWorldWidth() {
    return worldWidth;
  }

  /**
   * Returns the world height, in world units. Participates in: FR-002, FR-004, FR-009.
   *
   * @return the world height
   */
  public double getWorldHeight() {
    return worldHeight;
  }
}
