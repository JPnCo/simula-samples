package jpnco.simula.samples.trafficlight.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import jpnco.simula.Engine;
import jpnco.simula.Event;
import jpnco.simula.engine.EventImpl;
import jpnco.simula.samples.trafficlight.states.Direction;
import jpnco.simula.samples.trafficlight.states.GridState;
import jpnco.simula.samples.trafficlight.states.LightState;
import jpnco.simula.samples.trafficlight.states.VehicleView;
import org.junit.jupiter.api.Test;

class TrafficMonitorTest {

  private static GridState state() {
    final int n = TrafficCoordinator.INTERSECTIONS;
    final LightState[][] ns = new LightState[n][n];
    final LightState[][] ew = new LightState[n][n];
    final int[][] crossings = new int[n][n];
    for (int r = 0; r < n; r++) {
      for (int c = 0; c < n; c++) {
        ns[r][c] = LightState.GREEN;
        ew[r][c] = LightState.RED;
      }
    }
    return new GridState(
        3, List.of(new VehicleView(0, 0, 1, Direction.EAST, 10.0)), ns, ew, crossings);
  }

  @Test
  void process_prints_rendered_state_to_stdout() {
    final Engine engine = mock(Engine.class);
    final TrafficMonitor monitor = new TrafficMonitor(engine);

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
  void getId_is_unique_and_non_null() {
    final Engine engine = mock(Engine.class);
    final TrafficMonitor monitor = new TrafficMonitor(engine);
    assertEquals(monitor.getDelegate(), monitor.getDelegate());
    assertEquals(monitor.getId(), monitor.getId());
  }

  @Test
  void ignores_unrelated_topics() {
    final Engine engine = mock(Engine.class);
    final TrafficMonitor monitor = new TrafficMonitor(engine);
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
