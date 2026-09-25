package jpnco.simula.samples.trafficlight.actors;

import jpnco.simula.Actor;
import jpnco.simula.Engine;
import jpnco.simula.Event;
import jpnco.simula.engine.ActorDelegate;
import jpnco.simula.engine.IdBuilder;
import jpnco.simula.samples.trafficlight.states.GridState;

/**
 * The console display actor for the demo: it subscribes to {@link Topics#NEW_STATE} and prints the
 * received grid state to {@link System#out}.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-006, SC-001, SC-002.
 */
public final class TrafficMonitor implements Actor {

  /** The actor delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The unique identity of the monitor actor. */
  private final Integer id;

  /**
   * Creates a console display actor that subscribes to the {@link Topics#NEW_STATE} topic.
   *
   * @param engine the engine this actor lives on
   */
  public TrafficMonitor(final Engine engine) {
    id = IdBuilder.nextId();
    delegate = ActorDelegate.createDelegate(engine, this);
    engine.subscribe(this, Topics.NEW_STATE);
  }

  /**
   * Returns the actor delegate that drives this actor.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this actor.
   *
   * @return the id
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Processes a received event: on {@link Topics#NEW_STATE} it renders the {@link GridState} to
   * {@link System#out} using {@link MonitorRenderer}. Participates in: FR-006, SC-001, SC-002.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    if (Topics.NEW_STATE.equals(event.getTopic())) {
      System.out.println(MonitorRenderer.render((GridState) event.getParameters()[0]));
    }
  }
}
