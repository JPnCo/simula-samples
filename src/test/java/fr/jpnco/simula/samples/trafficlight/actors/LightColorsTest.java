package fr.jpnco.simula.samples.trafficlight.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.jpnco.simula.samples.trafficlight.states.LightState;
import org.junit.jupiter.api.Test;

class LightColorsTest {

  @Test
  void green_maps_to_green_color() {
    assertEquals(34, LightColors.of(LightState.GREEN).getRed());
    assertEquals(139, LightColors.of(LightState.GREEN).getGreen());
    assertEquals(34, LightColors.of(LightState.GREEN).getBlue());
  }

  @Test
  void orange_maps_to_orange_color() {
    assertEquals(255, LightColors.of(LightState.ORANGE).getRed());
    assertEquals(140, LightColors.of(LightState.ORANGE).getGreen());
    assertEquals(0, LightColors.of(LightState.ORANGE).getBlue());
  }

  @Test
  void red_maps_to_red_color() {
    assertEquals(220, LightColors.of(LightState.RED).getRed());
    assertEquals(40, LightColors.of(LightState.RED).getGreen());
    assertEquals(40, LightColors.of(LightState.RED).getBlue());
  }

  @Test
  void scene_colors_are_exposed_as_constants() {
    assertEquals(24, LightColors.GROUND.getRed());
    assertEquals(70, LightColors.ROAD.getRed());
    assertEquals(200, LightColors.LANE_MARKING.getRed());
    assertEquals(255, LightColors.VEHICLE.getRed());
  }
}
