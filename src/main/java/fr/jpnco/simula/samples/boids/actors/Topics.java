package fr.jpnco.simula.samples.boids.actors;

/**
 * The event topics shared by the boids flocking simulation actors.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-003, FR-005, FR-006.
 */
public final class Topics {

  /** Asks every {@link Boid} to compute and broadcast its future state. */
  public static final String NEXT_BOID_STATES = "next-boid-states";

  /** Broadcast by a {@link Boid} carrying its new state. */
  public static final String BOID_STATE = "boid-state";

  /**
   * Broadcast by the {@link fr.jpnco.simula.actors.Barrier} once every boid of the tick has
   * reported its state, telling the coordinator the boid reports are complete.
   */
  public static final String BOIDS_READY = "boids-ready";

  /** Broadcast by the coordinator carrying the assembled snapshot. */
  public static final String NEW_STATE = "new-state";

  /** Private constructor to prevent instantiation of this constant holder. */
  private Topics() {}
}
