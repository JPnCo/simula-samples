# Data Model: Traffic Light Grid Sample

Phase 1 output of the `/speckit.plan` command. Entities are derived from the
feature spec and the current sample code (all types are immutable state carriers
or enums; there is no persistence).

## Entities

### Grid

The bounded world of the simulation: a `GRID_SIZE` x `GRID_SIZE` layout of cells
bounded by `INTERSECTIONS` x `INTERSECTIONS` roads, with an intersection at every
crossing and four always-green corner intersections.

| Field | Type | Meaning |
|-------|------|---------|
| `GRID_SIZE` | int constant | Cells along each side (3) |
| `INTERSECTIONS` | int constant | Roads/intersections along each side (4) |
| `SEGMENT_LENGTH` | double constant | Road segment between two intersections (100.0 m) |
| `INTERSECTION_HALF` | double constant | Half the crossing width (5.0 m) |
| `LIGHT_POSITION` | double constant | Stop line of the next intersection's light (95.0 m) |

### Vehicle

A unit that moves along the grid.

| Field | Type | Meaning |
|-------|------|---------|
| `id` | int | Vehicle identity |
| `row` | int | Current row (origin of its segment) |
| `col` | int | Current column (origin of its segment) |
| `direction` | Direction | Current direction of travel |
| `speed` | double | Fixed speed in m/s |
| `distanceInSegment` | double | Distance travelled into the current segment (m) |

Validation/behavior rules (from FR-003/004/005):
- A vehicle MUST stop at `LIGHT_POSITION` when its band is not green.
- A vehicle MUST NOT leave the grid; on an edge it turns right or left.
- A vehicle NEVER reverses (only straight, right, left).

### TrafficLight

One per non-corner intersection; controls two bands.

| Field | Type | Meaning |
|-------|------|---------|
| `id` | int | Light identity |
| `row`/`col` | int | Intersection position |
| `greenDuration` | int | Green duration in s |
| `orangeDuration` | int | Orange duration in s |
| `phaseOffset` | int | Stagger offset in s |
| bands | `ns`, `ew` | North-south / east-west `LightState` |

Behavior rule (FR-002): bands alternate green → orange → red on the light's own
cycle; corners stay green.

### LightState (enum)

`GREEN`, `ORANGE`, `RED`.

### Direction (enum)

`NORTH`, `SOUTH`, `EAST`, `WEST`, each with `(rowDelta, colDelta)`; provides
`isVertical()`, `turnRight()`, `turnLeft()`.

### GridState (immutable snapshot)

Published once per completed tick and read safely by displays.

| Field | Type | Meaning |
|-------|------|---------|
| `simTime` | int | Simulated time of the snapshot |
| `vehicles` | List\<VehicleView\> | Unmodifiable vehicle positions/directions |
| `northSouth` | LightState[][] | NS band of each intersection |
| `eastWest` | LightState[][] | EW band of each intersection |
| `crossings` | int[][] | Per-cell vehicle arrival counts so far |

### VehicleView (immutable)

A read-only view of one vehicle inside a `GridState`: `id`, `row`, `col`,
`direction`, `distanceInSegment`.

### VehicleState (immutable report)

Broadcast by a vehicle each tick: `tick`, `id`, `row`, `col`, `direction`,
`distanceInSegment`, plus the list of `(row, col)` cells entered during the tick
(for crossing counters).

### TrafficLightState (immutable report)

Broadcast by a light each tick: `tick`, `row`, `col`, `ns`, `ew`.

### Outcome

The final aggregate used to compare execution modes (SC-003): vehicle count and
total crossings.

## State Transitions

### Traffic light band
`GREEN` → `ORANGE` → `RED` → `GREEN` (per light's own cycle; deterministic
function of `tick + phaseOffset`).

### Vehicle crossing decision
Approaching an intersection, a vehicle either **advances** (band green) or
**stops at the stop line** (band orange/red) and waits for green.

### Vehicle direction at an intersection
`straight` | `turnRight` | `turnLeft` (never reverse); on an edge the turn is
forced; otherwise random with `TURN_PROBABILITY`, discarding grid-leaving
choices.
