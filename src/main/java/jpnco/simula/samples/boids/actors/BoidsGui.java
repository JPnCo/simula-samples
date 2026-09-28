package jpnco.simula.samples.boids.actors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import jpnco.simula.Actor;
import jpnco.simula.Engine;
import jpnco.simula.Event;
import jpnco.simula.engine.ActorDelegate;
import jpnco.simula.engine.IdBuilder;
import jpnco.simula.samples.boids.states.BoidView;
import jpnco.simula.samples.boids.states.FlockState;

/**
 * A Swing (Java2D) window that visualizes the {@code boids} flocking simulation in real time.
 *
 * <p>This window is an {@link Actor}: it subscribes to {@link Topics#NEW_STATE}, stores the latest
 * {@link FlockState} snapshot, and a {@link Timer} polls that snapshot on the Event Dispatch Thread
 * and repaints the panel. Every boid is drawn as a small circle at its position, scaled to pixels
 * by the window size divided by the world size, so the flock is visible as it forms and moves. The
 * status line shows the simulated time and the number of boids.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-006, SC-004.
 */
public final class BoidsGui extends JFrame implements Actor {

  /** The window title. */
  private static final String TITLE = "Boids Flocking";

  /** The default window width in pixels. */
  private static final int WIDTH = 1000;

  /** The default window height in pixels. */
  private static final int HEIGHT = 800;

  /** The milliseconds between two repaints. */
  private static final int REPAINT_INTERVAL_MILLIS = 100;

  /** The diameter of a boid in pixels. */
  private static final int BOID_DIAMETER = 8;

  /** The background colour of the world. */
  private static final Color BACKGROUND = new Color(235, 240, 245);

  /** The colour of a boid. */
  private static final Color BOID_COLOR = new Color(20, 90, 200);

  /** The serial version UID for this {@code JFrame}. */
  private static final long serialVersionUID = 1L;

  /** The actor delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The unique identity of the GUI actor. */
  private final Integer id;

  /** The panel that draws the flock. */
  private final JPanel grid;

  /** The status label showing the time and boid count. */
  private final JLabel status;

  /** The latest snapshot received on {@link Topics#NEW_STATE}. */
  private volatile FlockState state;

  /**
   * Creates the window, subscribes it to {@link Topics#NEW_STATE} and starts the repaint timer.
   * Participates in: FR-006, SC-004.
   *
   * @param engine the engine this actor lives on
   */
  public BoidsGui(final Engine engine) {
    super(TITLE);
    id = IdBuilder.nextId();
    delegate = ActorDelegate.createDelegate(engine, this);
    engine.subscribe(this, Topics.NEW_STATE);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    grid = new GridPanel(this);
    grid.setPreferredSize(new Dimension(WIDTH, HEIGHT - 60));
    add(grid, BorderLayout.CENTER);

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
   * Returns the actor delegate that drives this actor. Participates in: FR-006, SC-004.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this actor. Participates in: FR-006, SC-004.
   *
   * @return the id
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Processes a received event: on {@link Topics#NEW_STATE} it stores the latest snapshot.
   * Participates in: FR-006, SC-004.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    if (Topics.NEW_STATE.equals(event.getTopic())) {
      state = (FlockState) event.getParameters()[0];
    }
  }

  /**
   * Returns the latest snapshot received, or {@code null} if none has been received yet.
   * Participates in: FR-006, SC-004.
   *
   * @return the latest snapshot, may be {@code null}
   */
  FlockState snapshot() {
    return state;
  }

  /**
   * Refreshes the grid and the status line from the latest snapshot. Participates in: FR-006,
   * SC-004.
   */
  private void refresh() {
    grid.repaint();
    final FlockState state = snapshot();
    if (state != null) {
      status.setText("t=" + state.getSimTime() + "   boids=" + state.getBoidCount());
    }
  }

  /** Shows the window on the Event Dispatch Thread. Participates in: FR-006, SC-004. */
  public void showWindow() {
    SwingUtilities.invokeLater(() -> setVisible(true));
  }

  /** The panel that draws the flock with Java2D. */
  private static final class GridPanel extends JPanel {

    /** The serial version UID for this {@code JPanel}. */
    private static final long serialVersionUID = 1L;

    /** The owning window used to read the latest snapshot. */
    private final BoidsGui owner;

    /**
     * Creates the panel for the given owner window. Participates in: FR-006, SC-004.
     *
     * @param owner the owning window
     */
    GridPanel(final BoidsGui owner) {
      this.owner = owner;
      setBackground(BACKGROUND);
    }

    /**
     * Paints the flock from the latest snapshot. Participates in: FR-006, SC-004.
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

      final FlockState state = owner.snapshot();
      if (state != null) {
        drawFlock(g2, state);
      }

      g2.dispose();
    }

    /**
     * Draws each boid as a circle at its position scaled to the panel size. Participates in:
     * FR-006, SC-004.
     *
     * @param g2 the graphics context
     * @param state the state to draw
     */
    private void drawFlock(final Graphics2D g2, final FlockState state) {
      final double scaleX = getWidth() / state.getWorldWidth();
      final double scaleY = getHeight() / state.getWorldHeight();
      g2.setColor(BOID_COLOR);
      for (final BoidView boid : state.getBoids()) {
        final int px = (int) Math.round(boid.getX() * scaleX);
        final int py = (int) Math.round(boid.getY() * scaleY);
        g2.fillOval(px - BOID_DIAMETER / 2, py - BOID_DIAMETER / 2, BOID_DIAMETER, BOID_DIAMETER);
      }
    }
  }
}
