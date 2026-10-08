# Feature Specification: Dashboard Simula Sample

**Feature Branch**: `003-dashboard`

**Created**: 2026-09-29

**Status**: Draft

**Input**: User request — "dashboard simula": a dashboard that attaches to an existing simulation (trafficlight or boids) and collects time series.

## Clarifications

### Session 2026-09-29

- Q: The feature was originally described as being based on a framework actor called `SimulaSupervisor`. Does this actor exist? → A: Research confirms `SimulaSupervisor`, `Supervisor`, and `SupervisorView` **do not exist** in the installed framework (`fr.jpnco.simula:simula-core:0.0.1-SNAPSHOT`) nor in any sample. The dashboard must therefore be implemented as a self-contained observer actor inside the samples project, following the existing `*Monitor`/`*Gui` pattern, and must NOT depend on a framework `examples` package (Constitution).
- Q: Which simulation should the dashboard attach to? → A: The dashboard is generic: it attaches to an existing simulation by subscribing to the shared `new-state` topic, which both the trafficlight sample (`GridState`) and the boids sample (`FlockState`) publish once per tick. The dashboard records whatever immutable snapshot payload it receives, so it works with either sample.
- Q: What is a "time series" in this context? → A: One recorded sample per simulation tick, keyed by the simulated time carried by each snapshot (`getSimTime()`), holding the aggregate metrics exposed by the snapshot (e.g. vehicle count, boid count). No replay or interpolation is required.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Attach a dashboard and collect a time series (Priority: P1)

As a developer evaluating the simula framework, I want to attach a dashboard to
a running simulation (trafficlight or boids) so that I can observe how an
aggregate metric evolves over simulated time.

**Why this priority**: This is the core value of the sample: demonstrating that
an external observer actor can hook onto a live simulation, subscribe to its
`new-state` events, and build a time series one sample per tick. Without this,
the dashboard has no purpose.

**Independent Test**: Can be fully tested by launching an existing simulation in
console mode with the dashboard attached, letting it run for a bounded number of
simulated seconds, and verifying the dashboard prints one record per tick with a
monotonically increasing simulated time and the expected aggregate value.

**Acceptance Scenarios**:

1. **Given** a simulation running in console mode with the dashboard attached,
   **When** each simulation tick produces a new state, **Then** the dashboard
   records one sample keyed by the tick's simulated time.
2. **Given** a bounded console run, **When** the configured simulated duration is
   reached, **Then** the dashboard prints a summary of the collected series (sample
   count, first/last simulated time) and the run terminates cleanly.
3. **Given** the dashboard attached to either the trafficlight or boids sample,
   **When** the simulation runs, **Then** the dashboard records the aggregate
   metric from the corresponding snapshot (vehicle count or boid count) without
   requiring any change to the dashboard.

---

### User Story 2 - Render the time series in a window (Priority: P2)

As a developer, I want to see the collected time series plotted in a graphical
window so that I can visually inspect the evolution of a metric over time.

**Why this priority**: A live chart adds strong visual value and demonstrates
that an observer actor can drive a Swing rendering loop, but the console time
series already delivers the core value, so it is secondary.

**Independent Test**: Can be tested independently by launching a simulation with
the dashboard's graphical display requested and verifying a window opens that
plots the collected samples and updates over time until closed.

**Acceptance Scenarios**:

1. **Given** the graphical display is requested, **When** the simulation starts,
   **Then** a window opens that plots the collected time series.
2. **Given** a running graphical display, **When** the simulation produces new
   states, **Then** the plot updates to include the latest samples.
3. **Given** a running graphical display, **When** the user closes the window,
   **Then** the simulation stops cleanly.

---

### User Story 3 - Export the time series to a file (Priority: P3)

As a developer, I want to save the collected time series to a machine-readable
file so that I can analyse it with external tooling.

**Why this priority**: Export enables offline analysis and is a useful extension,
but it is not required to demonstrate the core attach-and-collect value, so it is
secondary.

**Independent Test**: Can be tested independently by running a bounded console
simulation with export enabled and verifying that a file is written containing
one line per tick with the simulated time and the aggregate value.

**Acceptance Scenarios**:

1. **Given** export is enabled for a bounded run, **When** the run completes,
   **Then** a file is written containing one record per collected tick.
2. **Given** a freshly written export file, **When** it is read back, **Then**
   each line carries the simulated time and the aggregate value in a consistent,
   documented format.
3. **Given** an empty run with no states collected, **When** export is attempted,
   **Then** the sample reports that nothing was exported rather than writing a
   malformed file.

---

### Edge Cases

- What happens if the simulation produces no `new-state` events? The dashboard
  records no samples and reports an empty series instead of failing.
