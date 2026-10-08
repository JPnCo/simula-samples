package fr.jpnco.simula.samples.dashboard.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.engine.EventImpl;
import fr.jpnco.simula.samples.boids.states.BoidView;
import fr.jpnco.simula.samples.boids.states.FlockState;
import fr.jpnco.simula.samples.trafficlight.states.Direction;
import fr.jpnco.simula.samples.trafficlight.states.GridState;
import fr.jpnco.simula.samples.trafficlight.states.LightState;
import fr.jpnco.simula.samples.trafficlight.states.VehicleView;
import java.util.List;
import org.junit.jupiter.api.Test;

class DashboardTest {

  private static FlockState flockState(final int simTime, final int boids) {
    final BoidView view = new BoidView(0, 10.0, 20.0, 1.0, 1.0);
    final List<BoidView> views = new java.util.ArrayList<>();
    for (int i = 0; i < boids; i++) {
      views.add(view);
    }
    return new FlockState(simTime, views, 800.0, 600.0);
  }

  private static GridState gridState(final int simTime, final int vehicles) {
    final VehicleView view = new VehicleView(0, 1, 1, Direction.NORTH, 10.0);
    final List<VehicleView> views = new java.util.ArrayList<>();
    for (int i = 0; i < vehicles; i++) {
      views.add(view);
    }
    return new GridState(simTime, views, new LightState[3][3], new LightState[3][3], new int[3][3]);
  }

  @Test
  void subscribes_to_the_new_state_topic() {
    final Engine engine = mock(Engine.class);
    new Dashboard(engine, Topics.NEW_STATE);
    verify(engine).subscribe(any(), eq(Topics.NEW_STATE));
  }

  @Test
  void records_one_sample_per_new_state_event() {
    final Engine engine = mock(Engine.class);
    final Dashboard dashboard = new Dashboard(engine, Topics.NEW_STATE);
    assertTrue(dashboard.snapshot().isEmpty());

    dashboard.process(EventImpl.createEvent(Topics.NEW_STATE, dashboard, flockState(1, 4)));
    dashboard.process(EventImpl.createEvent(Topics.NEW_STATE, dashboard, flockState(2, 5)));

    assertEquals(2, dashboard.snapshot().size());
    assertEquals(4.0, dashboard.snapshot().first().getValue());
    assertEquals(2, dashboard.snapshot().last().getSimTime());
    assertEquals(5.0, dashboard.snapshot().last().getValue());
  }

  @Test
  void records_vehicle_count_from_grid_state_payload() {
    final Engine engine = mock(Engine.class);
    final Dashboard dashboard = new Dashboard(engine, Topics.NEW_STATE);
    dashboard.process(EventImpl.createEvent(Topics.NEW_STATE, dashboard, gridState(3, 7)));
    assertEquals(1, dashboard.snapshot().size());
    assertEquals(3, dashboard.snapshot().first().getSimTime());
    assertEquals(7.0, dashboard.snapshot().first().getValue());
  }

  @Test
  void ignores_unrelated_topics() {
    final Engine engine = mock(Engine.class);
    final Dashboard dashboard = new Dashboard(engine, Topics.NEW_STATE);
    final Event other = EventImpl.createEvent("some-other-topic", dashboard, 1);
    dashboard.process(other);
    assertTrue(dashboard.snapshot().isEmpty());
  }
}
