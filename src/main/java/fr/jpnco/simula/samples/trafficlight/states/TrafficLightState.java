package fr.jpnco.simula.samples.trafficlight.states;

/**
 * An immutable report of one intersection's light bands at one simulated tick, broadcast by a
 * {@link fr.jpnco.simula.samples.trafficlight.actors.CrossingTrafficLight} on the {@link
 * fr.jpnco.simula.samples.trafficlight.actors.Topics#TRAFFIC_LIGHT_STATE} topic.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-006.
 */
public final class TrafficLightState {

  /** The simulated tick this state belongs to (used to detect out-of-order events). */
  private final int tick;

  /** The row of the intersection. */
  private final int row;

  /** The column of the intersection. */
  private final int col;

  /** The north-south band state. */
  private final LightState ns;

  /** The east-west band state. */
  private final LightState ew;

  /**
   * Creates an immutable light report.
   *
   * @param tick the simulated tick
   * @param row the row of the intersection
   * @param col the column of the intersection
   * @param ns the north-south band state
   * @param ew the east-west band state
   */
  public TrafficLightState(
      final int tick, final int row, final int col, final LightState ns, final LightState ew) {
    this.tick = tick;
    this.row = row;
    this.col = col;
    this.ns = ns;
    this.ew = ew;
  }

  /**
   * Returns the simulated tick this state belongs to. Participates in: FR-002, FR-006.
   *
   * @return the tick
   */
  public int getTick() {
    return tick;
  }

  /**
   * Returns the row of the intersection. Participates in: FR-002, FR-006.
   *
   * @return the row
   */
  public int getRow() {
    return row;
  }

  /**
   * Returns the column of the intersection. Participates in: FR-002, FR-006.
   *
   * @return the column
   */
  public int getCol() {
    return col;
  }

  /**
   * Returns the north-south band state. Participates in: FR-002, FR-006.
   *
   * @return the north-south state
   */
  public LightState getNs() {
    return ns;
  }

  /**
   * Returns the east-west band state. Participates in: FR-002, FR-006.
   *
   * @return the east-west state
   */
  public LightState getEw() {
    return ew;
  }
}
