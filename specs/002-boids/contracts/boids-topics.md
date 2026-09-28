# Contract: Boids Event Topics

Phase 1 output of the `/speckit.plan` command. The sample's actors cooperate
exclusively by broadcasting events on shared topics (no actor holds a reference
to another). This is the internal contract between the actors.

| Topic | Publisher → Consumer | Payload |
|-------|----------------------|---------|
| `TIME_EVENT` | root engine → coordinator | tick (engine built-in) |
| `next-boid-states` | coordinator → each boid | tick, previous-tick `FlockState` |
| `boid-state` | each boid → `Barrier` | `BoidState` |
| `boids-ready` | `Barrier` → coordinator | (none) |
| `new-state` | coordinator → displays | `FlockState` |

## Grouping rule

Synchronization is delegated to a simula `Barrier` actor (in `CYCLIC` mode,
distinct-source counting) that subscribes to `boid-state`. When all boids of the
tick have reported, the `Barrier` broadcasts `boids-ready`. The coordinator
assembles and broadcasts `new-state` only after seeing that completion signal,
pulling each boid's last-reported state. Each boid decides on the **previous
tick's** flock state so its decision does not depend on report arrival order.
