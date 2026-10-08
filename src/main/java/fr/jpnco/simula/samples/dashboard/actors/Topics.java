package fr.jpnco.simula.samples.dashboard.actors;

/**
 * The event topic used by the dashboard observers.
 *
 * <p>The dashboard subscribes to the {@code new-state} topic, which is published by both the
 * traffic-light sample and the boids sample once per tick carrying the immutable snapshot. The
 * value is identical to the existing samples' {@code new-state} topic so the dashboard can attach
 * to either of them without modification (FR-002, FR-007).
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-007.
 */
public final class Topics {

  /** The {@code new-state} topic carrying each per-tick immutable snapshot. */
  public static final String NEW_STATE = "new-state";

  /** Private constructor to prevent instantiation of this constant holder. */
  private Topics() {}
}
