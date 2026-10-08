package fr.jpnco.simula.samples.trafficlight.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GridStateTest {

  private static VehicleView view(final int id) {
    return new VehicleView(id, 1, 2, Direction.EAST, 10.0);
  }

  private static LightState[][] nsTable() {
    return new LightState[][] {
      {LightState.GREEN, LightState.RED}, {LightState.ORANGE, LightState.GREEN}
    };
  }

  private static LightState[][] ewTable() {
    return new LightState[][] {
      {LightState.RED, LightState.GREEN}, {LightState.GREEN, LightState.ORANGE}
    };
  }

  private static int[][] crossings() {
    return new int[][] {{1, 2}, {3, 4}};
  }

  private static GridState state() {
    return new GridState(5, List.of(view(1), view(2)), nsTable(), ewTable(), crossings());
  }

  @Test
  void exposes_sim_time_and_vehicle_count() {
    final GridState s = state();
    assertEquals(5, s.getSimTime());
    assertEquals(2, s.getVehicleCount());
  }

  @Test
  void vehicles_are_unmodifiable() {
    final GridState s = state();
    assertThrows(UnsupportedOperationException.class, () -> s.getVehicles().clear());
    assertEquals(2, s.getVehicles().size());
  }

  @Test
  void vehicles_list_is_defensively_copied() {
    final List<VehicleView> input = new ArrayList<>(List.of(view(1)));
    final GridState s = new GridState(1, input, nsTable(), ewTable(), crossings());
    input.add(view(99));
    assertEquals(1, s.getVehicles().size());
  }

  @Test
  void north_south_green_query_reflects_ns_band() {
    final GridState s = state();
    assertTrue(s.isNorthSouthGreen(0, 0));
    assertFalse(s.isNorthSouthGreen(0, 1));
    assertFalse(s.isNorthSouthGreen(1, 0));
    assertTrue(s.isNorthSouthGreen(1, 1));
  }

  @Test
  void lightState_selects_band_by_vertical_flag() {
    final GridState s = state();
    assertSame(LightState.GREEN, s.lightState(0, 0, true));
    assertSame(LightState.RED, s.lightState(0, 0, false));
    assertSame(LightState.RED, s.lightState(0, 1, true));
    assertSame(LightState.GREEN, s.lightState(0, 1, false));
  }

  @Test
  void crossingsAt_returns_per_cell_count() {
    final GridState s = state();
    assertEquals(1, s.crossingsAt(0, 0));
    assertEquals(4, s.crossingsAt(1, 1));
  }

  @Test
  void constructor_copies_arrays_to_preserve_immutability() {
    final LightState[][] ns = nsTable();
    final int[][] cross = crossings();
    final GridState s = new GridState(1, List.of(view(1)), ns, ewTable(), cross);
    ns[0][0] = LightState.RED;
    cross[0][0] = 99;
    assertSame(LightState.GREEN, s.lightState(0, 0, true));
    assertEquals(1, s.crossingsAt(0, 0));
  }
}
