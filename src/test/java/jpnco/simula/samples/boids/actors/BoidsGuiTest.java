package jpnco.simula.samples.boids.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import jpnco.simula.Engine;
import jpnco.simula.Event;
import jpnco.simula.engine.EngineImpl;
import jpnco.simula.engine.EventImpl;
import jpnco.simula.engine.ExecutionMode;
import jpnco.simula.samples.boids.FlockParameters;
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

  @Test
  void sliders_push_parameters_to_the_attached_coordinator() throws Exception {
    final EngineImpl root = new EngineImpl("gui-root", 1, ExecutionMode.VIRTUAL);
    final FlockParameters params = new FlockParameters(4, 1.0, 1.0, 1.0, 40.0, 4.0, 800.0, 600.0);
    final BoidsCoordinator coordinator = new BoidsCoordinator(root, params, 120);
    coordinator.seed();
    root.registerAndStart(coordinator);

    final BoidsGui gui = new BoidsGui(root);
    gui.attach(coordinator);
    try {
      SwingUtilities.invokeAndWait(
          () -> {
            try {
              final Field speedField = BoidsGui.class.getDeclaredField("speedSlider");
              speedField.setAccessible(true);
              ((JSlider) speedField.get(gui)).setValue(1);
            } catch (final ReflectiveOperationException exc) {
              throw new RuntimeException(exc);
            }
          });
      root.start();
      root.signal(EventImpl.createEvent(Engine.TIME_EVENT, root, 1));
      final FlockState snap = awaitSnapshot(coordinator);
      assertNotNull(snap);
      for (final BoidView boid : snap.getBoids()) {
        assertTrue(Math.hypot(boid.getVx(), boid.getVy()) <= 1.0 + 1e-9);
      }
    } finally {
      root.stop();
      gui.dispose();
    }
  }

  private static FlockState awaitSnapshot(final BoidsCoordinator coordinator)
      throws InterruptedException {
    final long deadline = System.currentTimeMillis() + 5000L;
    FlockState state = coordinator.snapshot();
    while (state == null && System.currentTimeMillis() < deadline) {
      Thread.sleep(10L);
      state = coordinator.snapshot();
    }
    return state;
  }
}
