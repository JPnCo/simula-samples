package fr.jpnco.simula.samples.boids.actors;

import fr.jpnco.simula.Actor;
import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.engine.ActorDelegate;
import fr.jpnco.simula.engine.IdBuilder;
import fr.jpnco.simula.samples.boids.FlockParameters;
import fr.jpnco.simula.samples.boids.states.BoidView;
import fr.jpnco.simula.samples.boids.states.FlockState;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * A Swing (Java2D) window that visualizes the {@code boids} flocking simulation in real time.
 *
 * <p>This window is an {@link Actor}: it subscribes to {@link Topics#NEW_STATE}, stores the latest
 * {@link FlockState} snapshot, and a {@link Timer} polls that snapshot on the Event Dispatch Thread
 * and repaints the panel. Every boid is drawn as a small circle at its position, scaled to pixels
 * by the window size divided by the world size, so the flock is visible as it forms and moves. The
 * status line shows the simulated time and the number of boids.
 *
 * <p>A column of five sliders (separation, alignment, cohesion weights, perception radius and max
 * speed) lets the user tune the flocking live: whenever a slider moves, the window pushes a fresh
 * {@link FlockParameters} to the attached {@link BoidsCoordinator}, which forwards it to every boid
 * (FR-009). The sliders are inert until {@link #attach} is called.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-006, FR-009, SC-004.
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

  /** The length of a boid triangle from tail to nose, in pixels. */
  private static final int BOID_LENGTH = 14;

  /** The half width of a boid triangle, in pixels. */
  private static final int BOID_HALF_WIDTH = 4;

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

  /** The slider controlling the perception radius. */
  private final JSlider perceptionSlider;

  /** The slider controlling the separation weight. */
  private final JSlider separationSlider;

  /** The slider controlling the alignment weight. */
  private final JSlider alignmentSlider;

  /** The slider controlling the cohesion weight. */
  private final JSlider cohesionSlider;

  /** The slider controlling the maximum speed. */
  private final JSlider speedSlider;

  /** The coordinator that receives live parameter changes, or {@code null} if not yet attached. */
  private volatile BoidsCoordinator coordinator;

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

    final JPanel controls = new JPanel();
    controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
    controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    separationSlider =
        newSlider(0, 500, (int) (FlockParameters.DEFAULT.getSeparationWeight() * 100));
    alignmentSlider = newSlider(0, 500, (int) (FlockParameters.DEFAULT.getAlignmentWeight() * 100));
    cohesionSlider = newSlider(0, 500, (int) (FlockParameters.DEFAULT.getCohesionWeight() * 100));
    perceptionSlider = newSlider(5, 200, (int) FlockParameters.DEFAULT.getPerceptionRadius());
    speedSlider = newSlider(1, 20, (int) FlockParameters.DEFAULT.getMaxSpeed());
    controls.add(sliderRow("Separation", separationSlider));
    controls.add(sliderRow("Alignment", alignmentSlider));
    controls.add(sliderRow("Cohesion", cohesionSlider));
    controls.add(sliderRow("Perception radius", perceptionSlider));
    controls.add(sliderRow("Max speed", speedSlider));
    add(controls, BorderLayout.EAST);

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
   * Creates a slider in the given integer range with the given initial value and a permanent change
   * listener that pushes the current parameters to the attached coordinator. Participates in:
   * FR-006, FR-009, SC-004.
   *
   * @param min the minimum slider value
   * @param max the maximum slider value
   * @param value the initial slider value
   * @return the configured slider
   */
  private JSlider newSlider(final int min, final int max, final int value) {
    final JSlider slider = new JSlider(min, max, value);
    slider.setMajorTickSpacing((max - min) / 2);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    slider.setLabelTable(createLabelTable(min, max));
    slider.addChangeListener(e -> pushParameters());
    return slider;
  }

  /**
   * Builds the label table that paints the minimum and maximum values at the two ends of a slider.
   * Participates in: FR-006, FR-009, SC-004.
   *
   * @param min the minimum slider value
   * @param max the maximum slider value
   * @return the label table mapping the two boundary values to their labels
   */
  private static Hashtable<Integer, JLabel> createLabelTable(final int min, final int max) {
    final Hashtable<Integer, JLabel> table = new Hashtable<>();
    table.put(min, new JLabel(String.valueOf(min)));
    table.put(max, new JLabel(String.valueOf(max)));
    return table;
  }

  /**
   * Builds a horizontal row holding a label, the current value and a slider; the current value is
   * updated live as the slider moves. Participates in: FR-006, FR-009, SC-004.
   *
   * @param label the label text
   * @param slider the slider
   * @return the row panel
   */
  private JPanel sliderRow(final String label, final JSlider slider) {
    final JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
    final JLabel caption = new JLabel(label);
    caption.setPreferredSize(new Dimension(110, 20));
    final JLabel value = new JLabel(String.valueOf(slider.getValue()));
    value.setPreferredSize(new Dimension(40, 20));
    value.setHorizontalAlignment(SwingConstants.RIGHT);
    slider.addChangeListener(e -> value.setText(String.valueOf(slider.getValue())));
    row.add(caption);
    row.add(value);
    row.add(slider);
    return row;
  }

  /**
   * Reads all sliders and, if a coordinator is attached, pushes the resulting {@link
   * FlockParameters} to it so every boid adopts the new values at its next tick. Participates in:
   * FR-006, FR-009, SC-004.
   */
  private void pushParameters() {
    final BoidsCoordinator target = coordinator;
    if (target != null) {
      target.updateParameters(
          new FlockParameters(
              target.getBoidCount(),
              separationSlider.getValue() / 100.0,
              alignmentSlider.getValue() / 100.0,
              cohesionSlider.getValue() / 100.0,
              perceptionSlider.getValue(),
              speedSlider.getValue(),
              target.snapshot() == null ? 800.0 : target.snapshot().getWorldWidth(),
              target.snapshot() == null ? 600.0 : target.snapshot().getWorldHeight()));
    }
  }

  /**
   * Attaches the coordinator whose boids the sliders control. Until this is called the sliders do
   * nothing. Participates in: FR-006, FR-009, SC-004.
   *
   * @param coordinator the coordinator to control
   */
  public void attach(final BoidsCoordinator coordinator) {
    this.coordinator = coordinator;
    pushParameters();
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
     * Draws each boid as an elongated triangle whose nose points in the direction of its velocity.
     * Participates in: FR-006, SC-004.
     *
     * @param g2 the graphics context
     * @param state the state to draw
     */
    private void drawFlock(final Graphics2D g2, final FlockState state) {
      final double scaleX = getWidth() / state.getWorldWidth();
      final double scaleY = getHeight() / state.getWorldHeight();
      g2.setColor(BOID_COLOR);
      final Path2D base = new Path2D.Double();
      base.moveTo(BOID_LENGTH / 2.0, 0.0);
      base.lineTo(-BOID_LENGTH / 2.0, -BOID_HALF_WIDTH);
      base.lineTo(-BOID_LENGTH / 2.0, BOID_HALF_WIDTH);
      base.closePath();
      final AffineTransform transform = new AffineTransform();
      for (final BoidView boid : state.getBoids()) {
        final double px = boid.getX() * scaleX;
        final double py = boid.getY() * scaleY;
        final double angle = Math.atan2(boid.getVy(), boid.getVx());
        transform.setToIdentity();
        transform.translate(px, py);
        transform.rotate(angle);
        g2.fill(base.createTransformedShape(transform));
      }
    }
  }
}
