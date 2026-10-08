package fr.jpnco.simula.samples.trafficlight.actors;

import fr.jpnco.simula.Actor;
import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.engine.ActorDelegate;
import fr.jpnco.simula.engine.IdBuilder;
import fr.jpnco.simula.samples.trafficlight.states.Direction;
import fr.jpnco.simula.samples.trafficlight.states.GridState;
import fr.jpnco.simula.samples.trafficlight.states.VehicleView;
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

/**
 * A Swing (Java2D) window that visualizes the {@code trafficlight} grid simulation in real time.
 *
 * <p>This window is an {@link Actor}: it subscribes to {@link Topics#NEW_STATE}, stores the latest
 * {@link GridState} snapshot, and a {@link Timer} polls that snapshot on the Event Dispatch Thread
 * and repaints the panel. The roads are the grid lines of the {@value TrafficCoordinator#GRID_SIZE}
 * by {@value TrafficCoordinator#GRID_SIZE} cells: each lane is drawn along a grid line at its
 * physical width scaled to pixels by {@link GridGeometry}, and an intersection sits at every
 * crossing of a vertical and a horizontal road, so there are {@value
 * TrafficCoordinator#INTERSECTIONS} by {@value TrafficCoordinator#INTERSECTIONS} intersections. A
 * small circle at each intersection marks its light band, colored by {@link LightColors}, and every
 * vehicle is drawn as a small circle at its current position along a road. The status line shows
 * the simulated time, the vehicle count and the total crossings so far.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-007, SC-004.
 */
public final class TrafficLightGui extends JFrame implements Actor {

  /** The window title. */
  private static final String TITLE = "Traffic Light Grid";

  /** The default window width in pixels. */
  private static final int WIDTH = 1080;

  /** The default window height in pixels. */
  private static final int HEIGHT = 900;

  /** The milliseconds between two repaints. */
  private static final int REPAINT_INTERVAL_MILLIS = 100;

  /** The serial version UID for this {@code JFrame}. */
  private static final long serialVersionUID = 1L;

  /** The actor delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The unique identity of the GUI actor. */
  private final Integer id;

  /** The panel that draws the grid. */
  private final GridPanel grid;

  /** The status label showing the time, vehicle count and crossings. */
  private final JLabel status;

  /** The latest snapshot received on {@link Topics#NEW_STATE}. */
  private volatile GridState state;

