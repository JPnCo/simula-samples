package fr.jpnco.simula.samples.trafficlight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.jpnco.simula.engine.ExecutionMode;
import org.junit.jupiter.api.Test;

class TrafficLightCliTest {

  @Test
  void defaults_to_virtual_and_console() {
    assertEquals(ExecutionMode.VIRTUAL, TrafficLightCli.resolveMode(new String[] {}));
    assertEquals(
        TrafficLightCli.DisplayChoice.CONSOLE, TrafficLightCli.resolveDisplay(new String[] {}));
  }

  @Test
  void classic_selects_platform() {
    assertEquals(ExecutionMode.PLATFORM, TrafficLightCli.resolveMode(new String[] {"classic"}));
    assertEquals(ExecutionMode.PLATFORM, TrafficLightCli.resolveMode(new String[] {"CLASSIC"}));
  }

  @Test
  void gui_selects_gui_display() {
    assertEquals(
        TrafficLightCli.DisplayChoice.GUI, TrafficLightCli.resolveDisplay(new String[] {"gui"}));
    assertEquals(
        TrafficLightCli.DisplayChoice.GUI, TrafficLightCli.resolveDisplay(new String[] {"GUI"}));
  }

  @Test
  void unknown_tokens_are_ignored() {
    assertEquals(
        ExecutionMode.VIRTUAL, TrafficLightCli.resolveMode(new String[] {"bogus", "other"}));
    assertEquals(
        TrafficLightCli.DisplayChoice.CONSOLE,
        TrafficLightCli.resolveDisplay(new String[] {"bogus"}));
  }

  @Test
  void mode_and_display_resolve_independently() {
    assertEquals(
        ExecutionMode.PLATFORM, TrafficLightCli.resolveMode(new String[] {"gui", "classic"}));
    assertEquals(
        TrafficLightCli.DisplayChoice.GUI,
        TrafficLightCli.resolveDisplay(new String[] {"gui", "classic"}));
  }
}
