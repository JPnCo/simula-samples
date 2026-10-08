# Research: Traffic Light Grid Sample

Phase 0 output of the `/speckit.plan` command. Resolves the technical unknowns
of the feature against the actual framework API and codebase.

## R1 — Should the sample be self-contained under `fr.jpnco.simula.samples.trafficlight`?

- **Decision**: Yes. The sample MUST implement its own actors and state types
  using only the framework **core** API (`fr.jpnco.simula.Actor`,
  `fr.jpnco.simula.Engine`, `fr.jpnco.simula.engine.*`) and MUST NOT depend on the
  framework's `fr.jpnco.simula.examples.trafficlight` package.
- **Rationale**: Principle IX defines every sub-package of
  `fr.jpnco.simula.samples` as a self-contained sample. The current local copy
  imports the framework's `examples.trafficlight` classes and defines unused
  duplicate state types, so it neither compiles as a standalone demonstration
  nor documents itself. A sample must stand on its own using the framework API.
- **Alternatives considered**:
  - Keep importing the framework's `examples` classes — rejected: makes the
    local code dead/duplicated and contradicts the "self-contained sample"
    intent.
  - Fork a slimmed copy that re-exports the framework's examples — rejected:
    hides the real architecture and duplicates the framework.

## R2 — What is the framework core API surface the sample needs?

- **Decision**: The sample uses the framework core types already present in the
  installed `fr.jpnco.simula:simula-core:0.0.1-SNAPSHOT` jar:
  `Actor`, `Engine`, `Event`, `engine.ActorDelegate`, `engine.EngineImpl`,
  `engine.EventImpl`, `engine.ExecutionMode`, `engine.IdBuilder`,
  `actors.Logger` (+ `Logger.Level`).
- **Rationale**: These are the stable framework contracts for building an
  actor-based simulation; verified present in the jar.
- **Alternatives considered**: none — the core API is the intended extension
  point.

## R3 — How is the sample tested and covered?

- **Decision**: Test the sample's logic with JUnit 5 + Mockito (light state,
  vehicle movement/stop/direction rules, grid geometry, snapshot assembly) and
  measure line and branch coverage with a coverage tool, requiring ≥97% on both
  (Constitution Principle II — every sample is a deliverable, no exemption). The
  runnable/Swing demo and engine wiring are validated through the quickstart.
- **Rationale**: This satisfies Principle I (Test-First) and Principle II
  (coverage gate) for the sample.
- **Alternatives considered**:
  - Full integration tests driving the engine — rejected: heavy and brittle for
    a sample.
  - No tests / no coverage gate — rejected: violates Principles I and II.

## R4 — How are the two execution modes and determinism handled?

- **Decision**: Keep the framework's `ExecutionMode.VIRTUAL` (default) and
  `ExecutionMode.PLATFORM` (classic) as already implemented, with a fixed random
  seed so the same scenario yields an identical outcome in both modes
  (SC-003).
- **Rationale**: Reproducibility across modes is an explicit success criterion
  and a core framework property to demonstrate.
- **Alternatives considered**: a single mode — rejected: drops SC-003 value.

## R5 — What must the sample architecture document describe?

- **Decision**: `docs/trafficligth-architecture.md` MUST describe the current
  `fr.jpnco.simula.samples.trafficlight` architecture: packages, actors, state
  types, event topics, tick interaction, thread-safety model, determinism, and
  CLI, with Mermaid structural diagrams and **no historical information**
  (Principle IX).
- **Rationale**: The existing document references
  `fr.jpnco.simula.examples.trafficlight`, which no longer reflects the sample.
- **Alternatives considered**: leave as-is — rejected: violates G7/Principle IX.
