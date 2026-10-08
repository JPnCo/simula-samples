# Implementation Plan: Traffic Light Grid Sample

**Branch**: `001-trafficlight` | **Date**: 2026-09-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-trafficlight/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command; its definition describes the execution workflow.

## Summary

Deliver a self-contained, runnable **traffic-light grid sample** under
`fr.jpnco.simula.samples.trafficlight` that demonstrates the simula framework's
actor model. The sample models a bounded grid of intersections with traffic
lights and a fixed fleet of circulating vehicles, supports console and
graphical displays and two execution modes with a deterministic (identical)
outcome, and is documented by a current per-sample architecture document in
`docs/`.

The central technical decision: the sample must be **self-contained** — it
implements its own actors and state types on top of the framework **core** API
(`fr.jpnco.simula.*`), and must not depend on the framework's
`fr.jpnco.simula.examples.trafficlight` package. The current local copy wrongly
imports those `examples` classes (making the local code dead/duplicated); the
plan re-wires it to use only its own `samples` classes.

## Technical Context

**Language/Version**: Java 25 (`maven.compiler.source`/`target` 25)

**Primary Dependencies**: `fr.jpnco.simula:simula-core:0.0.1-SNAPSHOT` (framework, installed in
local `.m2`); JUnit Jupiter 5.14 and Mockito 5.22 (test scope)

**Storage**: N/A — the simulation is in-memory; no persistence.

**Testing**: JUnit 5 + Mockito, run by Maven Surefire (`mvn test`).

**Target Platform**: JVM (desktop-capable; Swing GUI optional).

**Project Type**: library + runnable demo (sample for the simula framework).

**Performance Goals**: real-time GUI rendering at a ~100 ms repaint cadence; one
simulated tick per simulated second.

**Constraints**: seeded, reproducible randomness; identical final outcome across
execution modes; no network or persistence.

**Scale/Scope**: a single sample — a 3x3 grid, 12 vehicles, 12 traffic lights,
2 displays, 2 execution modes.

## Key Decisions

- **Self-contained sample**: the sample implements its own actors and state types on top of the
  framework **core** API (`fr.jpnco.simula.*`) and does not depend on the framework's
  `fr.jpnco.simula.examples.trafficlight` package (Constitution Principle IX).
- **Barrier-driven report synchronization**: the coordinator does not count incoming reports
  manually. Two simula `Barrier` actors (in `CYCLIC` mode, distinct-source counting) subscribe to
  `traffic-light-state` and `vehicles-state`; when all 12 lights (respectively all 12 vehicles) of
  a tick have reported, they fire `lights-ready` (respectively `vehicles-ready`). The coordinator
  assembles and broadcasts `new-state` only after it has seen **both** completion signals, pulling
  each actor's last-reported state (each actor stores its state before broadcasting). This
  replaces the former counter-based grouping and keeps the tick grouping correct under the
  framework's asynchronous per-actor event delivery.
- **Determinism**: vehicles decide on the **previous tick's** light table and all randomness is
  seeded from `RANDOM_SEED`, so the same scenario yields an identical outcome in both execution
  modes (FR-008, FR-009, SC-003).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **G1 (Principle I — Test-First, NON-NEGOTIABLE)**: every sample behavior MUST
  be covered by tests written before the implementation. **Status**: PASS — the
  plan schedules unit tests for the pure logic (light state, vehicle movement,
  direction, grid geometry) before wiring the demo.
- **G2 (Principle II — Coverage ≥97%)**: the sample is a deliverable and MUST
  satisfy the 97% line and branch coverage thresholds (no sample exemption).
  **Status**: PASS — the plan schedules unit tests and a coverage tool must
  report ≥97% line and branch coverage before merge.
- **G3 (Principles III/IV — English code & comments)**: all source, comments and
  identifiers in English. **Status**: PASS — existing sample code is English.
- **G4 (Principle VI — Documentation)**: Javadoc on every package, class and
  method. **Status**: PASS — existing code has Javadoc; any new/modified code
  MUST keep it.
- **G5 (Principle VII — Named Literals)**: no magic literals except `-1`, `0`,
  `1`. **Status**: PASS — existing code uses named constants.
- **G6 (Principle VIII — Root Architecture Document)**: a current `architecture.md`
  at the repo root. **Status**: OBSERVATION — no root `architecture.md` exists
  yet; outside this feature's scope but flagged for follow-up.
- **G7 (Principle IX — Sample Architecture Document)**: every sub-package of
  `fr.jpnco.simula.samples` MUST have a current architecture document in `docs/`
  with no historical information. **Status**: **VIOLATION to fix** — the existing
  `docs/trafficligth-architecture.md` describes `fr.jpnco.simula.examples.trafficlight`
  and must be updated to reflect the current `fr.jpnco.simula.samples.trafficlight`
  architecture.

## Project Structure

### Documentation (this feature)

```text
specs/001-trafficlight/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── trafficlight-cli.md
└── tasks.md             # /speckit.tasks output (NOT created here)
```

### Source Code (repository root)

```text
src/main/java/fr/jpnco/simula/samples/trafficlight/
├── TrafficLightDemo.java        # runnable entry point (main)
├── actors/
│   ├── Topics.java
│   ├── TrafficCoordinator.java
│   ├── CrossingTrafficLight.java
│   ├── Vehicle.java
│   ├── TrafficMonitor.java
│   └── TrafficLightGui.java
└── states/
    ├── Direction.java
    ├── GridState.java
    ├── LightState.java
    ├── TrafficLightState.java
    ├── VehicleState.java
    └── VehicleView.java

src/test/java/fr/jpnco/simula/samples/trafficlight/
├── states/
│   ├── DirectionTest.java
│   ├── LightStateTest.java
│   ├── GridStateTest.java
│   └── ... (one test per pure-logic unit)
└── actors/
    ├── CrossingTrafficLightTest.java
    ├── VehicleTest.java
    └── TrafficCoordinatorTest.java

docs/
└── trafficligth-architecture.md   # per-sample architecture document (Principle IX)
```

**Structure Decision**: single Maven project; the sample lives under
`src/main/java/fr/jpnco/simula/samples/trafficlight/` (actors + states sub-packages),
with mirrored unit tests under `src/test/java/...`. The sample depends only on
the framework core API, not on the framework's `examples` package. The
per-sample architecture document is maintained in `docs/` per Principle IX.

## Complexity Tracking

> Filled because Constitution Check has accepted deviations / observations.

| Violation / Deviation | Why Needed | Simpler Alternative Rejected Because |
|-----------------------|------------|--------------------------------------|
| G6 — root `architecture.md` absent (observation) | Whole-project doc, out of this feature's scope | Adding it here would expand scope beyond the trafficlight sample; tracked separately |
