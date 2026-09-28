package jpnco.simula.samples.boids.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class FlockStateTest {

  private static BoidView view(final int id) {
    return new BoidView(id, 1.0, 2.0, 0.0, 0.0);
  }

  private static FlockState state() {
    return new FlockState(5, List.of(view(1), view(2)), 800.0, 600.0);
  }

  @Test
  void exposes_sim_time_world_and_boid_count() {
    final FlockState s = state();
    assertEquals(5, s.getSimTime());
    assertEquals(800.0, s.getWorldWidth());
    assertEquals(600.0, s.getWorldHeight());
    assertEquals(2, s.getBoidCount());
  }

  @Test
  void boids_are_unmodifiable() {
    final FlockState s = state();
    assertThrows(UnsupportedOperationException.class, () -> s.getBoids().clear());
  }

  @Test
  void boids_list_is_defensively_copied() {
    final List<BoidView> input = new ArrayList<>(List.of(view(1)));
    final FlockState s = new FlockState(1, input, 800.0, 600.0);
    input.add(view(99));
    assertEquals(1, s.getBoids().size());
  }
}
