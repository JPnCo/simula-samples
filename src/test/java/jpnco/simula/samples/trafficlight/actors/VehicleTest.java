package jpnco.simula.samples.trafficlight.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import jpnco.simula.Engine;
import jpnco.simula.Event;
import jpnco.simula.engine.EventImpl;
import jpnco.simula.samples.trafficlight.states.Direction;
import jpnco.simula.samples.trafficlight.states.LightState;
import jpnco.simula.samples.trafficlight.states.VehicleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class VehicleTest {

  private Engine engine;

  @BeforeEach
  void setUp() {
    engine = mock(Engine.class);
  }

  private static LightState[][][] greenTable() {
    final LightState[][][] table = new LightState[4][4][2];
    for (int r = 0; r < 4; r++) {
      for (int c = 0; c < 4; c++) {
        table[r][c][0] = LightState.GREEN;
        table[r][c][1] = LightState.GREEN;
      }
    }
    return table;
  }

  private static Event nextVehiclesEvent(
      final Vehicle vehicle, final int tick, final LightState[][][] lights) {
    return EventImpl.createEvent(Topics.NEXT_VEHICLES_STATES, vehicle, tick, lights);
  }

  private void runTicks(final Vehicle vehicle, final LightState[][][] lights, final int count) {
    for (int tick = 1; tick <= count; tick++) {
      vehicle.process(nextVehiclesEvent(vehicle, tick, lights));
    }
  }

  @Test
  void process_ignores_unrelated_topics() {
    final Vehicle vehicle = new Vehicle(engine, 0, 1, 1, Direction.EAST, 10.0, 1L);
    final Event other = EventImpl.createEvent("some-other-topic", vehicle, 1);
    vehicle.process(other);
    assertEquals(0.0, vehicle.getDistanceInSegment());
  }

  @Test
  void committed_vehicle_advances_past_a_red_light() {
    // speed 12 m/s: after 8 ticks the vehicle reaches 96 m, past the stop line (95 m);
    // it is then committed, so on the next tick it advances even if the light turns red.
    final Vehicle vehicle = new Vehicle(engine, 0, 1, 1, Direction.EAST, 12.0, 1L);
    runTicks(vehicle, greenTable(), 8);
    assertEquals(TrafficCoordinator.LIGHT_POSITION + 1.0, vehicle.getDistanceInSegment());
    final LightState[][][] red = greenTable();
    red[1][2][1] = LightState.RED;
    vehicle.process(nextVehiclesEvent(vehicle, 9, red));
    assertEquals(2, vehicle.getCol());
  }

  @Test
  void vehicle_reaching_bottom_edge_turns_to_stay_on_grid() {
    // starting at row 2 heading south, the destination (3,1) is on grid; once it is reached
    // the vehicle must turn right or left rather than continue off the bottom edge.
    final Vehicle vehicle = new Vehicle(engine, 0, 2, 1, Direction.SOUTH, 10.0, 1L);
    runTicks(vehicle, greenTable(), 12);
    assertTrue(vehicle.getRow() >= 0 && vehicle.getRow() < TrafficCoordinator.INTERSECTIONS);
    assertTrue(vehicle.getCol() >= 0 && vehicle.getCol() < TrafficCoordinator.INTERSECTIONS);
  }

  @Test
  void vehicle_reaching_top_edge_turns_to_stay_on_grid() {
    final Vehicle vehicle = new Vehicle(engine, 0, 1, 2, Direction.NORTH, 10.0, 1L);
    runTicks(vehicle, greenTable(), 12);
    assertTrue(vehicle.getRow() >= 0 && vehicle.getRow() < TrafficCoordinator.INTERSECTIONS);
    assertTrue(vehicle.getCol() >= 0 && vehicle.getCol() < TrafficCoordinator.INTERSECTIONS);
  }

  @Test
  void advances_distance_without_crossing() {
    final Vehicle vehicle = new Vehicle(engine, 0, 1, 1, Direction.EAST, 10.0, 1L);
    final LightState[][][] lights = greenTable();
    runTicks(vehicle, lights, 1);
    assertEquals(10.0, vehicle.getDistanceInSegment());
    assertEquals(1, vehicle.getRow());
    assertEquals(1, vehicle.getCol());
    assertEquals(Direction.EAST, vehicle.getDirection());
  }

  @Test
  void stops_at_light_when_band_is_red() {
    final Vehicle vehicle = new Vehicle(engine, 0, 1, 1, Direction.EAST, 10.0, 1L);
    final LightState[][][] lights = greenTable();
    lights[1][2][1] = LightState.RED;
    runTicks(vehicle, lights, 12);
    assertEquals(TrafficCoordinator.LIGHT_POSITION, vehicle.getDistanceInSegment());
    assertEquals(1, vehicle.getRow());
    assertEquals(1, vehicle.getCol());
    assertEquals(Direction.EAST, vehicle.getDirection());
  }

  @Test
  void crosses_intersection_when_band_is_green() {
    final Vehicle vehicle = new Vehicle(engine, 0, 1, 1, Direction.EAST, 10.0, 1L);
    final LightState[][][] lights = greenTable();
    runTicks(vehicle, lights, 12);
    assertEquals(1, vehicle.getRow());
    assertEquals(2, vehicle.getCol());
  }

  @Test
  void edge_vehicle_is_forced_to_turn_not_leave_grid() {
    final Vehicle vehicle = new Vehicle(engine, 0, 1, 2, Direction.EAST, 10.0, 1L);
    final LightState[][][] lights = greenTable();
    runTicks(vehicle, lights, 12);
    assertEquals(1, vehicle.getRow());
    assertEquals(3, vehicle.getCol());
    assertEquals(Direction.SOUTH, vehicle.getDirection());
  }

  @Test
  void entered_cells_are_broadcast_on_crossing() {
    final Vehicle vehicle = new Vehicle(engine, 0, 1, 1, Direction.EAST, 10.0, 1L);
    final LightState[][][] lights = greenTable();
    runTicks(vehicle, lights, 12);
    final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
    org.mockito.Mockito.verify(engine, org.mockito.Mockito.atLeastOnce()).signal(captor.capture());
    boolean crossedReported = false;
    for (final Event e : captor.getAllValues()) {
      final VehicleState s = (VehicleState) e.getParameters()[0];
      for (final int[] cell : s.getEnteredCells()) {
        if (cell[0] == 1 && cell[1] == 2) {
          crossedReported = true;
        }
      }
    }
    assertTrue(crossedReported, "expected cell (1,2) to be reported as entered");
  }

  @Test
  void interior_vehicle_covers_random_turn_branches_across_seeds() {
    boolean turned = false;
    for (long seed = 4000; seed <= 5000; seed++) {
      final Vehicle vehicle = new Vehicle(engine, 0, 1, 1, Direction.EAST, 10.0, seed);
      runTicks(vehicle, greenTable(), 12);
      final Direction d = vehicle.getDirection();
      assertTrue(
          d == Direction.EAST || d == Direction.SOUTH || d == Direction.NORTH,
          "unexpected direction " + d);
      if (d != Direction.EAST) {
        turned = true;
      }
    }
    assertTrue(turned, "expected at least one seed to make the vehicle turn");
  }

  @Test
  void edge_turn_that_would_leave_grid_falls_back_to_straight() {
    boolean turnedAway = false;
    boolean stayedStraight = false;
    for (long seed = 4000; seed <= 5000; seed++) {
      final Vehicle vehicle = new Vehicle(engine, 0, 0, 1, Direction.EAST, 10.0, seed);
      runTicks(vehicle, greenTable(), 12);
      final Direction d = vehicle.getDirection();
      assertTrue(d == Direction.EAST || d == Direction.SOUTH, "unexpected direction " + d);
      if (d != Direction.EAST) {
        turnedAway = true;
      } else {
        stayedStraight = true;
      }
    }
    assertTrue(turnedAway, "expected at least one seed to turn");
    assertTrue(stayedStraight, "expected at least one seed to fall back straight");
  }

  @Test
  void corner_vehicle_is_forced_to_turn_left() {
    final Vehicle vehicle = new Vehicle(engine, 0, 1, 3, Direction.NORTH, 10.0, 1L);
    runTicks(vehicle, greenTable(), 12);
    assertEquals(Direction.WEST, vehicle.getDirection());
  }
}
