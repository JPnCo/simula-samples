# Implementation Plan: Dashboard Simula Sample

**Branch**: `003-dashboard` | **Date**: 2026-09-29 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/003-dashboard/spec.md`.

## Summary

Deliver a self-contained, runnable **dashboard sample** under
`fr.jpnco.simula.samples.dashboard` that attaches to an existing simulation
(either the traffic-light sample or the boids sample) as an external observer
actor and collects a **time series** from the simulation's `new-state` events —
one sample per tick, keyed by the simulated time carried by each immutable
snapshot. The dashboard supports a console summary, an optional Swing plot, and
an optional file export, and it works with either target simulation without
code changes.

The sample follows the same architecture pattern as the existing samples: a
`Dashboard` actor implements the framework `Actor` interface, subscribes to the
target sample's `new-state` topic on the **same root engine**, records the
aggregate metric from each received snapshot into an immutable `TimeSeries`, and
feeds the series to a console monitor, a Swing GUI, or a file exporter. It reuses
the existing samples' coordinators/actors to build the target simulation and
simply registers the dashboard alongside them before the engine starts.

## Technical Context

**Language/Version**: Java 25 (`maven.compiler.source`/`target` 25)

**Primary Dependencies**: `fr.jpnco.simula:simula-core:0.0.1-SNAPSHOT` (framework, installed in
local `.m2`); existing `fr.jpnco.simula.samples.trafficlight` and
`fr.jpnco.simula.samples.boids` sample packages (same project); JUnit Jupiter 5.14
and Mockito 5.22 (test scope)

**Storage**: Optional local file export (one record per tick); the time series is
otherwise in-memory.

**Testing**: JUnit 5 + Mockito, run by Maven Surefire (`mvn test`). JaCoCo ≥97%
line and branch coverage gate (Constitution Principle II) enforced on `verify`;
`fmt-maven-plugin` (google-java-format) enforced on `verify`.

**Target Platform**: JVM (desktop-capable; Swing GUI optional).

**Project Type**: library + runnable demo (sample for the simula framework).

**Performance Goals**: real-time GUI plotting at a ~100 ms repaint cadence; one
sample recorded per simulated tick (one tick per simulated second).

**Constraints**: the dashboard MUST NOT modify the target simulation's actors or
topics; it MUST work with both target samples unchanged; the `SimulaSupervisor`
actor does not exist in the framework, so the dashboard is a self-contained
observer actor following the existing `*Monitor`/`*Gui` pattern.

## Key Decisions

- **Self-contained observer actor**: the dashboard is an actor that subscribes to
  the target sample's `new-state` topic on the same root engine. This is the
  exact pattern already proven by `TrafficMonitor`/`BoidsMonitor`/`BoidsGui` and
  requires no framework change. The dashboard does **not** rely on a
  `SimulaSupervisor` class (which does not exist in the installed framework) and
  does not depend on the framework's `examples` package (Constitution Principle IX).
- **Reuse the target simulation**: the dashboard demo builds the target
  simulation on its own root engine using the existing sample's coordinator and
  actors (`coordinator.seed()` + `registerAndStart`), then registers the
  `Dashboard` actor before `root.start()`. Because all samples live in the same
  Maven project, the dashboard package imports the target sample's actors. This
  avoids duplicating the simulation logic and keeps the dashboard generic.
- **Payload-agnostic sampling**: the dashboard reads the immutable snapshot from
  `Event.getParameters()[0]` and records the aggregate metric it exposes through a
  small extraction step, so the same dashboard works for `GridState` (vehicle
  count) and `FlockState` (boid count) without modification.
- **Immutable time series**: samples are immutable records keyed by simulated
  time; the `TimeSeries` is an append-only collection consumed by the monitor,
  GUI, and exporter. This makes the sampling logic trivially unit-testable to
  reach the 97% coverage gate.
- **Separate display/exporter actors-adjacent classes**: console rendering,
  Swing plotting, and CSV export are isolated from the core sampling logic so the
  pure sampling and data structures can be unit-tested independently of Swing.
- **CLI consistent with existing samples**: `mode`, `target` (trafficlight/boids),
  `display` (console/gui), and `export` (optional file path) are parsed case
  insensitively and order independently, with unknown tokens ignored.

## Project Structure

### Documentation (this feature)

```text
specs/003-dashboard/
├── plan.md              # This file
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── dashboard-topics.md
│   └── dashboard-cli.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/fr/jpnco/simula/samples/dashboard/
├── DashboardDemo.java        # runnable entry point (main)
├── DashboardCli.java         # CLI parsing (mode, target, display, export)
├── DashboardParameters.java  # immutable runnable parameters
├── actors/
│   ├── Dashboard.java        # observer actor: subscribes to new-state, records samples
│   ├── DashboardMonitor.java # console display (per-tick + summary)
│   └── DashboardGui.java     # Swing plot (JFrame + GridPanel + Timer)
├── model/
│   ├── Sample.java           # immutable time-series record (simTime, value)
│   ├── TimeSeries.java       # immutable append-only series
│   └── MetricExtractor.java  # extracts aggregate metric from a snapshot
└── export/
    └── CsvExporter.java      # writes the series to a CSV file

