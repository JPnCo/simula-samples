package jpnco.simula.samples.boids.states;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BoidViewTest {

  @Test
  void exposes_position_and_velocity() {
    final BoidView b = new BoidView(7, 2.5, 3.5, -1.0, 0.5);
    assertEquals(7, b.getId());
    assertEquals(2.5, b.getX());
    assertEquals(3.5, b.getY());
    assertEquals(-1.0, b.getVx());
    assertEquals(0.5, b.getVy());
  }
}
