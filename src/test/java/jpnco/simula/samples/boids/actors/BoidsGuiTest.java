package jpnco.simula.samples.boids.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import javax.swing.JPanel;
import jpnco.simula.Engine;
import jpnco.simula.Event;
import jpnco.simula.engine.EventImpl;
import jpnco.simula.samples.boids.states.BoidView;
import jpnco.simula.samples.boids.states.FlockState;
import org.junit.jupiter.api.Test;

class BoidsGuiTest {

  private static FlockState state() {
    return new FlockState(
        5,
        List.of(
            new BoidView(0, 10.0, 20.0, 1.0, 1.0),
            new BoidView(1, 50.0, 60.0, -1.0, 0.5),
            new BoidView(2, 100.0, 200.0, 0.0, 1.0)),
        800.0,
        600.0);
  }

  @Test
  void getId_and_delegate_are_consistent() {
    final Engine engine = mock(Engine.class);
    final BoidsGui gui = new BoidsGui(engine);
    assertNotNull(gui.getId());
    assertEquals(gui.getDelegate(), gui.getDelegate());
    gui.dispose();
  }

  @Test
  void process_stores_latest_state() {
    final Engine engine = mock(Engine.class);
    final BoidsGui gui = new BoidsGui(engine);
    gui.process(EventImpl.createEvent(Topics.NEW_STATE, gui, state()));
    final FlockState stored = gui.snapshot();
    assertNotNull(stored);
    assertEquals(5, stored.getSimTime());
    gui.dispose();
  }

  @Test
  void ignores_unrelated_topics() {
    final Engine engine = mock(Engine.class);
    final BoidsGui gui = new BoidsGui(engine);
    final Event other = EventImpl.createEvent("some-other-topic", gui, 1);
    gui.process(other);
    assertEquals(null, gui.snapshot());
    gui.dispose();
  }

  @Test
  void grid_panel_paints_state_without_error() throws Exception {
    final Engine engine = mock(Engine.class);
    final BoidsGui gui = new BoidsGui(engine);
    gui.process(EventImpl.createEvent(Topics.NEW_STATE, gui, state()));

    final Field gridField = BoidsGui.class.getDeclaredField("grid");
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
}
