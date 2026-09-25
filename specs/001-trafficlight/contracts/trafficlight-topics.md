# Contract: Traffic Light Event Topics

Phase 1 output of the `/speckit.plan` command. The sample's actors cooperate
exclusively by broadcasting events on shared topics (no actor holds a reference
to another). This is the internal contract between the actors.

| Topic | Publisher → Consumer | Payload |
|-------|----------------------|---------|
| `TIME_EVENT` | root engine → coordinator | tick (engine built-in) |
| `next-traffic-light-states` | coordinator → each light | tick |
| `next-vehicles-states` | coordinator → each vehicle | tick, previous-tick light table |
| `traffic-light-state` | each light → lights `Barrier` | `TrafficLightState` |
| `vehicles-state` | each vehicle → vehicles `Barrier` | `VehicleState` |
| `lights-ready` | lights `Barrier` → coordinator | (none) |
| `vehicles-ready` | vehicles `Barrier` → coordinator | (none) |
| `new-state` | coordinator → displays | `GridState` |

## Grouping rule

Synchronization is delegated to two simula `Barrier` actors (in `CYCLIC` mode with
distinct-source counting): the lights `Barrier` subscribes to `traffic-light-state`
and the vehicles `Barrier` subscribes to `vehicles-state`. When all lights of the
tick have reported, the lights `Barrier` broadcasts `lights-ready`; when all
vehicles have reported, the vehicles `Barrier` broadcasts `vehicles-ready`. The
coordinator assembles and broadcasts `new-state` only after it has seen **both**
completion signals for the current tick, pulling each light's and each vehicle's
last-reported state. Vehicles decide on the **previous tick's** light table so
their decision does not depend on report arrival order.

## Corner rule

At the start of each tick the coordinator resets its light table to all-green;
only non-corner cells are overwritten by received reports, so the four corners
remain green.
