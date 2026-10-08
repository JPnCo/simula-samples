package fr.jpnco.simula.samples.dashboard.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.engine.EventImpl;
import fr.jpnco.simula.samples.boids.states.BoidView;
import fr.jpnco.simula.samples.boids.states.FlockState;
import fr.jpnco.simula.samples.dashboard.model.Sample;
import fr.jpnco.simula.samples.dashboard.model.TimeSeries;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.Test;

class DashboardMonitorTest {

  private static FlockState state(final int simTime, final int boids) {
    final BoidView view = new BoidView(0, 10.0, 20.0, 1.0, 1.0);
    final List<BoidView> views = new java.util.ArrayList<>();
    for (int i = 0; i < boids; i++) {
      views.add(view);
    }
    return new FlockState(simTime, views, 800.0, 600.0);
  }

  @Test
  void renderLine_contains_sim_time_and_value() {
    final String line = DashboardMonitor.renderLine(new Sample(5, 42.0));
    assertEquals("t=5 value=42.0", line);
  }

  @Test
  void process_prints_a_line_for_a_new_state_event() {
    final Engine engine = mock(Engine.class);
    final DashboardMonitor monitor = new DashboardMonitor(engine, Topics.NEW_STATE);
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      monitor.process(EventImpl.createEvent(Topics.NEW_STATE, monitor, state(3, 8)));
    } finally {
      System.setOut(original);
    }
    assertEquals("t=3 value=8.0" + System.lineSeparator(), buffer.toString());
  }

  @Test
  void process_ignores_unrelated_topics() {
    final Engine engine = mock(Engine.class);
    final DashboardMonitor monitor = new DashboardMonitor(engine, Topics.NEW_STATE);
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

  @Test
  void printSummary_reports_count_first_and_last() {
    final Engine engine = mock(Engine.class);
    final DashboardMonitor monitor = new DashboardMonitor(engine, Topics.NEW_STATE);
    final TimeSeries series =
        TimeSeries.empty().append(new Sample(1, 10.0)).append(new Sample(2, 20.0));
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      monitor.printSummary(series);
    } finally {
      System.setOut(original);
    }
    assertEquals("samples=2, first=1, last=2" + System.lineSeparator(), buffer.toString());
  }

  @Test
  void printSummary_handles_an_empty_series() {
    final Engine engine = mock(Engine.class);
    final DashboardMonitor monitor = new DashboardMonitor(engine, Topics.NEW_STATE);
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    try {
      System.setOut(new PrintStream(buffer));
      monitor.printSummary(TimeSeries.empty());
    } finally {
      System.setOut(original);
    }
    assertEquals("samples=0, first=none, last=none" + System.lineSeparator(), buffer.toString());
  }
}
