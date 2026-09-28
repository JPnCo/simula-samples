# Implementation Plan: Boids Flocking Sample

**Branch**: `002-boids` | **Date**: 2026-09-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-boids/spec.md`.

## Summary

Deliver a self-contained, runnable **boids flocking sample** under
`jpnco.simula.samples.boids` that demonstrates the simula framework's actor model
with a large population of autonomous agents. The sample models a bounded 2D world
of boid agents that move according to the classic Reynolds flocking rules
(separation, alignment, cohesion), supports console and graphical displays and two
execution modes with a deterministic (identical) outcome, and is documented by a
per-sample architecture document in `docs/`.

The sample follows the same architecture pattern as the traffic-light sample: a
`BoidsCoordinator` actor pilots the ticks, owns the flock, and assembles immutable
`FlockState` snapshots; each `Boid` is an autonomous actor; a `Barrier` actor
synchronizes the per-tick reports. Each boid decides on the **previous tick's**
flock state so movement is deterministic regardless of report arrival order.

## Technical Context

**Language/Version**: Java 25 (`maven.compiler.source`/`target` 25)

**Primary Dependencies**: `jpnco:simula:0.0.1-SNAPSHOT` (framework, installed in
local `.m2`); JUnit Jupiter 5.14 and Mockito 5.22 (test scope)

**Storage**: N/A — the simulation is in-memory; no persistence.

**Testing**: JUnit 5 + Mockito, run by Maven Surefire (`mvn test`). JaCoCo ≥97%
line and branch coverage gate (Constitution Principle II) enforced on `verify`;
`fmt-maven-plugin` (google-java-format) enforced on `verify`.

**Target Platform**: JVM (desktop-capable; Swing GUI optional).

**Project Type**: library + runnable demo (sample for the simula framework).

**Performance Goals**: real-time GUI rendering at a ~100 ms repaint cadence; one
simulated tick per simulated second; a flock of 40 boids.

**Constraints**: seeded, reproducible randomness; identical final outcome across
execution modes; no network or persistence.

## Key Decisions

- **Self-contained sample**: the sample implements its own actors and state types
  on top of the framework **core** API (`jpnco.simula.*`) and does not depend on
  the framework's `examples` package (Constitution Principle IX).
- **Barrier-driven report synchronization**: the coordinator does not count
  incoming reports manually. A `Barrier` actor (in `CYCLIC` mode, distinct-source
  counting) subscribes to `boid-state`; when all boids of a tick have reported it
  fires `boids-ready`. The coordinator assembles and broadcasts `new-state` after
  seeing that completion signal, pulling each boid's last-reported state (each
  boid stores its state before broadcasting).
- **Determinism**: each boid decides on the **previous tick's** flock state and
  all randomness is seeded from `RANDOM_SEED`, so the same scenario yields an
  identical outcome in both execution modes (FR-007, FR-008, SC-003).
- **Pure flocking model**: the Reynolds rules are implemented in a pure,
  testable `BoidModel` class separate from the actor infrastructure, so the
  rules can be unit-tested to reach the 97% coverage gate.
- **Toroidal world**: the world is toroidal (wrap-around) — a boid reaching an
  edge reappears on the opposite edge, and neighbour distances are computed on
  the toroidal surface (no steering/clamping at the edges).
- **Configurable flocking parameters**: separation/alignment/cohesion weights,
  perception radius, maximum speed, world size, and boid count are configurable
  at runtime with sensible defaults (weights = 1.0 each, perception radius = 40,
  maximum speed = 4, world 800×600, 40 boids).

## Project Structure

### Documentation (this feature)

```text
specs/002-boids/
├── plan.md              # This file
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── boids-topics.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/jpnco/simula/samples/boids/
├── BoidsDemo.java        # runnable entry point (main)
├── actors/
│   ├── Topics.java
│   ├── BoidsCoordinator.java
│   ├── Boid.java
│   ├── BoidsMonitor.java
│   └── BoidsGui.java
└── states/
    ├── BoidModel.java    # pure flocking rules (separation/alignment/cohesion)
    ├── FlockState.java
    ├── BoidView.java
    └── BoidState.java

src/test/java/jpnco/simula/samples/boids/
├── states/
│   ├── BoidModelTest.java
│   ├── FlockStateTest.java
│   ├── BoidViewTest.java
│   └── BoidStateTest.java
└── actors/
    ├── BoidsCoordinatorTest.java
    ├── BoidTest.java
    └── BoidsMonitorTest.java

docs/
└── boids-architecture.md   # per-sample architecture document (Principle IX)
```

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **G1 (Principle I — Test-First, NON-NEGOTIABLE)**: every sample behavior MUST
  be covered by tests written before the implementation. **Status**: PASS — the
  plan schedules unit tests for the pure flocking logic and actors before wiring
  the demo.
- **G2 (Principle II — Coverage ≥97%)**: the sample is a deliverable and MUST
  satisfy the 97% line and branch coverage thresholds. **Status**: PASS — the
  plan schedules unit tests and the JaCoCo gate must report ≥97% before merge.
- **G3 (Principles III/IV — English code & comments)**: all source, comments and
  identifiers in English. **Status**: PASS.
- **G4 (Principle VI — Documentation)**: Javadoc on every package, class and
  method with FR/SC citations. **Status**: PASS — the plan keeps Javadoc on all
  new code.
- **G5 (Principle VII — Named Literals)**: no magic literals except `-1`, `0`,
  `1`. **Status**: PASS — all constants are named.
- **G6 (Principle VIII — Root Architecture Document)**: a current `architecture.md`
  at the repo root must be updated to include the new sample. **Status**: PASS —
  the plan updates `architecture.md`.
- **G7 (Principle IX — Sample Architecture Document)**: every sub-package of
  `jpnco.simula.samples` MUST have a current architecture document in `docs/`.
  **Status**: PASS — the plan creates `docs/boids-architecture.md`.

## Complexity Tracking

> Filled because Constitution Check has accepted deviations / observations.

| Violation / Deviation | Why Needed | Simpler Alternative Rejected Because |
|-----------------------|------------|--------------------------------------|
| None | — | — |
