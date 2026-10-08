package fr.jpnco.simula.samples.boids.states;

/**
 * An immutable report of one boid's position and velocity at one simulated tick, broadcast by a
 * {@link fr.jpnco.simula.samples.boids.actors.Boid} on the {@link
 * fr.jpnco.simula.samples.boids.actors.Topics#BOID_STATE} topic.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-003.
 */
public final class BoidState {

  /** The simulated tick this state belongs to (used to detect out-of-order events). */
  private final int tick;

  /** The boid identity. */
  private final int id;

  /** The boid position on the horizontal axis. */
  private final double x;

  /** The boid position on the vertical axis. */
  private final double y;

  /** The boid velocity on the horizontal axis. */
  private final double vx;

  /** The boid velocity on the vertical axis. */
  private final double vy;

  /**
   * Creates an immutable boid report.
   *
   * @param tick the simulated tick
   * @param id the boid identity
   * @param x the horizontal position
   * @param y the vertical position
   * @param vx the horizontal velocity
   * @param vy the vertical velocity
   */
  public BoidState(
      final int tick,
      final int id,
      final double x,
      final double y,
      final double vx,
      final double vy) {
    this.tick = tick;
    this.id = id;
    this.x = x;
    this.y = y;
    this.vx = vx;
    this.vy = vy;
  }

  /**
   * Returns the simulated tick this state belongs to. Participates in: FR-002, FR-003.
   *
   * @return the tick
   */
  public int getTick() {
    return tick;
  }

  /**
   * Returns the boid identity. Participates in: FR-002, FR-003.
   *
   * @return the boid id
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the boid position on the horizontal axis. Participates in: FR-002, FR-003.
   *
   * @return the horizontal position
   */
  public double getX() {
    return x;
  }

  /**
   * Returns the boid position on the vertical axis. Participates in: FR-002, FR-003.
   *
   * @return the vertical position
   */
  public double getY() {
    return y;
  }

  /**
   * Returns the boid velocity on the horizontal axis. Participates in: FR-002, FR-003.
   *
   * @return the horizontal velocity
   */
  public double getVx() {
    return vx;
  }

  /**
   * Returns the boid velocity on the vertical axis. Participates in: FR-002, FR-003.
   *
   * @return the vertical velocity
   */
  public double getVy() {
    return vy;
  }
}
