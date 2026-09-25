package jpnco.simula.samples.trafficlight.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GridGeometryTest {

  @Test
  void computes_metrics_for_standard_panel_size() {
    // width=1080, height=900: available=880, cell=283, ppm=2.83, lane=28,
    // extent=877, offsetX=115, offsetY=25, marking=1, diameter=6, radius=2, lateral=7.0
    final GridGeometry geo = GridGeometry.of(1080, 900);
    assertEquals(283, geo.cell());
    assertEquals(2.83, geo.pixelsPerMetre());
    assertEquals(28, geo.lane());
    assertEquals(877, geo.extent());
    assertEquals(115, geo.offsetX());
    assertEquals(25, geo.offsetY());
    assertEquals(1, geo.marking());
    assertEquals(6, geo.vehicleDiameter());
    assertEquals(2, geo.lightRadius());
    assertEquals(7.0, geo.lateral());
  }

  @Test
  void clamps_lane_and_vehicle_diameter_to_minimums() {
    final GridGeometry geo = GridGeometry.of(40, 40);
    assertEquals(4, geo.lane());
    assertEquals(2, geo.vehicleDiameter());
    assertEquals(1, geo.marking());
  }

  @Test
  void geometry_is_deterministic_for_same_size() {
    final GridGeometry a = GridGeometry.of(600, 600);
    final GridGeometry b = GridGeometry.of(600, 600);
    assertEquals(a.cell(), b.cell());
    assertEquals(a.offsetX(), b.offsetX());
    assertEquals(a.offsetY(), b.offsetY());
    assertEquals(a.lane(), b.lane());
  }

  @Test
  void offset_centers_grid_on_larger_dimension() {
    final GridGeometry geo = GridGeometry.of(1200, 600);
    // width is the larger dimension; available is driven by the smaller (height).
    final int available = 600 - 2 * 10;
    final int cell = (int) (available / (3.0 + 10.0 / 100.0));
    final double ppm = (double) cell / 100.0;
    final int lane = Math.max(4, (int) Math.round(10.0 * ppm));
    final int extent = cell * 3 + lane;
    assertEquals(cell, geo.cell());
    assertEquals(lane, geo.lane());
    assertEquals(extent, geo.extent());
    assertEquals((1200 - extent) / 2 + lane / 2, geo.offsetX());
    assertEquals((600 - extent) / 2 + lane / 2, geo.offsetY());
  }
}
