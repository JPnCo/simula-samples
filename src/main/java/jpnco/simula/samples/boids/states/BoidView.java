package jpnco.simula.samples.boids.states;

/**
 * An immutable view of one boid's position and velocity, carried inside a {@link FlockState} so
 * displays can read it safely from another thread.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-003, FR-006.
 */
public final class BoidView {

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
   * Creates an immutable boid view.
   *
   * @param id the boid identity
   * @param x the horizontal position
   * @param y the vertical position
   * @param vx the horizontal velocity
   * @param vy the vertical velocity
   */
  public BoidView(final int id, final double x, final double y, final double vx, final double vy) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.vx = vx;
    this.vy = vy;
  }

  /**
   * Returns the boid identity. Participates in: FR-002, FR-003, FR-006.
   *
   * @return the boid id
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the boid position on the horizontal axis. Participates in: FR-002, FR-003, FR-006.
   *
   * @return the horizontal position
   */
  public double getX() {
    return x;
  }

  /**
   * Returns the boid position on the vertical axis. Participates in: FR-002, FR-003, FR-006.
   *
   * @return the vertical position
   */
  public double getY() {
    return y;
  }

  /**
   * Returns the boid velocity on the horizontal axis. Participates in: FR-002, FR-003, FR-006.
   *
   * @return the horizontal velocity
   */
  public double getVx() {
    return vx;
  }

  /**
   * Returns the boid velocity on the vertical axis. Participates in: FR-002, FR-003, FR-006.
   *
   * @return the vertical velocity
   */
  public double getVy() {
    return vy;
  }
}
