package fr.jpnco.simula.samples.trafficlight.actors;

/**
 * The event topics shared by the traffic-light simulation actors.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-003, FR-006, FR-007.
 */
public final class Topics {

  /** Asks every {@link CrossingTrafficLight} to compute and broadcast its future state. */
  public static final String NEXT_TRAFFIC_LIGHT_STATES = "next-traffic-light-states";

  /** Asks every {@link Vehicle} to compute and broadcast its future state. */
  public static final String NEXT_VEHICLES_STATES = "next-vehicles-states";

  /** Broadcast by a {@link CrossingTrafficLight} carrying its new state. */
  public static final String TRAFFIC_LIGHT_STATE = "traffic-light-state";

  /** Broadcast by a {@link Vehicle} carrying its new state. */
  public static final String VEHICLES_STATE = "vehicles-state";

  /**
   * Broadcast by the lights {@link fr.jpnco.simula.actors.Barrier} once every light of the tick has
   * reported its state, telling the coordinator the light reports are complete.
   */
  public static final String LIGHTS_READY = "lights-ready";

  /**
   * Broadcast by the vehicles {@link fr.jpnco.simula.actors.Barrier} once every vehicle of the tick
   * has reported its state, telling the coordinator the vehicle reports are complete.
   */
  public static final String VEHICLES_READY = "vehicles-ready";

  /** Broadcast by the coordinator carrying the assembled snapshot. */
  public static final String NEW_STATE = "new-state";

  /** Private constructor to prevent instantiation of this constant holder. */
  private Topics() {}
}
