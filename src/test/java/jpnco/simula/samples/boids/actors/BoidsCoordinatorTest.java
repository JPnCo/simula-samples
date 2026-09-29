package jpnco.simula.samples.boids.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import jpnco.simula.Engine;
import jpnco.simula.engine.EngineImpl;
import jpnco.simula.engine.EventImpl;
import jpnco.simula.engine.ExecutionMode;
import jpnco.simula.samples.boids.FlockParameters;
import jpnco.simula.samples.boids.states.FlockState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BoidsCoordinatorTest {

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
    final BoidsCoordinator coordinator =
        new BoidsCoordinator(engine, FlockParameters.DEFAULT, DURATION_SECONDS);
    coordinator.process(EventImpl.createEvent("some-other-topic", coordinator, 1));
    org.mockito.Mockito.verify(engine, org.mockito.Mockito.never())
        .signal(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void does_not_assemble_before_boids_ready() {
    final BoidsCoordinator coordinator =
        new BoidsCoordinator(engine, FlockParameters.DEFAULT, DURATION_SECONDS);
    coordinator.process(EventImpl.createEvent(Topics.NEXT_BOID_STATES, coordinator, 1));
    org.mockito.Mockito.verify(engine, org.mockito.Mockito.never())
        .signal(org.mockito.ArgumentMatchers.argThat(e -> Topics.NEW_STATE.equals(e.getTopic())));
  }

  @Test
  void snapshot_is_null_before_any_tick() {
    final BoidsCoordinator coordinator =
        new BoidsCoordinator(engine, FlockParameters.DEFAULT, DURATION_SECONDS);
    assertNull(coordinator.snapshot());
  }

  @Test
  void seed_creates_the_configured_number_of_boids() {
    final FlockParameters params = new FlockParameters(10, 1.0, 1.0, 1.0, 40.0, 4.0, 800.0, 600.0);
    final BoidsCoordinator coordinator = new BoidsCoordinator(engine, params, DURATION_SECONDS);
    coordinator.seed();
    assertEquals(10, coordinator.getBoidCount());
  }

  @Test
  void assembles_state_after_boids_ready_and_tracks_distance() throws InterruptedException {
    final EngineImpl root = new EngineImpl(ROOT_NAME, TIME_FACTOR, ExecutionMode.VIRTUAL);
    final FlockParameters params = new FlockParameters(8, 1.0, 1.0, 1.0, 40.0, 4.0, 800.0, 600.0);
    final BoidsCoordinator coordinator = new BoidsCoordinator(root, params, DURATION_SECONDS);
    coordinator.seed();
    root.registerAndStart(coordinator);
    root.start();

    root.signal(EventImpl.createEvent(Engine.TIME_EVENT, root, 1));

    final FlockState state = awaitSnapshot(coordinator);
    assertNotNull(state, "a snapshot must be assembled after the boids report");
    assertEquals(1, state.getSimTime());
    assertEquals(8, state.getBoidCount());
    assertEquals(8, state.getBoids().size());
    assertNotNull(coordinator.getTotalDistance());

    root.stop();
  }

  @Test
  void updateParameters_propagates_the_new_values_to_every_boid() throws InterruptedException {
    final EngineImpl root = new EngineImpl(ROOT_NAME, TIME_FACTOR, ExecutionMode.VIRTUAL);
    final FlockParameters params = new FlockParameters(4, 1.0, 1.0, 1.0, 40.0, 4.0, 800.0, 600.0);
    final BoidsCoordinator coordinator = new BoidsCoordinator(root, params, DURATION_SECONDS);
    coordinator.seed();
    root.registerAndStart(coordinator);
    root.start();

    final FlockParameters updated = new FlockParameters(4, 1.0, 1.0, 1.0, 40.0, 0.5, 800.0, 600.0);
    coordinator.updateParameters(updated);
    root.signal(EventImpl.createEvent(Engine.TIME_EVENT, root, 1));

    final FlockState state = awaitSnapshot(coordinator);
    assertNotNull(state, "a snapshot must be assembled after the boids report");
    for (final jpnco.simula.samples.boids.states.BoidView boid : state.getBoids()) {
      final double speed = Math.hypot(boid.getVx(), boid.getVy());
      assertTrue(
          speed <= 0.5 + 1e-9, "the lowered max speed must cap every boid velocity, was " + speed);
    }

    root.stop();
  }

  private static FlockState awaitSnapshot(final BoidsCoordinator coordinator)
      throws InterruptedException {
    final long deadline = System.currentTimeMillis() + SNAPSHOT_TIMEOUT_MILLIS;
    FlockState state = coordinator.snapshot();
    while (state == null && System.currentTimeMillis() < deadline) {
      Thread.sleep(SNAPSHOT_POLL_MILLIS);
      state = coordinator.snapshot();
    }
    return state;
  }
}
