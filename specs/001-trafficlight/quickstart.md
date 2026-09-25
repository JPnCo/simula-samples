# Quickstart: Traffic Light Grid Sample

Phase 1 output of the `/speckit.plan` command. Runnable validation guide that
proves the feature works end-to-end.

## Prerequisites

- JDK 25 and Maven (3.9+).
- The `jpnco:simula:0.0.1-SNAPSHOT` framework installed in the local Maven repo
  (run `mvn install` in the SIMULA project first).

## Setup

```bash
# In the simula-samples project root
mvn compile
```

## Build & Test

```bash
mvn test        # runs the sample's unit tests (light state, vehicle, grid, direction)
mvn package     # produces target/simula-samples-0.0.1-SNAPSHOT.jar
```

## Run (console)

```bash
mvn exec:java -Dexec.mainClass=jpnco.simula.samples.trafficlight.TrafficLightDemo
# or with the built classpath:
java -cp "target/classes:<framework-jar>" jpnco.simula.samples.trafficlight.TrafficLightDemo
```

**Expected**: the simulation advances one tick per simulated second, then prints
a final outcome summary, e.g.:

```text
=== OUTCOME (VIRTUAL) ===
vehicles=12, crossings=<m>
```

and exits cleanly.

## Run (GUI)

```bash
java -cp "target/classes:<framework-jar>" jpnco.simula.samples.trafficlight.TrafficLightDemo gui
```

**Expected**: a Swing window opens showing the grid, roads, intersection traffic
lights, and moving vehicles; it repaints as the simulation advances and stops
when the window is closed.

## Validate determinism (SC-003)

```bash
java -cp "target/classes:<framework-jar>" jpnco.simula.samples.trafficlight.TrafficLightDemo
java -cp "target/classes:<framework-jar>" jpnco.simula.samples.trafficlight.TrafficLightDemo classic
```

**Expected**: both runs print an **identical** `vehicles=12, crossings=<m>`
outcome.

## Traceability

- Entities & rules: see [data-model.md](./data-model.md)
- CLI contract: [contracts/trafficlight-cli.md](./contracts/trafficlight-cli.md)
- Topic/actor contract: [contracts/trafficlight-topics.md](./contracts/trafficlight-topics.md)
- Per-sample architecture: [`docs/trafficligth-architecture.md`](../../docs/trafficligth-architecture.md)

## Reference

- Feature spec: [spec.md](./spec.md)
