package jpnco.simula.samples.trafficlight.states;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TrafficLightStateTest {

  @Test
  void exposes_tick_position_and_bands() {
    final TrafficLightState s = new TrafficLightState(3, 1, 2, LightState.GREEN, LightState.RED);
    assertEquals(3, s.getTick());
    assertEquals(1, s.getRow());
    assertEquals(2, s.getCol());
    assertEquals(LightState.GREEN, s.getNs());
    assertEquals(LightState.RED, s.getEw());
  }
}
