package jpnco.simula.samples.boids;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FlockParametersTest {

  @Test
  void defaults_match_the_configured_sensible_values() {
    assertEquals(40, FlockParameters.DEFAULT.getBoidCount());
    assertEquals(1.0, FlockParameters.DEFAULT.getSeparationWeight());
    assertEquals(1.0, FlockParameters.DEFAULT.getAlignmentWeight());
    assertEquals(1.0, FlockParameters.DEFAULT.getCohesionWeight());
    assertEquals(40.0, FlockParameters.DEFAULT.getPerceptionRadius());
    assertEquals(4.0, FlockParameters.DEFAULT.getMaxSpeed());
    assertEquals(800.0, FlockParameters.DEFAULT.getWorldWidth());
    assertEquals(600.0, FlockParameters.DEFAULT.getWorldHeight());
  }

  @Test
  void custom_values_are_exposed() {
    final FlockParameters p = new FlockParameters(10, 0.5, 0.7, 0.9, 25.0, 3.0, 500.0, 400.0);
    assertEquals(10, p.getBoidCount());
    assertEquals(0.5, p.getSeparationWeight());
    assertEquals(0.7, p.getAlignmentWeight());
    assertEquals(0.9, p.getCohesionWeight());
    assertEquals(25.0, p.getPerceptionRadius());
    assertEquals(3.0, p.getMaxSpeed());
    assertEquals(500.0, p.getWorldWidth());
    assertEquals(400.0, p.getWorldHeight());
  }
}
