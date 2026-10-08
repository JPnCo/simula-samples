package fr.jpnco.simula.samples.boids.states;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An immutable snapshot of the flock at one simulated instant, broadcast by the {@link
 * fr.jpnco.simula.samples.boids.actors.BoidsCoordinator} after every completed tick. It is read by
 * the console and GUI displays, which may run on a different thread than the coordinator.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-003, FR-006, SC-004.
 */
public final class FlockState {

  /** The simulated time at which this snapshot was taken. */
  private final int simTime;

  /** The world width, in world units. */
  private final double worldWidth;

  /** The world height, in world units. */
  private final double worldHeight;

  /** The positions and velocities of the boids at this instant. */
  private final List<BoidView> boids;

  /**
   * Creates an immutable snapshot, defensively copying the boids. Participates in: FR-002, FR-003,
   * FR-006, SC-004.
   *
   * @param simTime the simulated time
   * @param boids the boid views
   * @param worldWidth the world width
   * @param worldHeight the world height
   */
  public FlockState(
      final int simTime,
      final List<BoidView> boids,
      final double worldWidth,
      final double worldHeight) {
    this.simTime = simTime;
    this.worldWidth = worldWidth;
    this.worldHeight = worldHeight;
    this.boids = Collections.unmodifiableList(new ArrayList<>(boids));
  }

  /**
   * Returns the simulated time of this snapshot. Participates in: FR-002, FR-003, FR-006, SC-004.
   *
   * @return the simulated time
   */
  public int getSimTime() {
    return simTime;
  }

  /**
   * Returns the world width, in world units. Participates in: FR-002, FR-004.
   *
   * @return the world width
   */
  public double getWorldWidth() {
    return worldWidth;
  }

  /**
   * Returns the world height, in world units. Participates in: FR-002, FR-004.
   *
   * @return the world height
   */
  public double getWorldHeight() {
    return worldHeight;
  }

  /**
   * Returns the number of boids in the flock. Participates in: FR-002, FR-003, FR-006.
   *
   * @return the boid count
   */
  public int getBoidCount() {
    return boids.size();
  }

  /**
   * Returns an unmodifiable view of the boids. Participates in: FR-002, FR-003, FR-006, SC-004.
   *
   * @return the boids
   */
  public List<BoidView> getBoids() {
    return boids;
  }
}
