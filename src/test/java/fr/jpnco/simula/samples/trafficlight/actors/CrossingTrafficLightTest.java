package fr.jpnco.simula.samples.trafficlight.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.engine.EventImpl;
import fr.jpnco.simula.samples.trafficlight.states.LightState;
import fr.jpnco.simula.samples.trafficlight.states.TrafficLightState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CrossingTrafficLightTest {

  private Engine engine;

  @BeforeEach
  void setUp() {
    engine = mock(Engine.class);
  }

  private static Event nextStatesEvent(final CrossingTrafficLight light, final int tick) {
    return EventImpl.createEvent(Topics.NEXT_TRAFFIC_LIGHT_STATES, light, tick);
  }

  private TrafficLightState emittedState(final int tick) {
    final CrossingTrafficLight light = new CrossingTrafficLight(engine, 1, 2, 5, 2, 0);
    org.mockito.Mockito.clearInvocations(engine);
    light.process(nextStatesEvent(light, tick));
    final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
    verify(engine).signal(captor.capture());
    final Event emitted = captor.getValue();
    assertEquals(Topics.TRAFFIC_LIGHT_STATE, emitted.getTopic());
    return (TrafficLightState) emitted.getParameters()[0];
  }

  @Test
  void green_then_orange_on_north_south_band_then_switch() {
    // greenDuration=5, orangeDuration=2, phaseOffset=0 -> halfCycle=7, cycle=14
    TrafficLightState s = emittedState(0);
    assertEquals(LightState.GREEN, s.getNs());
    assertEquals(LightState.RED, s.getEw());

    s = emittedState(5);
    assertEquals(LightState.ORANGE, s.getNs());
    assertEquals(LightState.RED, s.getEw());

    s = emittedState(7);
    assertEquals(LightState.RED, s.getNs());
    assertEquals(LightState.GREEN, s.getEw());

    s = emittedState(12);
    assertEquals(LightState.RED, s.getNs());
    assertEquals(LightState.ORANGE, s.getEw());
  }

  @Test
  void phase_offset_shifts_the_cycle() {
    // greenDuration=5, orangeDuration=2, phaseOffset=3 -> phase=time+3
    final CrossingTrafficLight light = new CrossingTrafficLight(engine, 1, 2, 5, 2, 3);
    light.process(nextStatesEvent(light, 0));
    final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
    verify(engine).signal(captor.capture());
    final TrafficLightState s = (TrafficLightState) captor.getValue().getParameters()[0];
    // phase = 0+3 = 3 < halfCycle(7); ns = 3 < green(5) -> GREEN
    assertEquals(LightState.GREEN, s.getNs());
    assertEquals(LightState.RED, s.getEw());
  }

  @Test
  void emitted_state_carries_intersection_position() {
    final TrafficLightState s = emittedState(0);
    assertEquals(1, s.getRow());
    assertEquals(2, s.getCol());
  }

  @Test
  void ignores_unrelated_topics() {
    final CrossingTrafficLight light = new CrossingTrafficLight(engine, 1, 2, 5, 2, 0);
    final Event other = EventImpl.createEvent(Topics.NEXT_VEHICLES_STATES, light, 3);
    light.process(other);
    verify(engine, never()).signal(any(Event.class));
  }
}
