# Research: Dashboard Simula Sample

## Attaching an observer to a running simulation

The simula framework exposes `Engine.subscribe(Actor, String)` and
`Engine.registerAndStart(Actor)`. Every existing display actor
(`TrafficMonitor`, `BoidsMonitor`, `TrafficLightGui`, `BoidsGui`) is exactly an
external observer: it is constructed with the root `Engine`, subscribes to a
topic in its constructor, and is registered on the same engine before
`root.start()`. The dashboard follows this identical pattern — no framework
change is required.

- **Decision**: implement the dashboard as an observer actor that subscribes to
  the target sample's `new-state` topic on the same root engine.
- **Rationale**: this is the only attach mechanism the framework provides, it is
  already proven by the existing displays, and it requires no modification to the
  framework or to the target simulation.
- **Alternatives considered**: a framework parent/child engine
  (`EngineImpl(name, parent)` + `addChild` + `signalToChildren`) exists but is
  unused by any sample; it adds a separate event loop and `TimeSource` with no
  benefit for this dashboard, so the simple observer-actor route was chosen.

## The `SimulaSupervisor` class does not exist

The feature was originally described as being based on a framework actor called
`SimulaSupervisor`. An exhaustive search of the installed framework jar
(`simula-0.0.1-SNAPSHOT.jar`), the samples jar, and all other local `jpnco` jars
found **no** `SimulaSupervisor`, `Supervisor`, `SupervisorView`, or `Dashboard`
class, and no `examples` package. There are also no `engine-stats`/`actor-stats`
topics in the framework.

- **Decision**: do not rely on `SimulaSupervisor`; build the dashboard as a
  self-contained observer actor inside the samples project.
- **Rationale**: the class is not available and depending on a hypothetical
  framework `examples` package is forbidden by the constitution (self-containment,
  Principle IX). The existing `*Monitor`/`*Gui` actors are the de-facto
  supervision pattern.
- **Alternatives considered**: evolving the framework to add a supervisor class —
  rejected because the framework is an external dependency and this feature is a
  sample, not a framework change.

## Sampling from `new-state`

Both samples publish a snapshot on the `new-state` topic once per tick, delivered
as `Event.getParameters()[0]`:

- traffic-light → `GridState` (`getSimTime()`, `getVehicleCount()`, ...)
- boids → `FlockState` (`getSimTime()`, `getBoidCount()`, ...)

Both snapshots expose `getSimTime()` and an aggregate count. The dashboard
extracts `simTime` and one aggregate metric through a small `MetricExtractor`,
which makes the sampling step payload-agnostic.

- **Decision**: record one `Sample(simTime, value)` per `new-state` event.
- **Rationale**: `new-state` is the single public snapshot topic common to both
  samples, delivered exactly once per tick, which matches the spec's "one sample
  per tick" requirement (FR-002, FR-003).
- **Alternatives considered**: subscribing to `Engine.TIME_EVENT` to count ticks —
  rejected because it carries no snapshot/metric; the dashboard needs the snapshot
  payload, so `new-state` is the correct topic.

## Determinism and time

Both demos create the root engine with `TIME_FACTOR = 1`, i.e. one tick (one
`new-state`) per simulated second. `getSimTime()` on each snapshot increments by
one per tick. The dashboard records `simTime` from the snapshot itself, so no
separate clock is needed and the recorded series is deterministic given the target
simulation's seed.

## Framework mapping

- `Dashboard` — observer actor (subscribe `new-state`, record samples).
- `TimeSeries` / `Sample` — immutable data model (pure, testable).
- `MetricExtractor` — payload-agnostic snapshot → metric.
- `DashboardMonitor` — console display (per-tick + summary).
- `DashboardGui` — Swing plot (JFrame + Timer, like `BoidsGui`).
- `CsvExporter` — optional file export.
- `DashboardCli` / `DashboardDemo` — entry point consistent with existing samples.
