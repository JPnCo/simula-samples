package fr.jpnco.simula.samples.dashboard.model;

/**
 * An immutable time-series record: one aggregate metric value captured from one simulation snapshot
 * at one simulated instant (FR-003).
 *
 * <p>Each {@code Sample} is keyed by the simulated time of the source snapshot and carries the
 * aggregate metric recorded from it (e.g. the vehicle count of a traffic-light {@code GridState} or
 * the boid count of a boids {@code FlockState}). Samples are produced one per tick by the {@link
 * fr.jpnco.simula.samples.dashboard.actors.Dashboard} actor and stored in a {@link TimeSeries}.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-003, SC-002.
 */
public final class Sample {

  /** The simulated time at which this sample was recorded. */
  private final int simTime;

  /** The aggregate metric value recorded from the snapshot. */
  private final double value;

  /**
   * Creates an immutable time-series sample. Participates in: FR-003, SC-002.
   *
   * @param simTime the simulated time of the source snapshot
   * @param value the aggregate metric value recorded from the snapshot
   */
  public Sample(final int simTime, final double value) {
    this.simTime = simTime;
    this.value = value;
  }

  /**
   * Returns the simulated time of the source snapshot. Participates in: FR-003, SC-002.
   *
   * @return the simulated time
   */
  public int getSimTime() {
    return simTime;
  }

  /**
   * Returns the aggregate metric value recorded from the snapshot. Participates in: FR-003, SC-002.
   *
   * @return the metric value
   */
  public double getValue() {
    return value;
  }
}
