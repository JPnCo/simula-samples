package fr.jpnco.simula.samples.dashboard.actors;

import fr.jpnco.simula.Actor;
import fr.jpnco.simula.Engine;
import fr.jpnco.simula.Event;
import fr.jpnco.simula.engine.ActorDelegate;
import fr.jpnco.simula.engine.IdBuilder;
import fr.jpnco.simula.samples.dashboard.model.MetricExtractor;
import fr.jpnco.simula.samples.dashboard.model.Sample;
import fr.jpnco.simula.samples.dashboard.model.TimeSeries;

/**
 * The console display of the dashboard: it subscribes to the {@code new-state} topic and prints a
 * per-tick line for every received snapshot (FR-004, SC-001, SC-002).
 *
 * <p>Like the {@link Dashboard} collector, it converts each snapshot into a {@link Sample} via
 * {@link MetricExtractor} and prints it. At the end of a run the caller invokes {@link
 * #printSummary} with the collected {@link TimeSeries} to print the final summary (sample count and
 * first/last simulated time), handling an empty series gracefully (FR-008).
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-004, FR-008, SC-001, SC-002.
 */
public final class DashboardMonitor implements Actor {

  /** The actor delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The unique identity of the monitor actor. */
  private final Integer id;

  /** The {@code new-state} topic the monitor subscribes to. */
  private final String topic;

  /**
   * Creates a console display actor that subscribes to the given {@code new-state} topic.
   * Participates in: FR-004, FR-008, SC-001, SC-002.
   *
   * @param engine the engine this actor lives on
   * @param topic the {@code new-state} topic of the target simulation
   */
  public DashboardMonitor(final Engine engine, final String topic) {
    this.topic = topic;
    id = IdBuilder.nextId();
    delegate = ActorDelegate.createDelegate(engine, this);
    engine.subscribe(this, topic);
  }

  /**
   * Returns the actor delegate that drives this actor. Participates in: FR-004, FR-008, SC-001,
   * SC-002.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this actor. Participates in: FR-004, FR-008, SC-001, SC-002.
   *
   * @return the id
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Processes a received event: on the subscribed {@code new-state} topic it prints a per-tick line
   * for the received snapshot; other topics are ignored. Participates in: FR-004, FR-008, SC-001,
   * SC-002.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    if (topic.equals(event.getTopic())) {
      final Sample sample = MetricExtractor.toSample(event.getParameters()[0]);
      System.out.println(renderLine(sample));
    }
  }

  /**
   * Renders a single sample as a console line starting with the simulated time. Participates in:
   * FR-004, SC-001.
   *
   * @param sample the sample to render
   * @return the rendered line
   */
  static String renderLine(final Sample sample) {
    return "t=" + sample.getSimTime() + " value=" + sample.getValue();
  }

  /**
   * Prints the final summary of the collected series: the sample count and the first and last
   * simulated times. An empty series prints a zero-count summary rather than failing (FR-008).
   * Participates in: FR-004, FR-008, SC-002.
   *
   * @param series the collected series
   */
  public void printSummary(final TimeSeries series) {
    final StringBuilder out = new StringBuilder("samples=").append(series.size()).append(", ");
    if (series.isEmpty()) {
      out.append("first=none, last=none");
    } else {
      out.append("first=").append(series.first().getSimTime());
      out.append(", last=").append(series.last().getSimTime());
    }
    System.out.println(out);
  }
}