src/test/java/fr/jpnco/simula/samples/dashboard/
├── DashboardCliTest.java
├── DashboardParametersTest.java
├── model/
│   ├── SampleTest.java
│   ├── TimeSeriesTest.java
│   └── MetricExtractorTest.java
├── actors/
│   ├── DashboardTest.java
│   └── DashboardMonitorTest.java
└── export/
    └── CsvExporterTest.java

docs/
└── dashboard-architecture.md  # per-sample architecture document (Principle IX)
```

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **G1 (Principle I — Test-First, NON-NEGOTIABLE)**: every sample behavior MUST
  be covered by tests written before the implementation. **Status**: PASS — the
  plan schedules unit tests for the pure data structures (`Sample`, `TimeSeries`,
  `MetricExtractor`) and the `Dashboard` actor before wiring the demo.
- **G2 (Principle II — Coverage ≥97%)**: the sample is a deliverable and MUST
  satisfy the 97% line and branch coverage thresholds. **Status**: PASS — the
  plan schedules unit tests and the JaCoCo gate must report ≥97% before merge;
  the `DashboardGui` and `DashboardDemo` classes are added to the JaCoCo excludes
  (as are the existing GUI/demo classes).
- **G3 (Principles III/IV — English code & comments)**: all source, comments and
  identifiers in English. **Status**: PASS.
- **G4 (Principle VI — Documentation)**: Javadoc on every package, class and
  method with FR/SC citations. **Status**: PASS — the plan keeps Javadoc on all
  new code.
- **G5 (Principle VII — Named Literals)**: no magic literals except `-1`, `0`,
  `1`. **Status**: PASS — all constants (e.g. the `new-state` topic, default
  duration, export header) are named.
- **G6 (Principle VIII — Root Architecture Document)**: a current `architecture.md`
  at the repo root must be updated to include the new sample. **Status**: PASS —
  the plan updates `architecture.md`.
- **G7 (Principle IX — Sample Architecture Document)**: every sub-package of
  `fr.jpnco.simula.samples` MUST have a current architecture document in `docs/`.
  **Status**: PASS — the plan creates `docs/dashboard-architecture.md`.

## Complexity Tracking

> Filled because Constitution Check has accepted deviations / observations.

| Violation / Deviation | Why Needed | Simpler Alternative Rejected Because |
|-----------------------|------------|--------------------------------------|
| The dashboard sample imports the existing trafficlight/boids sample actors | Reuses the proven simulation coordinators/actors instead of duplicating simulation logic; keeps the dashboard generic across both targets | Rewiring the existing demos to be injectable would couple the demos to the dashboard and break the existing samples' self-containment |
