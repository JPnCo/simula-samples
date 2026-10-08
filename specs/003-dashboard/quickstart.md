# Quickstart: Dashboard Simula Sample

This guide validates the dashboard sample end to end.

## Build and test

```bash
mvn clean verify
```

Expected: build succeeds, all tests pass, and the JaCoCo gate reports ≥97% line
and branch coverage.

## Console run (attach to boids, default)

```bash
mvn exec:java -Dexec.mainClass=fr.jpnco.simula.samples.dashboard.DashboardDemo
# or, from a packaged jar
java -cp target/classes:... fr.jpnco.simula.samples.dashboard.DashboardDemo
```

Expected: the dashboard attaches to the boids simulation, records one sample per
tick, prints a per-tick line and a final summary
(`samples=<n>, first=<t0>, last=<tN>`), and exits cleanly.

## Console run (attach to traffic-light)

```bash
java -cp ... fr.jpnco.simula.samples.dashboard.DashboardDemo trafficlight
```

Expected: the dashboard attaches to the traffic-light simulation and records one
sample per tick (vehicle count) with the same console summary behavior.

## GUI run

```bash
java -cp ... fr.jpnco.simula.samples.dashboard.DashboardDemo boids gui
```

Expected: a window opens that plots the collected time series and repaints at a
regular cadence until the window is closed.

## Export

```bash
java -cp ... fr.jpnco.simula.samples.dashboard.DashboardDemo boids console export=series.csv
```

Expected: after the run completes, `series.csv` is written with a header line and
one `simTime,value` line per recorded tick.

## Cross-sample check

```bash
java -cp ... fr.jpnco.simula.samples.dashboard.DashboardDemo trafficlight console
java -cp ... fr.jpnco.simula.samples.dashboard.DashboardDemo boids console
```

Expected: the same dashboard build records a time series from both target samples
without code changes (SC-003). Refer to the [topics contract](./contracts/dashboard-topics.md)
and the [CLI contract](./contracts/dashboard-cli.md) for details.
