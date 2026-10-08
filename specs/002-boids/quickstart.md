# Quickstart: Boids Flocking Sample

This guide validates the boids sample end to end.

## Build and test

```bash
mvn clean verify
```

Expected: build succeeds, all tests pass, and the JaCoCo gate reports ≥97% line
and branch coverage.

## Console run

```bash
mvn exec:java -Dexec.mainClass=fr.jpnco.simula.samples.boids.BoidsDemo
# or, from a packaged jar
java -cp target/classes:... fr.jpnco.simula.samples.boids.BoidsDemo
```

Expected: the simulation advances one tick per simulated second, prints the world
grid each tick, stops at the bounded duration, prints a final outcome summary
(`boids=<n>, distance=<m>`), and exits cleanly.

## GUI run

```bash
java -cp ... fr.jpnco.simula.samples.boids.BoidsDemo gui
```

Expected: a window opens showing the moving boids and repaints at a regular
cadence until the window is closed.

## Determinism check

```bash
java -cp ... fr.jpnco.simula.samples.boids.BoidsDemo            # virtual (default)
java -cp ... fr.jpnco.simula.samples.boids.BoidsDemo classic    # platform
```

Expected: the printed final outcome (`boids=<n>, distance=<m>`) is identical in
both modes. This is also verified automatically by `DeterminismTest`.