  /**
   * Creates the window, subscribes it to {@link Topics#NEW_STATE} and starts the repaint timer.
   * Participates in: FR-007, SC-004.
   *
   * @param engine the engine this actor lives on
   */
  public TrafficLightGui(final Engine engine) {
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
   * Returns the actor delegate that drives this actor. Participates in: FR-007, SC-004.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this actor. Participates in: FR-007, SC-004.
   *
   * @return the id
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Processes a received event: on {@link Topics#NEW_STATE} it stores the latest snapshot.
   * Participates in: FR-007, SC-004.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    if (Topics.NEW_STATE.equals(event.getTopic())) {
      state = (GridState) event.getParameters()[0];
    }
  }

  /**
   * Returns the latest snapshot received, or {@code null} if none has been received yet.
   * Participates in: FR-007, SC-004.
   *
   * @return the latest snapshot, may be {@code null}
   */
  GridState snapshot() {
    return state;
  }

  /**
   * Refreshes the grid and the status line from the latest snapshot. Participates in: FR-007,
   * SC-004.
   */
  private void refresh() {
    grid.repaint();
    final GridState state = snapshot();
    if (state != null) {
      status.setText(
          "t="
              + state.getSimTime()
              + "   vehicles="
              + state.getVehicleCount()
              + "   totalCrossings="
              + totalCrossings(state));
    }
  }

  /**
   * Returns the total number of crossings summed over all cells. Participates in: FR-007, SC-004.
   *
   * @param state the state to read
   * @return the total crossings
   */
  private static int totalCrossings(final GridState state) {
    int total = 0;
    for (int row = 0; row < TrafficCoordinator.INTERSECTIONS; row++) {
      for (int col = 0; col < TrafficCoordinator.INTERSECTIONS; col++) {
        total += state.crossingsAt(row, col);
      }
    }
    return total;
  }

  /** Shows the window on the Event Dispatch Thread. Participates in: FR-007, SC-004. */
  public void showWindow() {
    SwingUtilities.invokeLater(
        () -> {
          setVisible(true);
        });
  }

  /** The panel that draws the grid with Java2D. */
  private static final class GridPanel extends JPanel {

    /** The serial version UID for this {@code JPanel}. */
    private static final long serialVersionUID = 1L;

    /** The owning window used to read the latest snapshot. */
    private final TrafficLightGui owner;

    /**
     * Creates the panel for the given owner window. Participates in: FR-007, SC-004.
     *
     * @param owner the owning window
     */
    GridPanel(final TrafficLightGui owner) {
      this.owner = owner;
      setBackground(LightColors.GROUND);
    }

    /**
     * Paints the grid, roads, lights and vehicles from the latest snapshot. Participates in:
     * FR-007, SC-004.
     *
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(final Graphics g) {
      super.paintComponent(g);
      final Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      final GridState state = owner.snapshot();
      final GridGeometry geo = GridGeometry.of(getWidth(), getHeight());

      g2.setColor(LightColors.GROUND);
      g2.fillRect(0, 0, getWidth(), getHeight());

      if (state != null) {
        drawGrid(g2, state, geo);
      }

      g2.dispose();
    }

    /**
     * Draws the roads, lights and vehicles for the given state. Participates in: FR-007, SC-004.
     *
     * @param g2 the graphics context
     * @param state the state to draw
     * @param geo the geometry
     */
    private void drawGrid(final Graphics2D g2, final GridState state, final GridGeometry geo) {
      drawRoads(g2, geo);
      drawLights(g2, state, geo);
      drawVehicles(g2, state, geo);
    }

    /**
     * Draws the roads as the grid lines of the {@value TrafficCoordinator#GRID_SIZE} by {@value
     * TrafficCoordinator#GRID_SIZE} cells: one lane per grid line. There are {@value
     * TrafficCoordinator#INTERSECTIONS} vertical and {@value TrafficCoordinator#INTERSECTIONS}
     * horizontal roads, so an intersection sits at every crossing of a vertical and a horizontal
     * road. Participates in: FR-007, SC-004.
     *
     * @param g2 the graphics context
     * @param geo the geometry
     */
    private void drawRoads(final Graphics2D g2, final GridGeometry geo) {
      final int cell = geo.cell();
      final int lane = geo.lane();
      final int marking = geo.marking();
      final int offsetX = geo.offsetX();
      final int offsetY = geo.offsetY();
      final int gridW = cell * TrafficCoordinator.GRID_SIZE;
      final int gridH = cell * TrafficCoordinator.GRID_SIZE;

      g2.setColor(LightColors.ROAD);
      for (int i = 0; i < TrafficCoordinator.INTERSECTIONS; i++) {
        final int x = offsetX + i * cell - lane / 2;
        g2.fillRect(x, offsetY, lane, gridH);
      }
      for (int j = 0; j < TrafficCoordinator.INTERSECTIONS; j++) {
        final int y = offsetY + j * cell - lane / 2;
        g2.fillRect(offsetX, y, gridW, lane);
      }
      // Fill the four outer corner squares so the outer roads connect without leaving a dark gap.
      g2.fillRect(offsetX - lane / 2, offsetY - lane / 2, lane, lane);
      g2.fillRect(offsetX + gridW - lane / 2, offsetY - lane / 2, lane, lane);
      g2.fillRect(offsetX - lane / 2, offsetY + gridH - lane / 2, lane, lane);
      g2.fillRect(offsetX + gridW - lane / 2, offsetY + gridH - lane / 2, lane, lane);

      g2.setColor(LightColors.LANE_MARKING);
      for (int i = 0; i < TrafficCoordinator.INTERSECTIONS; i++) {
        final int x = offsetX + i * cell - marking / 2;
        g2.fillRect(x, offsetY, marking, gridH);
      }
      for (int j = 0; j < TrafficCoordinator.INTERSECTIONS; j++) {
        final int y = offsetY + j * cell - marking / 2;
        g2.fillRect(offsetX, y, gridW, marking);
      }
    }

    /**
     * Draws a small circle for each approach of every intersection. A light is drawn only where a
     * road continues from that side (there is no road coming from outside the grid), so an interior
     * intersection has four lights, an edge (non-corner) intersection has three, and a corner has
     * none. Each light sits on the right side of its approach lane in the direction of travel, not
     * in the middle of the lane. Participates in: FR-007, SC-004.
     *
     * @param g2 the graphics context
     * @param state the state to draw
     * @param geo the geometry
     */
    private void drawLights(final Graphics2D g2, final GridState state, final GridGeometry geo) {
      final int cell = geo.cell();
      final int lane = geo.lane();
      final int radius = geo.lightRadius();
      final int diameter = radius * 2;
      final int offsetX = geo.offsetX();
      final int offsetY = geo.offsetY();
      final int last = TrafficCoordinator.INTERSECTIONS - 1;
      for (int row = 0; row < TrafficCoordinator.INTERSECTIONS; row++) {
        for (int col = 0; col < TrafficCoordinator.INTERSECTIONS; col++) {
          if (isCorner(row, col, last)) {
            continue;
          }
          final int cx = offsetX + col * cell;
          final int cy = offsetY + row * cell;
          if (row > 0) {
            drawLight(
                g2,
                state,
                row,
                col,
                true,
                cx - lane / 2 - radius,
                cy - lane / 2 - radius,
                diameter);
          }
          if (row < last) {
            drawLight(
                g2,
                state,
                row,
                col,
                true,
                cx + lane / 2 + radius,
                cy + lane / 2 + radius,
                diameter);
          }
          if (col > 0) {
            drawLight(
                g2,
                state,
                row,
                col,
                false,
                cx - lane / 2 - radius,
                cy + lane / 2 + radius,
                diameter);
          }
          if (col < last) {
            drawLight(
                g2,
                state,
                row,
                col,
                false,
                cx + lane / 2 + radius,
                cy - lane / 2 - radius,
                diameter);
          }
        }
      }
    }

    /**
     * Returns whether the given intersection is one of the four corners. Participates in: FR-007,
     * SC-004.
     *
     * @param row the row
     * @param col the column
     * @param last the index of the last row/column
     * @return {@code true} if the intersection is a corner
     */
    private static boolean isCorner(final int row, final int col, final int last) {
      return (row == 0 || row == last) && (col == 0 || col == last);
    }

    /**
     * Draws a single light circle for the given approach. Participates in: FR-007, SC-004.
     *
     * @param g2 the graphics context
     * @param state the state to read the light from
     * @param row the row
     * @param col the column
     * @param vertical whether the light is on a north-south approach
     * @param lightX the circle x position
     * @param lightY the circle y position
     * @param diameter the circle diameter
     */
    private void drawLight(
        final Graphics2D g2,
        final GridState state,
        final int row,
        final int col,
        final boolean vertical,
        final int lightX,
        final int lightY,
        final int diameter) {
      final Color color = LightColors.of(state.lightState(row, col, vertical));
      g2.setColor(color);
      g2.fillOval(lightX, lightY, diameter, diameter);
    }

    /**
     * Draws each vehicle as a circle whose diameter reflects its physical width, scaled to pixels
     * by {@link GridGeometry}, at its current position along its segment. A vehicle travels from
     * one intersection to the next along a road; it drives in the right half of its lane in the
     * direction of travel (offset by a quarter of the lane width to the right), not in the middle
     * of the road, and its position is clamped to the grid bounds. Participates in: FR-007, SC-004.
     *
     * @param g2 the graphics context
     * @param state the state to draw
     * @param geo the geometry
     */
    private void drawVehicles(final Graphics2D g2, final GridState state, final GridGeometry geo) {
      final int diameter = geo.vehicleDiameter();
      final int half = geo.lane() / 2;
      final int cell = geo.cell();
      final int offsetX = geo.offsetX();
      final int offsetY = geo.offsetY();
      final int minX = offsetX - half;
      final int maxX = offsetX + cell * TrafficCoordinator.GRID_SIZE + half;
      final int minY = offsetY - half;
      final int maxY = offsetY + cell * TrafficCoordinator.GRID_SIZE + half;
      final double lateral = geo.lateral();
      final double pixelsPerMetre = geo.pixelsPerMetre();
      g2.setColor(LightColors.VEHICLE);
      for (final VehicleView vehicle : state.getVehicles()) {
        final Direction direction = vehicle.getDirection();
        final double distancePx = vehicle.getDistanceInSegment() * pixelsPerMetre;
        double cx =
            offsetX
                + vehicle.getCol() * cell
                + direction.colDelta() * distancePx
                - direction.rowDelta() * lateral;
        double cy =
            offsetY
                + vehicle.getRow() * cell
                + direction.rowDelta() * distancePx
                + direction.colDelta() * lateral;
        cx = Math.max(minX, Math.min(maxX, cx));
        cy = Math.max(minY, Math.min(maxY, cy));
        g2.fillOval(
            (int) Math.round(cx - diameter / 2.0),
            (int) Math.round(cy - diameter / 2.0),
            diameter,
            diameter);
      }
    }
  }
}
