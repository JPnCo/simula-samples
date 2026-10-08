package fr.jpnco.simula.samples.boids;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.jpnco.simula.engine.ExecutionMode;
import org.junit.jupiter.api.Test;

class BoidsCliTest {

  @Test
  void resolveMode_returns_platform_for_classic_token() {
    assertEquals(ExecutionMode.PLATFORM, BoidsCli.resolveMode(new String[] {"classic"}));
  }

  @Test
  void resolveMode_defaults_to_virtual() {
    assertEquals(ExecutionMode.VIRTUAL, BoidsCli.resolveMode(new String[] {"something", "else"}));
  }

  @Test
  void resolveDisplay_returns_gui_for_gui_token() {
    assertEquals(BoidsCli.DisplayChoice.GUI, BoidsCli.resolveDisplay(new String[] {"gui"}));
  }

  @Test
  void resolveDisplay_defaults_to_console() {
    assertEquals(BoidsCli.DisplayChoice.CONSOLE, BoidsCli.resolveDisplay(new String[] {"classic"}));
  }

  @Test
  void resolveParameters_defaults_to_default_params() {
    final FlockParameters params = BoidsCli.resolveParameters(new String[] {"classic"});
    assertEquals(FlockParameters.DEFAULT.getBoidCount(), params.getBoidCount());
    assertEquals(FlockParameters.DEFAULT.getPerceptionRadius(), params.getPerceptionRadius());
  }

  @Test
  void resolveParameters_parses_custom_counts_and_radius() {
    final FlockParameters params =
        BoidsCli.resolveParameters(
            new String[] {"--boids=20", "--perception-radius=55", "--max-speed=6"});
    assertEquals(20, params.getBoidCount());
    assertEquals(55.0, params.getPerceptionRadius());
    assertEquals(6.0, params.getMaxSpeed());
    assertTrue(params.getBoidCount() > 0);
  }

  @Test
  void resolveParameters_parses_the_flocking_rule_weights() {
    final FlockParameters params =
        BoidsCli.resolveParameters(
            new String[] {
              "--separation-weight=2.5", "--alignment-weight=1.5", "--cohesion-weight=0.5"
            });
    assertEquals(2.5, params.getSeparationWeight());
    assertEquals(1.5, params.getAlignmentWeight());
    assertEquals(0.5, params.getCohesionWeight());
  }

  @Test
  void resolveParameters_defaults_the_weights_when_not_overridden() {
    final FlockParameters params = BoidsCli.resolveParameters(new String[] {"--boids=8"});
    assertEquals(FlockParameters.DEFAULT.getSeparationWeight(), params.getSeparationWeight());
    assertEquals(FlockParameters.DEFAULT.getAlignmentWeight(), params.getAlignmentWeight());
    assertEquals(FlockParameters.DEFAULT.getCohesionWeight(), params.getCohesionWeight());
  }
}
