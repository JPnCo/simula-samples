package fr.jpnco.simula.samples.dashboard.actors;

import fr.jpnco.simula.Actor;
import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.engine.ActorDelegate;
import fr.jpnco.simula.engine.IdBuilder;
import fr.jpnco.simula.samples.dashboard.model.Sample;
import fr.jpnco.simula.samples.dashboard.model.TimeSeries;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * A Swing (Java2D) window that plots the time series collected by a {@link Dashboard} in real time
 * (FR-005, SC-004).
 *
 * <p>This window is an {@link Actor}: it subscribes to the {@code new-state} topic of the target
 * simulation and a {@link Timer} repaints the plot on the Event Dispatch Thread. The plot reads the
 * collected series from the attached {@link Dashboard} (its {@code volatile} {@link TimeSeries}),
 * so it reflects the latest samples as they are recorded. The status line shows the current sample
 * count and the latest simulated time.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-005, SC-004.
 */
public final class DashboardGui extends JFrame implements Actor {

  /** The window title. */
  private static final String TITLE = "Simula Dashboard";

  /** The default window width in pixels. */
  private static final int WIDTH = 900;

  /** The default window height in pixels. */
  private static final int HEIGHT = 600;

  /** The milliseconds between two repaints. */
  private static final int REPAINT_INTERVAL_MILLIS = 100;

  /** The background colour of the plot. */
  private static final Color BACKGROUND = new Color(245, 247, 250);

  /** The colour of the plotted line. */
  private static final Color LINE_COLOR = new Color(20, 90, 200);

  /** The horizontal padding around the plot, in pixels. */
  private static final int PADDING = 40;

  /** The serial version UID for this {@code JFrame}. */
  private static final long serialVersionUID = 1L;

  /** The actor delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The unique identity of the GUI actor. */
  private final Integer id;

  /** The dashboard whose collected series is plotted. */
  private final Dashboard dashboard;

  /** The panel that draws the plot. */
  private final JPanel plot;

  /** The status label showing the sample count and the latest time. */
  private final JLabel status;

  /**
   * Creates the window, subscribes it to the {@code new-state} topic and starts the repaint timer.
   * Participates in: FR-005, SC-004.
   *
   * @param engine the engine this actor lives on
   * @param dashboard the dashboard whose series is plotted
   */
  public DashboardGui(final Engine engine, final Dashboard dashboard) {
    super(TITLE);
    this.dashboard = dashboard;
    id = IdBuilder.nextId();
    delegate = ActorDelegate.createDelegate(engine, this);
    engine.subscribe(this, fr.jpnco.simula.samples.dashboard.actors.Topics.NEW_STATE);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    plot = new PlotPanel(this);
    plot.setPreferredSize(new Dimension(WIDTH, HEIGHT - 40));
    add(plot, BorderLayout.CENTER);

    status = new JLabel(" ");
    status.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    status.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    add(status, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(null);

    final Timer timer = new Timer(REPAINT_INTERVAL_MILLIS, e -> refresh());
    timer.start();
  }

  /**
   * Returns the actor delegate that drives this actor. Participates in: FR-005, SC-004.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this actor. Participates in: FR-005, SC-004.
   *
   * @return the id
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Processes a received event; the plot reads the dashboard's series directly, so this is a no-op
   * kept to satisfy the {@link Actor} contract. Participates in: FR-005, SC-004.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    // The plot reads the latest series from the dashboard on each repaint.
  }

  /**
   * Returns the latest series collected by the dashboard. Participates in: FR-005, SC-004.
   *
   * @return the collected series
   */
  TimeSeries series() {
    return dashboard.snapshot();
  }

  /**
   * Refreshes the plot and the status line from the dashboard's latest series. Participates in:
   * FR-005, SC-004.
   */
  private void refresh() {
    plot.repaint();
    final TimeSeries series = series();
    if (!series.isEmpty()) {
      status.setText("samples=" + series.size() + "   last t=" + series.last().getSimTime());
    }
  }

  /** Shows the window on the Event Dispatch Thread. Participates in: FR-005, SC-004. */
  public void showWindow() {
    SwingUtilities.invokeLater(() -> setVisible(true));
  }

  /** The panel that plots the collected time series with Java2D. */
  private static final class PlotPanel extends JPanel {

    /** The serial version UID for this {@code JPanel}. */
    private static final long serialVersionUID = 1L;

    /** The owning window used to read the latest series. */
    private final DashboardGui owner;

    /**
     * Creates the panel for the given owner window. Participates in: FR-005, SC-004.
     *
     * @param owner the owning window
     */
    PlotPanel(final DashboardGui owner) {
      this.owner = owner;
      setBackground(BACKGROUND);
    }

    /**
     * Paints the time series from the dashboard's latest series. Participates in: FR-005, SC-004.
     *
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(final Graphics g) {
      super.paintComponent(g);
      final Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(BACKGROUND);
      g2.fillRect(0, 0, getWidth(), getHeight());
      drawSeries(g2, owner.series());
      g2.dispose();
    }

    /**
     * Draws the collected samples as a polyline of value against simulated time. Participates in:
     * FR-005, SC-004.
     *
     * @param g2 the graphics context
     * @param series the series to draw
     */
    private void drawSeries(final Graphics2D g2, final TimeSeries series) {
      final List<Sample> samples = series.getSamples();
      if (samples.isEmpty()) {
        return;
      }
      final int width = getWidth() - PADDING * 2;
      final int height = getHeight() - PADDING * 2;
      final int minTime = samples.get(0).getSimTime();
      final int maxTime = samples.get(samples.size() - 1).getSimTime();
      double minValue = samples.get(0).getValue();
      double maxValue = samples.get(0).getValue();
      for (final Sample sample : samples) {
        minValue = Math.min(minValue, sample.getValue());
        maxValue = Math.max(maxValue, sample.getValue());
      }
      final double timeSpan = Math.max(1, maxTime - minTime);
      final double valueSpan = Math.max(1e-9, maxValue - minValue);

      g2.setColor(LINE_COLOR);
      g2.setStroke(new BasicStroke(2.0f));
      final int[] xs = new int[samples.size()];
      final int[] ys = new int[samples.size()];
      for (int i = 0; i < samples.size(); i++) {
        final Sample sample = samples.get(i);
        xs[i] = PADDING + (int) ((sample.getSimTime() - minTime) / timeSpan * width);
        ys[i] = PADDING + height - (int) ((sample.getValue() - minValue) / valueSpan * height);
      }
      g2.drawPolyline(xs, ys, xs.length);
    }
  }
}
