package jpnco.simula.samples.boids.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import jpnco.simula.Engine;
import jpnco.simula.Event;
import jpnco.simula.engine.EventImpl;
import jpnco.simula.samples.boids.states.BoidView;
import jpnco.simula.samples.boids.states.FlockState;
import org.junit.jupiter.api.Test;

class BoidsMonitorTest {

  private static FlockState state() {
    return new FlockState(3, List.of(new BoidView(0, 10.0, 20.0, 1.0, 1.0)), 800.0, 600.0);
  }

  @Test
  void process_prints_rendered_state_to_stdout() {
    final Engine engine = mock(Engine.class);
    final BoidsMonitor monitor = new BoidsMonitor(engine);

    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      monitor.process(EventImpl.createEvent(Topics.NEW_STATE, monitor, state()));
    } finally {
      System.setOut(original);
    }
    assertTrue(buffer.toString().startsWith("t=3"));
  }

  @Test
  void getId_and_delegate_are_consistent() {
    final Engine engine = mock(Engine.class);
    final BoidsMonitor monitor = new BoidsMonitor(engine);
    assertEquals(monitor.getDelegate(), monitor.getDelegate());
    assertEquals(monitor.getId(), monitor.getId());
  }

  @Test
  void ignores_unrelated_topics() {
    final Engine engine = mock(Engine.class);
    final BoidsMonitor monitor = new BoidsMonitor(engine);
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      final Event other = EventImpl.createEvent("some-other-topic", monitor, 1);
      monitor.process(other);
    } finally {
      System.setOut(original);
    }
    assertEquals("", buffer.toString());
  }
}
