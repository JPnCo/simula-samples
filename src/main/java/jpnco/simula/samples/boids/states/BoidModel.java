package jpnco.simula.samples.boids.states;

import java.util.List;

/**
 * The pure, stateless implementation of the Reynolds flocking rules (FR-003) used by the {@link
 * jpnco.simula.samples.boids.actors.Boid} actors. Every method is deterministic, takes all inputs
 * as arguments and returns a new result, so the rules are directly unit-testable. The world is
 * toroidal (FR-004): neighbour distances are computed on the toroidal surface and {@link #wrap}
 * keeps positions inside the world.
 *
 * <p>All rule weights, the perception radius and the maximum speed are configurable parameters
 * (FR-009); the default values are carried by {@link
 * jpnco.simula.samples.boids.FlockParameters#DEFAULT}.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-003, FR-004, FR-009.
 */
public final class BoidModel {

  /** The unit push applied when a neighbour exactly overlaps the boid, to avoid stacking. */
  private static final double OVERLAP_PUSH = 1.0;

  /** A small tolerance used to decide whether a distance is zero. */
  private static final double EPSILON = 1e-9;

  /** Private constructor to prevent instantiation of this utility class. */
  private BoidModel() {}

  /**
   * Wraps a position around the toroidal world so it always lies in {@code [0, extent)}. A value
   * strictly below zero or at or above the extent is moved by one full wrap. Participates in:
   * FR-004.
   *
   * @param x the horizontal position
   * @param y the vertical position
   * @param worldWidth the world width
   * @param worldHeight the world height
   * @return the wrapped {@code [x, y]} position
   */
  public static double[] wrap(
      final double x, final double y, final double worldWidth, final double worldHeight) {
    return new double[] {wrapAxis(x, worldWidth), wrapAxis(y, worldHeight)};
  }

  /**
   * Wraps a single coordinate onto the circle of the given extent. Participates in: FR-004.
   *
   * @param value the coordinate
   * @param extent the world extent on that axis
   * @return the wrapped coordinate in {@code [0, extent)}
   */
  private static double wrapAxis(final double value, final double extent) {
    double wrapped = value % extent;
    if (wrapped < 0.0) {
      wrapped += extent;
    }
    return wrapped;
  }

  /**
   * Returns the shortest signed difference between two coordinates on a toroidal axis of the given
   * extent. Participates in: FR-003, FR-004.
   *
   * @param a the first coordinate
   * @param b the second coordinate
   * @param extent the world extent on that axis
   * @return the signed delta in {@code (-extent/2, extent/2]}
   */
  private static double toroidalDelta(final double a, final double b, final double extent) {
    double delta = a - b;
    if (delta > extent / 2.0) {
      delta -= extent;
    } else if (delta < -extent / 2.0) {
      delta += extent;
    }
    return delta;
  }

  /**
   * Computes the separation steering force: it pushes the boid away from every neighbour within the
   * perception radius, most strongly from the closest ones. Neighbour distances are toroidal. When
   * a neighbour exactly overlaps the boid a fixed unit push is applied so the boids do not stack
   * permanently. Participates in: FR-003, FR-004, FR-009.
   *
   * @param x the boid horizontal position
   * @param y the boid vertical position
   * @param neighbors the neighbour boids
   * @param perceptionRadius the perception radius
   * @param worldWidth the world width
   * @param worldHeight the world height
   * @return the {@code [sx, sy]} separation force
   */
  public static double[] separation(
      final double x,
      final double y,
      final List<BoidView> neighbors,
      final double perceptionRadius,
      final double worldWidth,
      final double worldHeight) {
    double sx = 0.0;
    double sy = 0.0;
    for (final BoidView neighbor : neighbors) {
      final double dx = toroidalDelta(neighbor.getX(), x, worldWidth);
      final double dy = toroidalDelta(neighbor.getY(), y, worldHeight);
      final double distance = Math.hypot(dx, dy);
      if (distance > perceptionRadius) {
        continue;
      }
      if (distance < EPSILON) {
        sx += OVERLAP_PUSH;
      } else {
        final double weight = 1.0 / distance;
        sx -= dx * weight;
        sy -= dy * weight;
      }
    }
    return new double[] {sx, sy};
  }

