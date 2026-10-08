package fr.jpnco.simula.samples.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import fr.jpnco.simula.engine.ExecutionMode;
import fr.jpnco.simula.samples.dashboard.DashboardParameters.Display;
import fr.jpnco.simula.samples.dashboard.DashboardParameters.Target;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DashboardCliTest {

  @Test
  void default_target_is_boids() {
    assertEquals(Target.BOIDS, DashboardCli.resolveTarget(new String[] {}));
  }

  @Test
  void trafficlight_argument_selects_trafficlight_case_insensitive() {
    assertEquals(Target.TRAFFICLIGHT, DashboardCli.resolveTarget(new String[] {"TrafficLight"}));
  }

  @Test
  void classic_argument_selects_platform_mode() {
    assertEquals(ExecutionMode.PLATFORM, DashboardCli.resolveMode(new String[] {"classic"}));
  }

  @Test
  void default_mode_is_virtual() {
    assertEquals(ExecutionMode.VIRTUAL, DashboardCli.resolveMode(new String[] {}));
  }

  @Test
  void gui_argument_selects_gui_display() {
    assertEquals(Display.GUI, DashboardCli.resolveDisplay(new String[] {"gui"}));
  }

  @Test
  void default_display_is_console() {
    assertEquals(Display.CONSOLE, DashboardCli.resolveDisplay(new String[] {}));
  }

  @Test
  void export_argument_resolves_path() {
    assertEquals(
        Path.of("series.csv"), DashboardCli.resolveExportPath(new String[] {"export=series.csv"}));
  }

  @Test
  void missing_export_resolves_to_null() {
    assertNull(DashboardCli.resolveExportPath(new String[] {}));
  }

  @Test
  void resolveParameters_combines_tokens_and_defaults_rest() {
    final DashboardParameters params =
        DashboardCli.resolveParameters(new String[] {"trafficlight", "classic", "export=out.csv"});
    assertEquals(Target.TRAFFICLIGHT, params.getTarget());
    assertEquals(ExecutionMode.PLATFORM, params.getMode());
    assertEquals(Display.CONSOLE, params.getDisplay());
    assertEquals(Path.of("out.csv"), params.getExportPath());
  }

  @Test
  void resolveParameters_ignores_unknown_tokens() {
    final DashboardParameters params =
        DashboardCli.resolveParameters(new String[] {"bogus", "123"});
    assertEquals(DashboardParameters.DEFAULT.getTarget(), params.getTarget());
    assertEquals(DashboardParameters.DEFAULT.getMode(), params.getMode());
    assertEquals(DashboardParameters.DEFAULT.getDisplay(), params.getDisplay());
    assertNull(params.getExportPath());
  }
}
