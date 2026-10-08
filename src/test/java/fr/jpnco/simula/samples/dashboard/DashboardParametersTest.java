package fr.jpnco.simula.samples.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import fr.jpnco.simula.engine.ExecutionMode;
import fr.jpnco.simula.samples.dashboard.DashboardParameters.Display;
import fr.jpnco.simula.samples.dashboard.DashboardParameters.Target;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DashboardParametersTest {

  @Test
  void default_parameters_use_boids_virtual_console_without_export() {
    assertEquals(Target.BOIDS, DashboardParameters.DEFAULT.getTarget());
    assertEquals(ExecutionMode.VIRTUAL, DashboardParameters.DEFAULT.getMode());
    assertEquals(Display.CONSOLE, DashboardParameters.DEFAULT.getDisplay());
    assertNull(DashboardParameters.DEFAULT.getExportPath());
  }

  @Test
  void exposes_all_fields() {
    final Path path = Path.of("series.csv");
    final DashboardParameters params =
        new DashboardParameters(Target.TRAFFICLIGHT, ExecutionMode.PLATFORM, Display.GUI, path);
    assertEquals(Target.TRAFFICLIGHT, params.getTarget());
    assertEquals(ExecutionMode.PLATFORM, params.getMode());
    assertEquals(Display.GUI, params.getDisplay());
    assertEquals(path, params.getExportPath());
  }
}
