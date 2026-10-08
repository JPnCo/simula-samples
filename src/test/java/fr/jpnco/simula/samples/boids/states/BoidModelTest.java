package fr.jpnco.simula.samples.boids.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Verifies the pure Reynolds flocking rules implemented by {@link BoidModel}. The rules must be
 * deterministic, respect a configurable perception radius and maximum speed, and operate on a
 * toroidal world (FR-003, FR-004, FR-009).
 */
class BoidModelTest {

  private static final double EPSILON = 1e-9;
  private static final double WORLD_WIDTH = 800.0;
  private static final double WORLD_HEIGHT = 600.0;

  private static BoidView neighbor(final double x, final double y) {
    return new BoidView(0, x, y, 0.0, 0.0);
  }

  private static BoidView neighbor(
      final double x, final double y, final double vx, final double vy) {
    return new BoidView(0, x, y, vx, vy);
  }

  @Test
  void wrap_keeps_position_inside_the_world() {
    assertEquals(10.0, BoidModel.wrap(810.0, 0.0, WORLD_WIDTH, WORLD_HEIGHT)[0]);
    assertEquals(790.0, BoidModel.wrap(-10.0, 0.0, WORLD_WIDTH, WORLD_HEIGHT)[0]);
    assertEquals(10.0, BoidModel.wrap(0.0, 610.0, WORLD_WIDTH, WORLD_HEIGHT)[1]);
    assertEquals(590.0, BoidModel.wrap(0.0, -10.0, WORLD_WIDTH, WORLD_HEIGHT)[1]);
  }

  @Test
  void wrap_leaves_interior_position_unchanged() {
    final double[] wrapped = BoidModel.wrap(400.0, 300.0, WORLD_WIDTH, WORLD_HEIGHT);
    assertEquals(400.0, wrapped[0]);
    assertEquals(300.0, wrapped[1]);
  }

  @Test
  void limitSpeed_caps_velocity_at_max_speed() {
    final double[] capped = BoidModel.limitSpeed(10.0, 0.0, 4.0);
    assertEquals(4.0, Math.hypot(capped[0], capped[1]), EPSILON);
  }

  @Test
  void limitSpeed_leaves_velocity_under_max_speed_unchanged() {
    final double[] capped = BoidModel.limitSpeed(1.0, 1.0, 4.0);
    assertEquals(1.0, capped[0], EPSILON);
    assertEquals(1.0, capped[1], EPSILON);
  }

  @Test
  void separation_pushes_away_from_a_close_neighbor() {
    // A neighbor exactly ahead on the x axis: separation must push back in the -x direction.
    final double[] sep =
        BoidModel.separation(
            0.0, 0.0, List.of(neighbor(10.0, 0.0)), 40.0, WORLD_WIDTH, WORLD_HEIGHT);
    assertTrue(sep[0] < 0.0, "separation must push away from the neighbor");
  }

  @Test
  void separation_ignores_neighbors_beyond_perception_radius() {
    final double[] sep =
        BoidModel.separation(
            0.0, 0.0, List.of(neighbor(100.0, 0.0)), 40.0, WORLD_WIDTH, WORLD_HEIGHT);
    assertEquals(0.0, sep[0], EPSILON);
    assertEquals(0.0, sep[1], EPSILON);
  }

  @Test
  void separation_handles_exact_overlap() {
    // A neighbor exactly on top: the separation must be non-zero so boids do not stack.
    final double[] sep =
        BoidModel.separation(
            5.0, 5.0, List.of(neighbor(5.0, 5.0)), 40.0, WORLD_WIDTH, WORLD_HEIGHT);
    assertTrue(Math.hypot(sep[0], sep[1]) > 0.0, "overlapping boids must be pushed apart");
  }

  @Test
  void alignment_returns_average_neighbor_velocity() {
    final double[] align =
        BoidModel.alignment(
            List.of(neighbor(1, 2, 3.0, 0.0), neighbor(3, 4, 5.0, 0.0), neighbor(5, 6, 4.0, 0.0)));
    assertEquals(4.0, align[0], EPSILON);
    assertEquals(0.0, align[1], EPSILON);
  }

  @Test
  void alignment_returns_zero_with_no_neighbors() {
    final double[] align = BoidModel.alignment(List.of());
    assertEquals(0.0, align[0], EPSILON);
    assertEquals(0.0, align[1], EPSILON);
  }

  @Test
  void cohesion_steers_toward_average_neighbor_position() {
    // Neighbors to the right: cohesion must steer in the +x direction.
    final double[] coh =
        BoidModel.cohesion(
            0.0, 0.0, List.of(neighbor(30.0, 0.0), neighbor(50.0, 0.0)), WORLD_WIDTH, WORLD_HEIGHT);
    assertTrue(coh[0] > 0.0, "cohesion must steer toward the average neighbor position");
  }

  @Test
  void cohesion_returns_zero_with_no_neighbors() {
    final double[] coh = BoidModel.cohesion(0.0, 0.0, List.of(), WORLD_WIDTH, WORLD_HEIGHT);
    assertEquals(0.0, coh[0], EPSILON);
    assertEquals(0.0, coh[1], EPSILON);
  }

  @Test
  void nextVelocity_combines_weighted_rules_and_limits_speed() {
    // Current velocity 0; a neighbor to the right (cohesion + separation) and aligned heading.
    final double[] next =
        BoidModel.nextVelocity(
            0.0,
            0.0,
            0.0,
            0.0,
            List.of(neighbor(30.0, 0.0, 2.0, 0.0)),
            1.0,
            1.0,
            1.0,
            40.0,
            4.0,
            WORLD_WIDTH,
            WORLD_HEIGHT);
    assertTrue(Math.hypot(next[0], next[1]) <= 4.0, "next velocity must be capped at max speed");
    assertTrue(next[0] > 0.0, "a right-side neighbor must move the boid to the right");
  }

  @Test
  void nextVelocity_coasts_when_there_are_no_neighbors() {
    final double[] next =
        BoidModel.nextVelocity(
            100.0, 100.0, 3.0, 1.0, List.of(), 1.0, 1.0, 1.0, 40.0, 4.0, WORLD_WIDTH, WORLD_HEIGHT);
    assertEquals(3.0, next[0], EPSILON);
    assertEquals(1.0, next[1], EPSILON);
  }

  @Test
  void nextVelocity_respects_configurable_weights() {
    // A very large cohesion weight must dominate the result. A close neighbour is used so the
    // weak (weight 1) response stays below the maximum speed and the strong (weight 100) response
    // is speed-limited at the maximum, making the stronger response measurably larger.
    final double[] weak =
        BoidModel.nextVelocity(
            0.0,
            0.0,
            0.0,
            0.0,
            List.of(neighbor(3.0, 0.0)),
            1.0,
            1.0,
            1.0,
            40.0,
            4.0,
            WORLD_WIDTH,
            WORLD_HEIGHT);
    final double[] strong =
        BoidModel.nextVelocity(
            0.0,
            0.0,
            0.0,
            0.0,
            List.of(neighbor(3.0, 0.0)),
            1.0,
            1.0,
            100.0,
            40.0,
            4.0,
            WORLD_WIDTH,
            WORLD_HEIGHT);
    assertTrue(
        Math.hypot(strong[0], strong[1]) > Math.hypot(weak[0], weak[1]),
        "a larger cohesion weight must produce a larger steering response");
  }
}
