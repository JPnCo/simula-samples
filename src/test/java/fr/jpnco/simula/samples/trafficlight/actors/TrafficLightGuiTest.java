package fr.jpnco.simula.samples.trafficlight.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.engine.EventImpl;
import fr.jpnco.simula.samples.trafficlight.states.Direction;
import fr.jpnco.simula.samples.trafficlight.states.GridState;
import fr.jpnco.simula.samples.trafficlight.states.LightState;
import fr.jpnco.simula.samples.trafficlight.states.VehicleView;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class TrafficLightGuiTest {

  private static GridState state() {
    final int n = TrafficCoordinator.INTERSECTIONS;
    final LightState[][] ns = new LightState[n][n];
    final LightState[][] ew = new LightState[n][n];
    final int[][] crossings = new int[n][n];
    for (int r = 0; r < n; r++) {
      for (int c = 0; c < n; c++) {
        ns[r][c] = (r == 0 && c == 0) ? LightState.GREEN : LightState.RED;
        ew[r][c] = LightState.RED;
      }
    }
    final List<VehicleView> views =
        List.of(
            new VehicleView(0, 0, 1, Direction.EAST, 10.0),
            new VehicleView(1, 1, 2, Direction.SOUTH, 40.0),
            new VehicleView(2, 2, 3, Direction.WEST, 90.0),
            new VehicleView(3, 3, 0, Direction.NORTH, 30.0));
    return new GridState(5, views, ns, ew, crossings);
  }

  @Test
  void getId_and_delegate_are_consistent() {
    final Engine engine = mock(Engine.class);
    final TrafficLightGui gui = new TrafficLightGui(engine);
    assertNotNull(gui.getId());
    assertEquals(gui.getDelegate(), gui.getDelegate());
    gui.dispose();
  }

  @Test
  void process_stores_latest_state() throws Exception {
    final Engine engine = mock(Engine.class);
    final TrafficLightGui gui = new TrafficLightGui(engine);
    gui.process(EventImpl.createEvent(Topics.NEW_STATE, gui, state()));
    final GridState stored = gui.snapshot();
    assertNotNull(stored);
    assertEquals(5, stored.getSimTime());
    gui.dispose();
  }

  @Test
  void grid_panel_paints_state_without_error() throws Exception {
    final Engine engine = mock(Engine.class);
    final TrafficLightGui gui = new TrafficLightGui(engine);
    gui.process(EventImpl.createEvent(Topics.NEW_STATE, gui, state()));

    final Field gridField = TrafficLightGui.class.getDeclaredField("grid");
    gridField.setAccessible(true);
    final JPanel grid = (JPanel) gridField.get(gui);

    final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
    final Graphics g = image.createGraphics();
    try {
      final Method paintComponent =
          grid.getClass().getDeclaredMethod("paintComponent", Graphics.class);
      paintComponent.setAccessible(true);
      paintComponent.invoke(grid, g);
    } finally {
      g.dispose();
      gui.dispose();
    }
  }

  @Test
  void ignores_unrelated_topics() throws Exception {
    final Engine engine = mock(Engine.class);
    final TrafficLightGui gui = new TrafficLightGui(engine);
    final Event other = EventImpl.createEvent("some-other-topic", gui, 1);
    gui.process(other);
    assertEquals(null, gui.snapshot());
    gui.dispose();
  }
}
