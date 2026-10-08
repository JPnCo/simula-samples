package fr.jpnco.simula.samples.dashboard.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.jpnco.simula.samples.boids.states.BoidView;
import fr.jpnco.simula.samples.boids.states.FlockState;
import fr.jpnco.simula.samples.trafficlight.states.Direction;
import fr.jpnco.simula.samples.trafficlight.states.GridState;
import fr.jpnco.simula.samples.trafficlight.states.LightState;
import fr.jpnco.simula.samples.trafficlight.states.VehicleView;
import java.util.List;
import org.junit.jupiter.api.Test;

class MetricExtractorTest {

  private static GridState gridState() {
    final List<VehicleView> vehicles =
        List.of(
            new VehicleView(0, 1, 1, Direction.NORTH, 10.0),
            new VehicleView(1, 1, 2, Direction.EAST, 20.0),
            new VehicleView(2, 2, 2, Direction.SOUTH, 30.0));
    final LightState[][] northSouth = {
      {LightState.GREEN, LightState.RED, LightState.GREEN},
      {LightState.RED, LightState.GREEN, LightState.RED},
      {LightState.GREEN, LightState.RED, LightState.GREEN}
    };
    final LightState[][] eastWest = {
      {LightState.RED, LightState.GREEN, LightState.RED},
      {LightState.GREEN, LightState.RED, LightState.GREEN},
      {LightState.RED, LightState.GREEN, LightState.RED}
    };
    return new GridState(4, vehicles, northSouth, eastWest, new int[3][3]);
  }

  private static FlockState flockState() {
    return new FlockState(
        5,
        List.of(new BoidView(0, 10.0, 20.0, 1.0, 1.0), new BoidView(1, 30.0, 40.0, -1.0, 0.5)),
        800.0,
        600.0);
  }

  @Test
  void extracts_vehicle_count_from_grid_state() {
    final Sample sample = MetricExtractor.toSample(gridState());
    assertEquals(4, sample.getSimTime());
    assertEquals(3, sample.getValue());
  }

  @Test
  void extracts_boid_count_from_flock_state() {
    final Sample sample = MetricExtractor.toSample(flockState());
    assertEquals(5, sample.getSimTime());
    assertEquals(2, sample.getValue());
  }

  @Test
  void rejects_an_unsupported_snapshot_type() {
    assertThrows(IllegalArgumentException.class, () -> MetricExtractor.toSample("not-a-snapshot"));
  }
}
