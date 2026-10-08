# Data Model: Dashboard Simula Sample

## Entities

### Sample
An immutable time-series record: one aggregate metric value captured from one
simulation snapshot at one simulated time.

| Field | Type | Meaning |
|-------|------|---------|
| `simTime` | int | Simulated time of the source snapshot (`getSimTime()`) |
| `value` | double | Aggregate metric recorded from the snapshot (e.g. vehicle count or boid count) |

### TimeSeries
The ordered, append-only collection of `Sample`s recorded over a run. Immutable
view for consumers (monitor, GUI, exporter).

| Field | Type | Meaning |
|-------|------|---------|
| `samples` | List\<Sample\> | Ordered samples, one per tick |

Behavior:
- `append(sample)` — record a new sample (returns a new `TimeSeries`; the type is
  immutable).
- `size()` — number of recorded samples.
- `first()` / `last()` — first/last sample by insertion order (for the console
  summary).
- `isEmpty()` — true when no `new-state` events were received (empty-series edge
  case).

### MetricExtractor
Pure, stateless function that extracts the aggregate metric from an immutable
snapshot. It is the single point where the dashboard maps a target-specific
snapshot to a `double` value.

| Input snapshot | Metric extracted |
|----------------|------------------|
| `GridState` (traffic-light) | `getVehicleCount()` |
| `FlockState` (boids) | `getBoidCount()` |

### Dashboard (observer actor)
Attaches to an existing simulation: subscribes to the target's `new-state` topic
and records a `Sample` into the `TimeSeries` for each received snapshot.

### DashboardParameters
Immutable configuration for a run: execution mode, target sample (trafficlight or
boids), display (console/gui), and optional export file path.

## Relationships

- A `Snapshot` (the immutable `new-state` payload) is converted by
  `MetricExtractor` into the metric of a `Sample`.
- A `Sample` is appended to a `TimeSeries`.
- The `Dashboard` actor owns the `TimeSeries` and is the only writer.
- `DashboardMonitor`, `DashboardGui`, and `CsvExporter` read the `TimeSeries`
  (console summary, plotting, and export respectively).
- `DashboardParameters` drives `DashboardDemo` and the `Dashboard` construction.

## State transitions

The `TimeSeries` grows monotonically by one sample per tick; there is no
removal, update, or replay. An empty run (no `new-state`) leaves the series empty,
which the sample reports rather than fails (FR-008).
