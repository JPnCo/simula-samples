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
 * The observer actor that attaches to an existing simulation and collects a time series (FR-002,
 * FR-003).
 *
 * <p>It subscribes to the {@code new-state} topic of a running simulation on the same root engine
 * (the topic string is supplied at construction, so it works for both the traffic-light sample and
 * the boids sample). On every received {@code new-state} event it converts the snapshot payload
 * into a {@link Sample} via {@link MetricExtractor} and appends it to its current {@link
 * TimeSeries}, which it exposes through a {@code volatile} field so other threads (the console
 * monitor, the GUI or the exporter) can read a consistent snapshot.
 *
 * <p>This class is demonstration code under the {@code samples} package; it is not part of the
 * framework contract; per the project constitution it MUST satisfy the coverage gate.
 *
 * <p>Implements: FR-002, FR-003, FR-007, FR-008, SC-001, SC-002, SC-003.
 */
public final class Dashboard implements Actor {

  /** The actor delegate that runs this actor's event loop. */
  private final Actor delegate;

  /** The unique identity of the dashboard actor. */
  private final Integer id;

  /** The {@code new-state} topic the dashboard subscribes to. */
  private final String topic;

  /** The latest collected time series, replaced on every recorded sample. */
  private volatile TimeSeries series = TimeSeries.empty();

  /**
   * Creates an observer actor that subscribes to the given {@code new-state} topic. Participates
   * in: FR-002, FR-003, FR-007, FR-008, SC-001, SC-002, SC-003.
   *
   * @param engine the engine this actor lives on
   * @param topic the {@code new-state} topic of the target simulation
   */
  public Dashboard(final Engine engine, final String topic) {
    this.topic = topic;
    id = IdBuilder.nextId();
    delegate = ActorDelegate.createDelegate(engine, this);
    engine.subscribe(this, topic);
  }

  /**
   * Returns the actor delegate that drives this actor. Participates in: FR-002, FR-003, FR-007,
   * FR-008, SC-001, SC-002, SC-003.
   *
   * @return the delegate
   */
  @Override
  public Actor getDelegate() {
    return delegate;
  }

  /**
   * Returns the unique identity of this actor. Participates in: FR-002, FR-003, FR-007, FR-008,
   * SC-001, SC-002, SC-003.
   *
   * @return the id
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Processes a received event: on the subscribed {@code new-state} topic it records a sample for
   * the received snapshot; other topics are ignored. Participates in: FR-002, FR-003, FR-007,
   * FR-008, SC-001, SC-002, SC-003.
   *
   * @param event the event to process
   */
  @Override
  public void process(final Event event) {
    if (topic.equals(event.getTopic())) {
      final Sample sample = MetricExtractor.toSample(event.getParameters()[0]);
      series = series.append(sample);
    }
  }

  /**
   * Returns the latest collected time series. Participates in: FR-003, FR-004, FR-006, SC-001,
   * SC-002.
   *
   * @return the current series
   */
  public TimeSeries snapshot() {
    return series;
  }
}
