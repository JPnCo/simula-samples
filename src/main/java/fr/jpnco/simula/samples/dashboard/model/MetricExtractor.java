package fr.jpnco.simula.samples.dashboard.model;

import fr.jpnco.simula.samples.boids.states.FlockState;
import fr.jpnco.simula.samples.trafficlight.states.GridState;

/**
 * Maps an immutable simulation snapshot into a {@link Sample}, extracting the simulated time and
 * the aggregate metric from the snapshot (FR-003, FR-007).
 *
 * <p>The dashboard subscribes to the {@code new-state} topic, which is published by both the
 * traffic-light sample (carrying a {@link GridState}) and the boids sample (carrying a {@link
 * FlockState}). This is the single point that turns either snapshot into a {@code Sample}, so the
 * {@link fr.jpnco.simula.samples.dashboard.actors.Dashboard} actor is payload-agnostic and works
 * with both target samples without modification.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-003, FR-007, SC-003.
 */
public final class MetricExtractor {

  /** Private constructor to prevent instantiation of this utility class. */
  private MetricExtractor() {}

  /**
   * Converts a snapshot into a {@link Sample}, reading the simulated time and the aggregate metric.
   * The metric is the vehicle count for a {@link GridState} and the boid count for a {@link
   * FlockState}. Participates in: FR-003, FR-007, SC-003.
   *
   * @param snapshot the immutable snapshot received on {@code new-state}
   * @return the extracted sample
   * @throws IllegalArgumentException if the snapshot is neither a {@link GridState} nor a {@link
   *     FlockState}
   */
  public static Sample toSample(final Object snapshot) {
    if (snapshot instanceof GridState gridState) {
      return new Sample(gridState.getSimTime(), gridState.getVehicleCount());
    }
    if (snapshot instanceof FlockState flockState) {
      return new Sample(flockState.getSimTime(), flockState.getBoidCount());
    }
    throw new IllegalArgumentException(
        "Unsupported snapshot type: " + snapshot.getClass().getName());
  }
}
