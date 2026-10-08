package fr.jpnco.simula.samples.dashboard.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TimeSeriesTest {

  @Test
  void empty_series_is_empty_with_zero_size() {
    final TimeSeries series = TimeSeries.empty();
    assertTrue(series.isEmpty());
    assertEquals(0, series.size());
  }

  @Test
  void append_returns_a_new_series_with_the_sample() {
    final TimeSeries empty = TimeSeries.empty();
    final TimeSeries one = empty.append(new Sample(1, 10.0));
    final TimeSeries two = one.append(new Sample(2, 20.0));

    assertTrue(empty.isEmpty());
    assertEquals(1, one.size());
    assertEquals(2, two.size());
    assertEquals(1, two.first().getSimTime());
    assertEquals(20.0, two.last().getValue());
    assertFalse(two.isEmpty());
  }

  @Test
  void first_and_last_return_ordered_samples() {
    final TimeSeries series =
        TimeSeries.empty().append(new Sample(1, 10.0)).append(new Sample(2, 20.0));
    assertEquals(1, series.first().getSimTime());
    assertEquals(2, series.last().getSimTime());
    assertEquals(2, series.getSamples().size());
  }

  @Test
  void first_on_empty_series_throws() {
    assertThrows(IllegalStateException.class, () -> TimeSeries.empty().first());
  }

  @Test
  void last_on_empty_series_throws() {
    assertThrows(IllegalStateException.class, () -> TimeSeries.empty().last());
  }
}
