package fr.jpnco.simula.samples.dashboard.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import fr.jpnco.simula.Engine;
import fr.jpnco.simula.engine.EventImpl;
import fr.jpnco.simula.samples.boids.states.BoidView;
import fr.jpnco.simula.samples.boids.states.FlockState;
import fr.jpnco.simula.samples.dashboard.model.TimeSeries;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class DashboardGuiTest {

  private static FlockState state(final int simTime, final int boids) {
    final BoidView view = new BoidView(0, 10.0, 20.0, 1.0, 1.0);
    final List<BoidView> views = new java.util.ArrayList<>();
    for (int i = 0; i < boids; i++) {
      views.add(view);
    }
    return new FlockState(simTime, views, 800.0, 600.0);
  }

  @Test
  void getId_and_delegate_are_consistent() {
    final Engine engine = mock(Engine.class);
    final Dashboard dashboard = new Dashboard(engine, Topics.NEW_STATE);
    final DashboardGui gui = new DashboardGui(engine, dashboard);
    assertNotNull(gui.getId());
    assertEquals(gui.getDelegate(), gui.getDelegate());
    gui.dispose();
  }

  @Test
  void series_reads_the_dashboard_collection() {
    final Engine engine = mock(Engine.class);
    final Dashboard dashboard = new Dashboard(engine, Topics.NEW_STATE);
    final DashboardGui gui = new DashboardGui(engine, dashboard);
    dashboard.process(EventImpl.createEvent(Topics.NEW_STATE, gui, state(3, 8)));
    final TimeSeries series = gui.series();
    assertEquals(1, series.size());
    assertEquals(8.0, series.first().getValue());
    gui.dispose();
  }

  @Test
  void plot_panel_paints_the_series_without_error() throws Exception {
    final Engine engine = mock(Engine.class);
    final Dashboard dashboard = new Dashboard(engine, Topics.NEW_STATE);
    final DashboardGui gui = new DashboardGui(engine, dashboard);
    dashboard.process(EventImpl.createEvent(Topics.NEW_STATE, gui, state(3, 8)));

    final Field plotField = DashboardGui.class.getDeclaredField("plot");
    plotField.setAccessible(true);
    final JPanel plot = (JPanel) plotField.get(gui);

    final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
    final Graphics g = image.createGraphics();
    try {
      final Method paintComponent =
          plot.getClass().getDeclaredMethod("paintComponent", Graphics.class);
      paintComponent.setAccessible(true);
      paintComponent.invoke(plot, g);
    } finally {
      g.dispose();
      gui.dispose();
    }
  }
}