- What happens when a snapshot carries a simulated time equal to a previously
  recorded one? The dashboard records it as a distinct sample; no deduplication
  is performed because ticks are the natural sampling unit.
- What happens when the dashboard is attached to an unknown or unsupported
  simulation? It still subscribes to `new-state` and records whatever snapshot it
  receives; it does not depend on a specific payload type beyond the immutable
  snapshot interface.
- What happens if the user requests both a window and an export? Both outputs
  operate on the same collected series and do not conflict.
- What happens when the simulation runs unbounded (GUI mode)? The dashboard
  keeps recording and rendering until the simulation stops; it does not impose a
  duration of its own.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The sample MUST provide a runnable entry point that attaches a
  dashboard to an existing simulation (trafficlight or boids) without requiring
  any external setup beyond the framework and the existing sample.
- **FR-002**: The dashboard MUST subscribe to the `new-state` topic of the
  running simulation and MUST record one sample per tick from each received
  immutable snapshot.
- **FR-003**: Each recorded sample MUST be keyed by the simulated time exposed by
  the snapshot (`getSimTime()`) and MUST carry the aggregate metric exposed by
  that snapshot.
- **FR-004**: The dashboard MUST collect a bounded series that can be summarised
  in console mode, reporting the sample count and the first and last simulated
  times when the run ends.
- **FR-005**: The dashboard MUST support a graphical (windowed) display that
  plots the collected time series and updates in real time.
- **FR-006**: The dashboard MUST support exporting the collected time series to a
  machine-readable file, with one record per tick in a consistent, documented
  format.
- **FR-007**: The dashboard MUST work with both the trafficlight sample (using
  its `GridState` snapshot) and the boids sample (using its `FlockState`
  snapshot) without modification.
- **FR-008**: The sample MUST terminate cleanly on a bounded console run and MUST
  report an empty series (rather than fail) when no states are collected.
- **FR-009**: The sample MUST expose an entry point and CLI (display selection and
  export path) consistent with the existing samples' conventions, with sensible
  defaults for a console run.

### Key Entities *(include if feature involves data)*

- **Snapshot**: The immutable per-tick report published by an existing simulation
  on the `new-state` topic (a `GridState` for trafficlight or a `FlockState` for
  boids). It exposes the simulated time (`getSimTime()`) and one or more aggregate
  metrics (e.g. vehicle count, boid count).
- **Sample**: A single time-series record, keyed by the simulated time of a
  snapshot and holding the aggregate value recorded from that snapshot.
- **Time Series**: The ordered collection of samples recorded over a run, used as
  the basis for console summary, plotting, and export.
- **Dashboard**: The observer actor that attaches to an existing simulation,
  subscribes to `new-state`, and turns the received snapshots into the time
  series.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can attach the dashboard to an existing simulation and observe
  it record one sample per simulated second until the run stops at the configured
  duration.
- **SC-002**: A bounded console run always terminates and prints a summary of the
  collected series (sample count and first/last simulated time) without user
  intervention.
- **SC-003**: The same dashboard build records a time series from both the
  trafficlight and the boids sample without code changes.
- **SC-004**: The graphical display opens, plots the collected samples, and
  updates at a regular cadence to reflect the latest state.
- **SC-005**: Enabling export produces a file with one record per collected tick in
  a consistent, documented format that can be read back.
- **SC-006**: The sample achieves at least 97% line and branch coverage as
  measured by an automated coverage tool (Principle II applies to every sample).

## Assumptions

- The sample is demonstration code intended to showcase the framework; per the
  project constitution it is a deliverable that MUST satisfy the 97% line and
  branch coverage thresholds (Principle II).
- `SimulaSupervisor` and related supervision classes do not exist in the installed
  framework, so the dashboard is implemented as a self-contained observer actor
  inside the samples project, following the existing `*Monitor`/`*Gui` pattern. It
  MUST NOT depend on a framework `examples` package (Constitution).
- The dashboard attaches to an existing simulation as an external observer on the
  same root engine (via `subscribe` to `new-state`); it does not modify the
  simulation's own actors or topics.
- The sample ships as a sub-package of `fr.jpnco.simula.samples` and, per the
  constitution, MUST have its own architecture document in the `docs` directory
  reflecting its current architecture, and MUST be referenced from the root
  `architecture.md`.
- The dashboard is generic and payload-agnostic beyond the immutable snapshot; the
  aggregate metric recorded is whatever the snapshot exposes (e.g. vehicle count
  for trafficlight, boid count for boids).
- A console display is the default; the graphical display and the export are
  optional and activated on request.
- The default simulated duration for a console run is finite and bounded so the run
  terminates; in GUI mode the run is unbounded and stops when the window is closed.
- The simulation is single-user and local; there is no persistence, network, or
  multi-user requirement beyond the optional file export.
