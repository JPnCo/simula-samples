package fr.jpnco.simula.samples.dashboard.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The ordered, append-only collection of {@link Sample}s recorded over a run (FR-003, FR-004).
 *
 * <p>The series is immutable: {@link #append} returns a new {@code TimeSeries} that contains the
 * previous samples plus the new one, and the stored samples are exposed through an unmodifiable
 * view. The {@link fr.jpnco.simula.samples.dashboard.actors.Dashboard} actor holds the current
 * series in a {@code volatile} field and replaces it on every recorded sample, so the console
 * monitor, the GUI and the exporter can read a consistent snapshot from another thread.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-003, FR-004, FR-008, SC-002.
 */
public final class TimeSeries {

  /** An empty time series, reused as the starting point of every run. */
  private static final TimeSeries EMPTY = new TimeSeries(List.of());

  /** The ordered samples of this series. */
  private final List<Sample> samples;

  /**
   * Creates a time series over the given samples, defensively copied. Participates in: FR-003,
   * FR-004, FR-008, SC-002.
   *
   * @param samples the ordered samples
   */
  private TimeSeries(final List<Sample> samples) {
    this.samples = Collections.unmodifiableList(new ArrayList<>(samples));
  }

  /**
   * Returns the empty time series. Participates in: FR-003, FR-008.
   *
   * @return the empty series
   */
  public static TimeSeries empty() {
    return EMPTY;
  }

  /**
   * Returns a new time series that appends the given sample to this one. Participates in: FR-003,
   * FR-004.
   *
   * @param sample the sample to append
   * @return a new series containing the previous samples and the new one
   */
  public TimeSeries append(final Sample sample) {
    final List<Sample> extended = new ArrayList<>(samples);
    extended.add(sample);
    return new TimeSeries(extended);
  }

  /**
   * Returns whether this series has no samples. Participates in: FR-008.
   *
   * @return {@code true} if there are no samples
   */
  public boolean isEmpty() {
    return samples.isEmpty();
  }

  /**
   * Returns the number of samples in this series. Participates in: FR-003, FR-004, SC-002.
   *
   * @return the sample count
   */
  public int size() {
    return samples.size();
  }

  /**
   * Returns the first sample by insertion order. Participates in: FR-004, SC-002.
   *
   * @return the first sample
   * @throws IllegalStateException if the series is empty
   */
  public Sample first() {
    if (samples.isEmpty()) {
      throw new IllegalStateException("Cannot read the first sample of an empty series");
    }
    return samples.get(0);
  }

  /**
   * Returns the last sample by insertion order. Participates in: FR-004, SC-002.
   *
   * @return the last sample
   * @throws IllegalStateException if the series is empty
   */
  public Sample last() {
    if (samples.isEmpty()) {
      throw new IllegalStateException("Cannot read the last sample of an empty series");
    }
    return samples.get(samples.size() - 1);
  }

  /**
   * Returns an unmodifiable view of the ordered samples. Participates in: FR-003, FR-006, SC-002.
   *
   * @return the ordered samples
   */
  public List<Sample> getSamples() {
    return samples;
  }
}
