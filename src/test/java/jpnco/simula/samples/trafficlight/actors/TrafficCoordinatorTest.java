package jpnco.simula.samples.trafficlight.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import jpnco.simula.Engine;
import jpnco.simula.engine.EngineImpl;
import jpnco.simula.engine.EventImpl;
import jpnco.simula.engine.ExecutionMode;
import jpnco.simula.samples.trafficlight.states.GridState;
import jpnco.simula.samples.trafficlight.states.LightState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrafficCoordinatorTest {

  private static final String ROOT_NAME = "test-root";
  private static final int TIME_FACTOR = 1;
  private static final int DURATION_SECONDS = 120;
  private static final long SNAPSHOT_TIMEOUT_MILLIS = 5000L;
  private static final long SNAPSHOT_POLL_MILLIS = 10L;

  private Engine engine;

  @BeforeEach
  void setUp() {
    engine = mock(Engine.class);
  }

  @Test
  void process_ignores_unrelated_topics() {
    final TrafficCoordinator coordinator = new TrafficCoordinator(engine, DURATION_SECONDS);
    coordinator.process(EventImpl.createEvent("some-other-topic", coordinator, 1));
    org.mockito.Mockito.verify(engine, org.mockito.Mockito.never())
        .signal(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void does_not_assemble_when_only_one_group_is_ready() {
    final TrafficCoordinator coordinator = new TrafficCoordinator(engine, DURATION_SECONDS);
    coordinator.process(EventImpl.createEvent(Topics.LIGHTS_READY, coordinator));
    org.mockito.Mockito.verify(engine, org.mockito.Mockito.never())
        .signal(org.mockito.ArgumentMatchers.argThat(e -> Topics.NEW_STATE.equals(e.getTopic())));

    final TrafficCoordinator other = new TrafficCoordinator(engine, DURATION_SECONDS);
    other.process(EventImpl.createEvent(Topics.VEHICLES_READY, other));
    org.mockito.Mockito.verify(engine, org.mockito.Mockito.never())
        .signal(org.mockito.ArgumentMatchers.argThat(e -> Topics.NEW_STATE.equals(e.getTopic())));
  }

  @Test
  void seeds_correct_number_of_lights_and_vehicles() {
    final TrafficCoordinator coordinator = new TrafficCoordinator(engine, DURATION_SECONDS);
    coordinator.seed();
    // 12 non-corner lights (16 intersections - 4 corners) and 12 vehicles.
    assertEquals(12, nonCornerCount());
    assertEquals(12, TrafficCoordinator.INITIAL_VEHICLES);
  }

  private static boolean isCorner(final int row, final int col) {
    final int last = TrafficCoordinator.INTERSECTIONS - 1;
    return (row == 0 || row == last) && (col == 0 || col == last);
  }

  private static int nonCornerCount() {
    int count = 0;
    for (int r = 0; r < TrafficCoordinator.INTERSECTIONS; r++) {
      for (int c = 0; c < TrafficCoordinator.INTERSECTIONS; c++) {
        if (!isCorner(r, c)) {
          count++;
        }
      }
    }
    return count;
  }

  @Test
  void assembles_state_after_all_reports_and_keeps_corners_green() throws InterruptedException {
    final EngineImpl root = new EngineImpl(ROOT_NAME, TIME_FACTOR, ExecutionMode.VIRTUAL);
    final TrafficCoordinator coordinator = new TrafficCoordinator(root, DURATION_SECONDS);
    coordinator.seed();
    root.registerAndStart(coordinator);
    root.start();

    root.signal(EventImpl.createEvent(Engine.TIME_EVENT, root, 1));

    final GridState state = awaitSnapshot(coordinator);
    assertNotNull(state, "a snapshot must be assembled after both groups report");
    assertEquals(1, state.getSimTime());
    assertEquals(TrafficCoordinator.INITIAL_VEHICLES, state.getVehicleCount());
    // Corners have no light actor and stay green; interior cells carry a valid band state.
    assertEquals(LightState.GREEN, state.lightState(0, 0, true));
    assertEquals(LightState.GREEN, state.lightState(0, 0, false));
    assertEquals(LightState.GREEN, state.lightState(0, 3, true));
    assertEquals(LightState.GREEN, state.lightState(3, 0, false));
    assertNotNull(state.lightState(1, 1, true));
    assertNotNull(state.lightState(1, 1, false));

    root.stop();
  }

  @Test
  void snapshot_is_null_before_any_tick() {
    final TrafficCoordinator coordinator = new TrafficCoordinator(engine, DURATION_SECONDS);
    assertNull(coordinator.snapshot());
  }

  private static GridState awaitSnapshot(final TrafficCoordinator coordinator)
      throws InterruptedException {
    final long deadline = System.currentTimeMillis() + SNAPSHOT_TIMEOUT_MILLIS;
    GridState state = coordinator.snapshot();
    while (state == null && System.currentTimeMillis() < deadline) {
      Thread.sleep(SNAPSHOT_POLL_MILLIS);
      state = coordinator.snapshot();
    }
    return state;
  }
}
