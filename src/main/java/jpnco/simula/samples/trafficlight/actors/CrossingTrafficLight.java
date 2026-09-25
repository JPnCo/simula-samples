package jpnco.simula.samples.trafficlight.actors;

import jpnco.simula.Actor;
import jpnco.simula.Engine;
import jpnco.simula.Event;
import jpnco.simula.engine.ActorDelegate;
import jpnco.simula.engine.EventImpl;
import jpnco.simula.samples.trafficlight.states.LightState;
import jpnco.simula.samples.trafficlight.states.TrafficLightState;

/**
 * An autonomous {@link Actor} representing the traffic light of one intersection. Each crossing
 * owns its own timing: its green and orange durations and a phase offset are fixed at construction,
 * so different intersections may have different periods. On each {@link
 * Topics#NEXT_TRAFFIC_LIGHT_STATES} event it computes the state of its two bands (north-south and
 * east-west) for the current tick and broadcasts it on {@link Topics#TRAFFIC_LIGHT_STATE}.
 *
 * <p>The four corners of the grid have no crossing traffic and are always green; they have no
 * {@code CrossingTrafficLight} actor.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-009.
 */
final class CrossingTrafficLight implements Actor {

  /** The actor delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The unique identity of the light. */
  private final Integer id;

  /** The row of the intersection. */
  private final int row;

  /** The column of the intersection. */
  private final int col;

  /** The green duration in seconds. */
  private final int greenDuration;

  /** The orange duration in seconds. */
  private final int orangeDuration;

  /** The number of seconds of one half-cycle (green + orange). */
  private final int halfCycle;

  /** The number of seconds of a full cycle (two half-cycles). */
  private final int cycle;

  /** The stagger offset in seconds. */
  private final int phaseOffset;

  /** The most recent state this light computed and broadcast, readable by the coordinator. */
  private TrafficLightState lastState;

  /**
   * Creates a traffic-light actor for the given intersection and subscribes it to {@link
   * Topics#NEXT_TRAFFIC_LIGHT_STATES}. Participates in: FR-002, FR-009.
   *
   * @param engine the engine this actor lives on
   * @param id the light identity
   * @param row the row of the intersection
   * @param col the column of the intersection
   * @param greenDuration the green duration in seconds
   * @param orangeDuration the orange duration in seconds
   * @param phaseOffset the stagger offset in seconds
   */
  CrossingTrafficLight(
      final Engine engine,
      final int id,
      final int row,
      final int col,
      final int greenDuration,
      final int orangeDuration,
      final int phaseOffset) {
    this.id = id;
    this.row = row;
    this.col = col;
    this.greenDuration = greenDuration;
    this.orangeDuration = orangeDuration;
    halfCycle = greenDuration + orangeDuration;
    cycle = 2 * halfCycle;
    this.phaseOffset = phaseOffset;
    delegate = ActorDelegate.createDelegate(engine, this);
    engine.subscribe(this, Topics.NEXT_TRAFFIC_LIGHT_STATES);
  }

  /**
   * Returns the actor delegate that drives this actor. Participates in: FR-002, FR-009.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this actor. Participates in: FR-002, FR-009.
   *
   * @return the id
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Processes a received event: on {@link Topics#NEXT_TRAFFIC_LIGHT_STATES} it computes this
   * intersection's band states for the tick and broadcasts them on {@link
   * Topics#TRAFFIC_LIGHT_STATE}. Participates in: FR-002, FR-009.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    if (Topics.NEXT_TRAFFIC_LIGHT_STATES.equals(event.getTopic())) {
      final int tick = (Integer) event.getParameters()[0];
      final LightState[] bands = stateAt(tick);
      lastState = new TrafficLightState(tick, row, col, bands[0], bands[1]);
      getEngine().signal(EventImpl.createEvent(Topics.TRAFFIC_LIGHT_STATE, this, lastState));
    }
  }

  /**
   * Returns the most recent state this light computed and broadcast, or {@code null} if it has not
   * computed one yet. The state is stored before it is broadcast, so once the lights {@link
   * jpnco.simula.actors.Barrier} has fired for a tick every light's stored state is current for
   * that tick and the coordinator can pull it to assemble the snapshot. Participates in: FR-002,
   * FR-009.
   *
   * @return the last computed state, may be {@code null}
   */
  TrafficLightState getLastState() {
    return lastState;
  }

  /**
   * Returns the two band states ({@code [north-south, east-west]}) of this intersection at the
   * given simulated time, computed deterministically from this crossing's own durations and phase
   * offset. Participates in: FR-002, FR-009.
   *
   * @param time the simulated time
   * @return the band states, index 0 = north-south, index 1 = east-west
   */
  private LightState[] stateAt(final int time) {
    final int phase = Math.floorMod(time + phaseOffset, cycle);
    final boolean nsTurn = phase < halfCycle;
    if (nsTurn) {
      final LightState ns = phase < greenDuration ? LightState.GREEN : LightState.ORANGE;
      return new LightState[] {ns, LightState.RED};
    }
    final int ewPhase = phase - halfCycle;
    final LightState ew = ewPhase < greenDuration ? LightState.GREEN : LightState.ORANGE;
    return new LightState[] {LightState.RED, ew};
  }
}
