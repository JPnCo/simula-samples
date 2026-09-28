# Research: Boids Flocking Sample

## Reynolds Flocking Model

Craig Reynolds' "boids" model defines three steering behaviours that a boid
combines each step:

1. **Separation**: steer to avoid crowding local flockmates (push away from
   neighbours that are too close).
2. **Alignment**: steer towards the average heading of local flockmates.
3. **Cohesion**: steer to move toward the average position of local flockmates.

Each behaviour considers only neighbours within a **perception radius**. The
resulting steering is added to the boid's current velocity, which is then capped
at a **maximum speed** so the boid does not accelerate without bound.

## Determinism

To obtain an identical outcome across execution modes, every boid must decide on
a **deterministic** input. Following the traffic-light sample's approach, each
boid decides on the **previous tick's** flock state (already assembled and
broadcast), not on the arrival order of the current tick's reports. All
randomness (initial positions/velocities) is seeded from a single `RANDOM_SEED`
plus the boid id.

## Edge handling

The world is **toroidal** (wrap-around): a boid reaching an edge reappears on
the opposite edge, so no boid ever leaves the world and no steering or clamping
force is needed at the edges. Neighbour distances are also computed on the
toroidal surface so that boids near opposite edges can still interact.

## Framework mapping

- `BoidsCoordinator` — orchestrator actor (assemble + broadcast `new-state`).
- `Boid` — one actor per boid.
- `Barrier` (simula) — synchronizes the per-tick `boid-state` reports
  (`boids-ready`).
- `BoidsMonitor` — console display.
- `BoidsGui` — Swing display.
