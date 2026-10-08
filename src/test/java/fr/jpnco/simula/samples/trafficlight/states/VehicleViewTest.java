package fr.jpnco.simula.samples.trafficlight.states;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class VehicleViewTest {

  @Test
  void exposes_position_and_direction() {
    final VehicleView v = new VehicleView(7, 2, 3, Direction.WEST, 42.5);
    assertEquals(7, v.getId());
    assertEquals(2, v.getRow());
    assertEquals(3, v.getCol());
    assertEquals(Direction.WEST, v.getDirection());
    assertEquals(42.5, v.getDistanceInSegment());
  }
}
