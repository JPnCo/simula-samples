package jpnco.simula.samples.boids.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import jpnco.simula.Engine;
import jpnco.simula.Event;
import jpnco.simula.engine.EventImpl;
import jpnco.simula.samples.boids.states.BoidState;
import jpnco.simula.samples.boids.states.BoidView;
import jpnco.simula.samples.boids.states.FlockState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BoidTest {

  private static final double WORLD_WIDTH = 800.0;
  private static final double WORLD_HEIGHT = 600.0;

  private Engine engine;

  @BeforeEach
  void setUp() {
    engine = mock(Engine.class);
  }

  private static FlockState emptyFlock(final int tick) {
    return new FlockState(tick, List.of(), WORLD_WIDTH, WORLD_HEIGHT);
  }

  private static Event nextEvent(final Boid boid, final int tick, final FlockState previous) {
    return EventImpl.createEvent(Topics.NEXT_BOID_STATES, boid, tick, previous);
  }

  @Test
  void process_ignores_unrelated_topics() {
    final Boid boid = new Boid(engine, 0, 10.0, 10.0, 1.0, 0.0, 40.0, 4.0);
    final Event other = EventImpl.createEvent("some-other-topic", boid, 1);
    boid.process(other);
    assertNull(boid.getLastState());
  }

  @Test
  void coasts_and_advances_when_there_are_no_neighbors() {
    final Boid boid = new Boid(engine, 0, 10.0, 10.0, 3.0, 1.0, 40.0, 4.0);
    boid.process(nextEvent(boid, 1, emptyFlock(0)));
    final BoidState state = boid.getLastState();
    assertEquals(1, state.getTick());
    assertEquals(3.0, state.getVx());
    assertEquals(1.0, state.getVy());
    assertEquals(13.0, state.getX());
    assertEquals(11.0, state.getY());
  }

  @Test
  void wraps_around_when_it_leaves_the_world() {
    final Boid boid = new Boid(engine, 0, WORLD_WIDTH - 1.0, 10.0, 3.0, 0.0, 40.0, 4.0);
    boid.process(nextEvent(boid, 1, emptyFlock(0)));
    final BoidState state = boid.getLastState();
    assertTrue(state.getX() >= 0.0 && state.getX() < WORLD_WIDTH);
  }

  @Test
  void stores_and_broadcasts_its_state() {
    final Boid boid = new Boid(engine, 0, 10.0, 10.0, 3.0, 1.0, 40.0, 4.0);
    boid.process(nextEvent(boid, 1, emptyFlock(0)));
    org.mockito.Mockito.verify(engine, org.mockito.Mockito.atLeastOnce())
        .signal(org.mockito.ArgumentMatchers.argThat(e -> Topics.BOID_STATE.equals(e.getTopic())));
  }

  @Test
  void flocking_steers_toward_a_right_side_neighbor() {
    // The previous flock holds one neighbor to the right of this boid.
    final FlockState previous =
        new FlockState(
            0, List.of(new BoidView(99, 60.0, 10.0, 0.0, 0.0)), WORLD_WIDTH, WORLD_HEIGHT);
    final Boid boid = new Boid(engine, 0, 10.0, 10.0, 0.0, 0.0, 40.0, 4.0);
    boid.process(nextEvent(boid, 1, previous));
    final BoidState state = boid.getLastState();
    assertTrue(state.getVx() > 0.0, "a right-side neighbor must steer the boid right");
  }

  @Test
  void exposes_its_position_and_velocity() {
    final Boid boid = new Boid(engine, 3, 12.0, 14.0, 2.0, -1.0, 40.0, 4.0);
    assertEquals(3, boid.getId());
    assertEquals(12.0, boid.getX());
    assertEquals(14.0, boid.getY());
    assertEquals(2.0, boid.getVx());
    assertEquals(-1.0, boid.getVy());
    assertEquals(boid.getDelegate(), boid.getDelegate());
  }

  @Test
  void updateParameters_caps_the_velocity_at_the_new_max_speed() {
    final Boid boid = new Boid(engine, 0, 10.0, 10.0, 6.0, 0.0, 40.0, 4.0);
    boid.updateParameters(1.0, 1.0, 1.0, 40.0, 2.0);
    boid.process(nextEvent(boid, 1, emptyFlock(0)));
    final BoidState state = boid.getLastState();
    final double speed = Math.hypot(state.getVx(), state.getVy());
    assertTrue(speed <= 2.0, "the lowered max speed must cap the velocity");
  }
}
