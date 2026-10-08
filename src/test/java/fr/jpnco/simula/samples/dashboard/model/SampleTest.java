package fr.jpnco.simula.samples.dashboard.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SampleTest {

  @Test
  void exposes_simTime_and_value() {
    final Sample sample = new Sample(7, 42.0);
    assertEquals(7, sample.getSimTime());
    assertEquals(42.0, sample.getValue());
  }
}
