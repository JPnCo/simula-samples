# Feature Specification: Boids Flocking Sample

**Feature Branch**: `002-boids`

**Created**: 2026-09-25

**Status**: Draft

**Input**: User request — "1" (implement the Boids / flocking simulation sample for the simula framework).

## Clarifications

### Session 2026-09-25

- Q: When a boid reaches a world edge, how should it be kept inside the world bounds? → A: The world is toroidal (wrap-around) — a boid exiting one edge reappears on the opposite edge; no steering or clamping force is applied.
- Q: How should the simulation advance position each tick and how is the "total distance travelled" outcome computed? → A: Each tick uses dt = 1, so each boid advances by its velocity (`x += vx`, `y += vy`). "Total distance" is the sum over all boids of their per-tick displacement magnitude (the length of the velocity vector), accumulated over the whole run.
- Q: What concrete weights and parameter values should the flocking rules use, and what are the perception radius and maximum speed? → A: Defaults are separation/alignment/cohesion weights = 1.0, perception radius = 40, maximum speed = 4 (units per tick), in a world of 800×600 — but these parameters MUST be configurable at runtime (e.g., via command-line options), not hard-coded.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Run the flocking simulation (Priority: P1)

As a developer evaluating the simula framework, I want to run a self-contained
boids flocking simulation so that I can observe a working actor-based
simulation with many concurrent autonomous agents.

**Why this priority**: This is the core value of the sample: a single runnable
demonstration of a large population of independent actors cooperating to form a
flock, which the user can launch and watch.

**Independent Test**: Can be fully tested by launching the simulation in console
mode and verifying it runs for a bounded number of simulated seconds, stops, and
prints a final outcome summary (boid count and total distance travelled).

**Acceptance Scenarios**:

1. **Given** a fresh simulation run, **When** the user launches the console
   display, **Then** the simulation advances one tick per simulated second.
2. **Given** a running console simulation, **When** the configured simulated
   duration is reached, **Then** the engine stops and a final outcome summary is
   printed with the boid count and total distance travelled.
3. **Given** the simulation, **When** it completes, **Then** the process exits
   cleanly without error.

---

### User Story 2 - Visualize the flock in a window (Priority: P2)

As a developer, I want to watch the flock rendered in a graphical window so that
I can see the boids move and align in real time.

**Why this priority**: The GUI adds visual value and demonstrates that the
framework supports actor-driven rendering of a large population, but the console
run already delivers the core value, so it is secondary.

**Independent Test**: Can be tested independently by launching the simulation
with the GUI display and verifying a window opens, shows the moving boids, and
updates over time until closed.

**Acceptance Scenarios**:

1. **Given** the GUI display is requested, **When** the simulation starts,
   **Then** a window opens showing the boids moving in the world.
2. **Given** a running GUI display, **When** the simulation produces new states,
   **Then** the window repaints to reflect the latest state.
3. **Given** a running GUI display, **When** the user closes the window, **Then**
   the simulation stops.

---

### User Story 3 - Compare execution modes (Priority: P3)

As a developer, I want to run the same scenario under two execution modes and
obtain the same outcome so that I can confirm behavioral equivalence.

**Why this priority**: This demonstrates the framework property (deterministic,
mode-independent behavior) but is a secondary exploration on top of the core
runnable sample.

**Independent Test**: Can be tested independently by running the same scenario
once in each mode and verifying the printed final outcome (boids and total
distance) is identical.

**Acceptance Scenarios**:

1. **Given** the same fixed random seed, **When** the scenario is run in the
   default mode, **Then** the final outcome is a specific, reproducible value.
2. **Given** the same scenario, **When** it is run in the alternative mode,
   **Then** the final outcome is identical to the default mode.

---

### Edge Cases

- What happens when a boid reaches a world edge? The world is toroidal
  (wrap-around): the boid reappears on the opposite edge, so no boid ever leaves
  the world.
- What happens when a boid has no neighbours within its perception radius? It
  applies no social rule and simply continues at its current velocity (coasted),
  so the sample still advances.
