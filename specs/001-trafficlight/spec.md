# Feature Specification: Traffic Light Grid Sample

**Feature Branch**: `001-trafficlight`

**Created**: 2026-09-25

**Status**: Draft

**Input**: User description: "définit une feature trafficlight sur la base du code du sample trafficlight"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Run the traffic-light simulation (Priority: P1)

As a developer evaluating the simula framework, I want to run a self-contained
traffic-light grid simulation so that I can observe a working actor-based
simulation end to end.

**Why this priority**: This is the core value of the sample: a single, runnable
demonstration of the framework that a user can launch and watch. Without it
there is no sample to explore.

**Independent Test**: Can be fully tested by launching the simulation in console
mode and verifying it runs for a bounded number of simulated seconds, stops, and
prints a final outcome summary (vehicle count and total crossings).

**Acceptance Scenarios**:

1. **Given** a fresh simulation run, **When** the user launches the console
   display, **Then** the simulation advances one tick per simulated second.
2. **Given** a running console simulation, **When** the configured simulated
   duration is reached, **Then** the engine stops and a final outcome summary is
   printed with the vehicle count and total crossings.
3. **Given** the simulation, **When** it completes, **Then** the process exits
   cleanly without error.

---

### User Story 2 - Visualize the simulation in a window (Priority: P2)

As a developer, I want to watch the simulation rendered in a graphical window so
that I can see the vehicles, roads, and traffic lights move in real time.

**Why this priority**: The GUI adds visual value and demonstrates that the
framework supports actor-driven rendering, but the console run already delivers
the core value, so it is secondary.

**Independent Test**: Can be tested independently by launching the simulation
with the GUI display and verifying a window opens, shows the grid, roads,
lights, and moving vehicles, and updates over time until closed.

**Acceptance Scenarios**:

1. **Given** the GUI display is requested, **When** the simulation starts,
   **Then** a window opens showing the grid with roads, intersection traffic
   lights, and vehicles.
2. **Given** a running GUI display, **When** the simulation produces new
   states, **Then** the window repaints to reflect the latest state.
3. **Given** a running GUI display, **When** the user closes the window, **Then**
   the simulation stops.

---

### User Story 3 - Compare execution modes (Priority: P3)

As a developer, I want to run the same scenario under two execution modes and
obtain the same outcome so that I can confirm behavioral equivalence.

**Why this priority**: This demonstrates a framework property (deterministic,
mode-independent behavior) but is a secondary exploration on top of the core
runnable sample.

**Independent Test**: Can be tested independently by running the same scenario
once in each mode and verifying the printed final outcome (vehicles and total
crossings) is identical.

**Acceptance Scenarios**:

1. **Given** the same fixed random seed, **When** the scenario is run in the
   default mode, **Then** the final outcome is a specific, reproducible value.
2. **Given** the same scenario, **When** it is run in the alternative mode,
   **Then** the final outcome is identical to the default mode.

---

### Edge Cases

- What happens when the user passes an unknown command-line token? The unknown
  token is ignored and the default behavior applies (no crash).
- What happens when both a mode and a display argument are given? The two
  arguments are resolved independently and both take effect.
- What happens when a vehicle reaches the edge of the grid? It is forced to turn
  right or left so that it never leaves the grid and keeps circulating.
- What happens at the four corner intersections? They have no crossing traffic
  and are always green, so vehicles flow through without stopping.
- What happens to a vehicle that reaches a red light? It stops at the stop line
  and waits until its band turns green.
- What happens if the engine does not complete within a bounded wait? The
  console run reports that completion was not reached rather than hanging
  indefinitely.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The sample MUST provide a runnable entry point that starts a
  traffic-light grid simulation without requiring any external setup beyond the
  framework.
- **FR-002**: The simulation MUST model a grid of intersections, with a traffic
  light at each non-corner intersection and two alternating bands
  (north-south and east-west).
- **FR-003**: The simulation MUST include a fixed fleet of vehicles that travel
  the grid, respect the traffic lights, and keep circulating within the grid.
- **FR-004**: A vehicle MUST stop at a red (or orange) light for its direction
  and MUST only advance when its band is green.
- **FR-005**: A vehicle MUST NOT leave the grid; at an edge it MUST turn rather
  than exit.
- **FR-006**: The sample MUST support a console display that prints the grid
  state each tick and a final outcome summary when the run ends.
- **FR-007**: The sample MUST support a graphical (windowed) display that
  renders the grid, roads, lights, and vehicles and updates in real time.
- **FR-008**: The sample MUST support at least two execution modes and MUST
  produce an identical final outcome in each mode for the same scenario.
- **FR-009**: All randomness in the simulation MUST be seeded so that a given
  scenario is reproducible across runs and modes.

### Key Entities *(include if feature involves data)*

- **Vehicle**: A unit that moves along the grid, holding a position, a
  direction, a fixed speed, and a distance travelled into its current segment.
- **Traffic Light**: A per-intersection unit with two bands (north-south,
  east-west) that alternate between green, orange, and red.
- **Grid**: A bounded 3x3 layout of cells bounded by roads, with an
  intersection at every crossing and four always-green corner intersections.
- **Simulation State**: An immutable snapshot of the grid at one instant,
  covering the vehicles, the light bands, and the per-cell crossing counts.
- **Outcome**: The final aggregate of the run (vehicle count and total
  crossings) used to compare execution modes.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can launch the console simulation and observe it progress
  one tick per simulated second until it stops at the configured duration.
- **SC-002**: The console run always terminates and prints a final outcome
  summary (vehicle count and total crossings) without user intervention.
- **SC-003**: Running the same seeded scenario in both supported execution modes
  yields an identical final outcome (behavioral equivalence).
- **SC-004**: The GUI display opens, renders the grid with roads, lights, and
  vehicles, and repaints at a regular cadence to reflect the latest state.
- **SC-005**: The sample builds and runs as a standalone demonstration, with all
  randomness seeded so a given scenario is reproducible across runs.
- **SC-006**: The sample achieves at least 97% line and branch coverage as
  measured by an automated coverage tool (Principle II applies to every sample).

## Implementation Notes

- **Report synchronization (non-functional)**: the coordinator does not manually
  count incoming reports. Two simula `Barrier` actors (in `CYCLIC` mode,
  distinct-source counting) subscribe to `traffic-light-state` and
  `vehicles-state`; when all lights (respectively all vehicles) of a tick have
  reported they fire `lights-ready` (respectively `vehicles-ready`). The
  coordinator assembles and broadcasts `new-state` only after seeing **both**
  completion signals, pulling each actor's last-reported state. This is an
  implementation decision that satisfies the grouping described in the
  acceptance scenarios without changing any functional requirement.

## Assumptions

- The sample is demonstration code intended to showcase the framework; it is not
  part of the framework contract, but per the project constitution it is a
  deliverable that MUST satisfy the 97% line and branch coverage thresholds
  (Principle II).
- The sample ships as a sub-package of `jpnco.simula.samples` and, per the
  project constitution, MUST have its own architecture document in the `docs`
  directory reflecting its current architecture.
- A console display is the default; the graphical display is optional and
  activated on request.
- The default simulated duration for a console run is finite and bounded so the
  run terminates.
- The simulation is single-user and local; there is no persistence, network, or
  multi-user requirement.
