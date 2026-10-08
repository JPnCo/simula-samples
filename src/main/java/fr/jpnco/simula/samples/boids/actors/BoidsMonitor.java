package fr.jpnco.simula.samples.boids.actors;

import fr.jpnco.simula.Actor;
import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.engine.ActorDelegate;
import fr.jpnco.simula.engine.IdBuilder;
import fr.jpnco.simula.samples.boids.states.FlockState;

/**
 * The console display actor for the demo: it subscribes to {@link Topics#NEW_STATE} and prints the
 * received flock state to {@link System#out}.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-005, SC-001, SC-002.
 */
public final class BoidsMonitor implements Actor {

  /** The actor delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The unique identity of the monitor actor. */
  private final Integer id;

  /**
   * Creates a console display actor that subscribes to the {@link Topics#NEW_STATE} topic.
   * Participates in: FR-005, SC-001, SC-002.
   *
   * @param engine the engine this actor lives on
   */
  public BoidsMonitor(final Engine engine) {
    id = IdBuilder.nextId();
    delegate = ActorDelegate.createDelegate(engine, this);
    engine.subscribe(this, Topics.NEW_STATE);
  }

  /**
   * Returns the actor delegate that drives this actor. Participates in: FR-005, SC-001, SC-002.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this actor. Participates in: FR-005, SC-001, SC-002.
   *
   * @return the id
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Processes a received event: on {@link Topics#NEW_STATE} it renders the {@link FlockState} to
   * {@link System#out}. Participates in: FR-005, SC-001, SC-002.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    if (Topics.NEW_STATE.equals(event.getTopic())) {
      System.out.println(render((FlockState) event.getParameters()[0]));
    }
  }

  /**
   * Renders the flock state as a single console line starting with the simulated time. Participates
   * in: FR-005, SC-001, SC-002.
   *
   * @param state the state to render
   * @return the rendered line
   */
  static String render(final FlockState state) {
    return "t="
        + state.getSimTime()
        + " boids="
        + state.getBoidCount()
        + " world="
        + state.getWorldWidth()
        + "x"
        + state.getWorldHeight();
  }
}
