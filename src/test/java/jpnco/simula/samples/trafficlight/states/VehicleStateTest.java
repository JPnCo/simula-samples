package jpnco.simula.samples.trafficlight.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class VehicleStateTest {

  @Test
  void exposes_tick_identity_position_and_distance() {
    final VehicleState s =
        new VehicleState(4, 9, 0, 1, Direction.SOUTH, 33.0, List.of(new int[] {1, 1}));
    assertEquals(4, s.getTick());
    assertEquals(9, s.getId());
    assertEquals(0, s.getRow());
    assertEquals(1, s.getCol());
    assertEquals(Direction.SOUTH, s.getDirection());
    assertEquals(33.0, s.getDistanceInSegment());
  }

  @Test
  void enteredCells_are_defensively_copied_and_unmodifiable() {
    final List<int[]> input = new ArrayList<>(List.of(new int[] {1, 1}));
    final VehicleState s = new VehicleState(4, 9, 0, 1, Direction.SOUTH, 33.0, input);
    input.add(new int[] {2, 2});
    assertEquals(1, s.getEnteredCells().size());
    assertThrows(UnsupportedOperationException.class, () -> s.getEnteredCells().clear());
    assertEquals(1, s.getEnteredCells().get(0)[0]);
    assertEquals(1, s.getEnteredCells().get(0)[1]);
  }
}