- What happens when a boid is exactly on top of a neighbour? The separation rule
  pushes it away so boids do not stack permanently.
- What happens if the engine does not complete within a bounded wait? The
  console run reports that completion was not reached rather than hanging
  indefinitely.
- What happens when an unknown command-line token is passed? The unknown token
  is ignored and the default behavior applies (no crash).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The sample MUST provide a runnable entry point that starts a boids
  flocking simulation without requiring any external setup beyond the framework.
- **FR-002**: The simulation MUST model a bounded 2D world containing a fixed
  population of autonomous boid agents, each with a position and a velocity.
- **FR-003**: Each boid MUST apply the classic flocking rules — separation,
  alignment, and cohesion — based on the neighbouring boids within its
  perception radius.
- **FR-004**: The world is toroidal: a boid reaching an edge MUST wrap around to
  the opposite edge, so no boid ever leaves the world.
- **FR-005**: The sample MUST support a console display that prints the world
  state each tick and a final outcome summary when the run ends.
- **FR-006**: The sample MUST support a graphical (windowed) display that
  renders the boids and updates in real time.
- **FR-007**: The sample MUST support at least two execution modes and MUST
  produce an identical final outcome in each mode for the same scenario.
- **FR-008**: All randomness in the simulation MUST be seeded so that a given
  scenario is reproducible across runs and modes.
- **FR-009**: The flocking parameters — separation, alignment, and cohesion
  weights, perception radius, and maximum speed — MUST be configurable at
  runtime (e.g., via command-line options), with sensible defaults
  (weights = 1.0 each, perception radius = 40, maximum speed = 4).

### Key Entities *(include if feature involves data)*

- **Boid**: An autonomous agent with a position (x, y), a velocity (vx, vy), a
  maximum speed (default 4, configurable), and a perception radius (default 40,
  configurable). Each tick (dt = 1) it advances by its velocity: `x += vx`,
  `y += vy`.
- **Flock**: The fixed population of boid agents that interact through the
  flocking rules.
- **World**: A bounded, toroidal 2D rectangle (default 800×600, configurable
  width/height) in which the boids move; a boid reaching an edge wraps around to
  the opposite edge.
- **Simulation State**: An immutable snapshot of the flock at one instant,
  covering the boids' positions and velocities.
- **Outcome**: The final aggregate of the run (boid count and total distance
  travelled) used to compare execution modes. "Total distance" is the sum over
  all boids of their per-tick displacement magnitude (the length of the velocity
  vector), accumulated over the whole run.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can launch the console simulation and observe it progress
  one tick per simulated second until it stops at the configured duration.
- **SC-002**: The console run always terminates and prints a final outcome
  summary (boid count and total distance) without user intervention.
- **SC-003**: Running the same seeded scenario in both supported execution modes
  yields an identical final outcome (behavioral equivalence).
- **SC-004**: The GUI display opens, renders the boids, and repaints at a
  regular cadence to reflect the latest state.
- **SC-005**: The sample builds and runs as a standalone demonstration, with all
  randomness seeded so a given scenario is reproducible across runs.
- **SC-006**: The sample achieves at least 97% line and branch coverage as
  measured by an automated coverage tool (Principle II applies to every sample).

## Assumptions

- The sample is demonstration code intended to showcase the framework; it is not
  part of the framework contract, but per the project constitution it is a
  deliverable that MUST satisfy the 97% line and branch coverage thresholds
  (Principle II).
- The sample ships as a sub-package of `fr.jpnco.simula.samples` and, per the
  project constitution, MUST have its own architecture document in the `docs`
  directory reflecting its current architecture.
- A console display is the default; the graphical display is optional and
  activated on request.
- The default simulated duration for a console run is finite and bounded so the
  run terminates.
- The simulation is single-user and local; there is no persistence, network, or
  multi-user requirement.
- Each boid decides on the **previous tick's** flock state so its movement does
  not depend on report arrival order (determinism).
