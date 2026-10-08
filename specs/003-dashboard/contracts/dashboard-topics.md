# Contract: Dashboard Observer Topic

Phase 1 output of the `/speckit.plan` command. The dashboard is an external
observer: it does not publish any topic of its own; it subscribes to an existing
simulation's `new-state` topic and records the snapshot payload.

| Topic | Publisher → Consumer | Payload |
|-------|----------------------|---------|
| `new-state` (traffic-light) | `TrafficCoordinator` → `Dashboard` | `GridState` |
| `new-state` (boids) | `BoidsCoordinator` → `Dashboard` | `FlockState` |

## Subscribe rule

The `Dashboard` actor subscribes to the target sample's `new-state` topic on the
same root engine (constructor: `engine.subscribe(this, Topics.NEW_STATE)`). It
reads the snapshot from `Event.getParameters()[0]` and records
`Sample(snapshot.getSimTime(), MetricExtractor.extract(snapshot))`.

## Sampling rule

One sample is recorded per received `new-state` event (one per tick). `simTime`
is taken from the snapshot itself, so no separate clock is needed. The dashboard
does not deduplicate samples: each tick is a distinct sample.

## Non-goals

- The dashboard does not publish or alter any topic of the target simulation.
- The dashboard introduces no new topics beyond the existing `new-state`.
