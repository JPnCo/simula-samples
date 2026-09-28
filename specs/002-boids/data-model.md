# Data Model: Boids Flocking Sample

## Entities

### Boid
An autonomous agent living in a bounded 2D world.

| Field | Type | Meaning |
|-------|------|---------|
| `id` | int | Boid identity |
| `x` | double | Position on the horizontal axis |
| `y` | double | Position on the vertical axis |
| `vx` | double | Velocity on the horizontal axis |
| `vy` | double | Velocity on the vertical axis |
| `maxSpeed` | double | Maximum speed (magnitude of the velocity is capped) |
| `perceptionRadius` | double | Radius within which neighbours are considered |
| `seed` | long | Randomness seed (deterministic per boid) |

Each tick (dt = 1) the boid advances by its velocity: `x += vx`, `y += vy`.

### World
The bounded, toroidal rectangle in which the boids move (a boid reaching an edge
wraps around to the opposite edge).

| Field | Type | Meaning |
|-------|------|---------|
| `width` | double | World width |
| `height` | double | World height |

### FlockState (immutable snapshot)
Broadcast on `new-state` after every completed tick.

| Field | Type | Meaning |
|-------|------|---------|
| `simTime` | int | Simulated time |
| `boids` | List\<BoidView\> | Immutable view of the boids' positions/velocities |
| `worldWidth` | double | World width |
| `worldHeight` | double | World height |

### BoidView (immutable)
One boid's position and velocity.

| Field | Type | Meaning |
|-------|------|---------|
| `id` | int | Boid identity |
| `x` | double | Position x |
| `y` | double | Position y |
| `vx` | double | Velocity x |
| `vy` | double | Velocity y |

### BoidState (immutable report)
Broadcast on `boid-state` by each boid.

| Field | Type | Meaning |
|-------|------|---------|
| `tick` | int | Simulated tick |
| `id` | int | Boid identity |
| `x` | double | Position x |
| `y` | double | Position y |
| `vx` | double | Velocity x |
| `vy` | double | Velocity y |

### BoidModel (pure logic)
Static methods implementing the Reynolds flocking rules. No mutable state. All
weights and radii are configurable parameters.

| Method | Purpose |
|--------|---------|
| `nextVelocity(...)` | Applies weighted separation, alignment, cohesion and speed limiting |
| `separation(...)` | Pushes the boid away from nearby neighbours |
| `alignment(...)` | Steers toward the average velocity of neighbours |
| `cohesion(...)` | Steers toward the average position of neighbours |
| `limitSpeed(...)` | Caps the velocity magnitude at the maximum speed |
| `wrap(x, y, worldWidth, worldHeight)` | Wraps a position around the toroidal world bounds |

## Relationships

- A `World` contains many `Boid` agents (the flock).
- A `Boid` produces a `BoidState` each tick.
- `BoidsCoordinator` aggregates all `BoidState`s into a `FlockState` (of
  `BoidView`s) and broadcasts it.
- Displays (`BoidsMonitor`, `BoidsGui`) read `FlockState`.
