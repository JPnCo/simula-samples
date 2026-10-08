package fr.jpnco.simula.samples.boids.states;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BoidStateTest {

  @Test
  void exposes_tick_position_and_velocity() {
    final BoidState s = new BoidState(3, 1, 10.0, 20.0, 2.0, -3.0);
    assertEquals(3, s.getTick());
    assertEquals(1, s.getId());
    assertEquals(10.0, s.getX());
    assertEquals(20.0, s.getY());
    assertEquals(2.0, s.getVx());
    assertEquals(-3.0, s.getVy());
  }
}