  /**
   * Computes the alignment steering force: the average velocity of the neighbours, which the boid
   * steers toward. Participates in: FR-003.
   *
   * @param neighbors the neighbour boids
   * @return the {@code [ax, ay]} alignment force
   */
  public static double[] alignment(final List<BoidView> neighbors) {
    double ax = 0.0;
    double ay = 0.0;
    for (final BoidView neighbor : neighbors) {
      ax += neighbor.getVx();
      ay += neighbor.getVy();
    }
    if (!neighbors.isEmpty()) {
      ax /= neighbors.size();
      ay /= neighbors.size();
    }
    return new double[] {ax, ay};
  }

  /**
   * Computes the cohesion steering force: it points from the boid toward the average position of
   * the neighbours, computed on the toroidal surface. Participates in: FR-003, FR-004.
   *
   * @param x the boid horizontal position
   * @param y the boid vertical position
   * @param neighbors the neighbour boids
   * @param worldWidth the world width
   * @param worldHeight the world height
   * @return the {@code [cx, cy]} cohesion force
   */
  public static double[] cohesion(
      final double x,
      final double y,
      final List<BoidView> neighbors,
      final double worldWidth,
      final double worldHeight) {
    double ax = 0.0;
    double ay = 0.0;
    for (final BoidView neighbor : neighbors) {
      ax += neighbor.getX();
      ay += neighbor.getY();
    }
    if (neighbors.isEmpty()) {
      return new double[] {0.0, 0.0};
    }
    ax /= neighbors.size();
    ay /= neighbors.size();
    return new double[] {toroidalDelta(ax, x, worldWidth), toroidalDelta(ay, y, worldHeight)};
  }

  /**
   * Caps the magnitude of a velocity at the given maximum speed, preserving its direction. A
   * velocity at or below the maximum is returned unchanged. Participates in: FR-003, FR-009.
   *
   * @param vx the horizontal velocity
   * @param vy the vertical velocity
   * @param maxSpeed the maximum speed
   * @return the capped {@code [vx, vy]} velocity
   */
  public static double[] limitSpeed(final double vx, final double vy, final double maxSpeed) {
    final double speed = Math.hypot(vx, vy);
    if (speed > maxSpeed && speed > EPSILON) {
      final double scale = maxSpeed / speed;
      return new double[] {vx * scale, vy * scale};
    }
    return new double[] {vx, vy};
  }

  /**
   * Computes the next velocity of a boid by combining the three weighted flocking rules — current
   * velocity plus weighted separation, alignment and cohesion — and then capping the result at the
   * maximum speed. When there are no neighbours every rule is zero and the boid simply coasts at
   * its current velocity. Participates in: FR-003, FR-004, FR-009.
   *
   * @param x the boid horizontal position
   * @param y the boid vertical position
   * @param vx the current horizontal velocity
   * @param vy the current vertical velocity
   * @param neighbors the neighbour boids
   * @param separationWeight the separation weight
   * @param alignmentWeight the alignment weight
   * @param cohesionWeight the cohesion weight
   * @param perceptionRadius the perception radius
   * @param maxSpeed the maximum speed
   * @param worldWidth the world width
   * @param worldHeight the world height
   * @return the next {@code [vx, vy]} velocity, capped at the maximum speed
   */
  public static double[] nextVelocity(
      final double x,
      final double y,
      final double vx,
      final double vy,
      final List<BoidView> neighbors,
      final double separationWeight,
      final double alignmentWeight,
      final double cohesionWeight,
      final double perceptionRadius,
      final double maxSpeed,
      final double worldWidth,
      final double worldHeight) {
    final double[] separation =
        separation(x, y, neighbors, perceptionRadius, worldWidth, worldHeight);
    final double[] alignment = alignment(neighbors);
    final double[] cohesion = cohesion(x, y, neighbors, worldWidth, worldHeight);
    final double nextX =
        vx
            + separation[0] * separationWeight
            + alignment[0] * alignmentWeight
            + cohesion[0] * cohesionWeight;
    final double nextY =
        vy
            + separation[1] * separationWeight
            + alignment[1] * alignmentWeight
            + cohesion[1] * cohesionWeight;
    return limitSpeed(nextX, nextY, maxSpeed);
  }
}
