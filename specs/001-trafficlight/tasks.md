---

description: "Task list for the Traffic Light Grid Sample feature"
---

# Tasks: Traffic Light Grid Sample

**Input**: Design documents from `/specs/001-trafficlight/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests ARE included. The spec requires ≥97% line and branch coverage
(SC-006 / Constitution Principle II) and the plan mandates Test-First (Principle
I). Tests MUST be written and confirmed to FAIL before implementation.

**Organization**: Tasks are grouped by user story to enable independent
implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `src/test/` at repository root
- Sample code under `src/main/java/jpnco/simula/samples/trafficlight/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and coverage tooling

- [x] T001 Add JaCoCo coverage plugin to `pom.xml` with 97% line and branch
      thresholds (supports SC-006 / Constitution Principle II)
- [x] T002 [P] Verify the build resolves `jpnco:simula:0.0.1-SNAPSHOT` and
      compiles via `mvn compile` (framework dependency present in `pom.xml`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Self-contained `states` package that ALL user stories depend on

**CRITICAL**: No user story work can begin until this phase is complete

- [x] T003 Make the `states/` package fully self-contained: correct Javadoc
      `{@link jpnco.simula.examples...}` references to
      `jpnco.simula.samples...` in
      `src/main/java/jpnco/simula/samples/trafficlight/states/GridState.java`,
      `TrafficLightState.java`, `VehicleState.java` (Constitution Principle IX:
      self-contained sample, no `examples` dependency)
- [x] T004 [P] Write failing unit tests for `states/Direction.java`
      (rowDelta/colDelta, isVertical, turnRight, turnLeft for all four
      directions) in
      `src/test/java/jpnco/simula/samples/trafficlight/states/DirectionTest.java`
- [x] T005 [P] Write failing unit tests for `states/GridState.java`
      (immutability/copy, getVehicles unmodifiable, lightState band selection,
      crossingsAt) in
      `src/test/java/jpnco/simula/samples/trafficlight/states/GridStateTest.java`
- [x] T006 [P] Write failing unit tests for `states/VehicleView.java`,
      `states/TrafficLightState.java`, `states/VehicleState.java` (getters,
      enteredCells unmodifiable copy) in
      `src/test/java/jpnco/simula/samples/trafficlight/states/`
      (`VehicleViewTest.java`, `TrafficLightStateTest.java`,
      `VehicleStateTest.java`)

**Checkpoint**: Foundation ready - the states package is self-contained and
tested; user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Run the simulation (Priority: P1) MVP

**Goal**: Launch the console traffic-light grid simulation end to end; it
advances one tick per simulated second, stops at the bounded duration, prints an
outcome summary, and exits cleanly (FR-001, FR-002, FR-003, FR-004, FR-005,
FR-006; SC-001, SC-002).

**Independent Test**: `mvn test` (actors unit tests green); run
`jpnco.simula.samples.trafficlight.TrafficLightDemo` in console mode and observe
tick-by-tick output, a bounded run, an outcome summary
(`vehicles=12, crossings=<m>`), and a clean exit.

### Tests for User Story 1 (write FIRST, confirm FAIL before implementation)

- [x] T007 [P] [US1] Write failing unit tests for `actors/CrossingTrafficLight.java`
      band-state computation (green/orange/red timing, phase offset, cycle) in
      `src/test/java/jpnco/simula/samples/trafficlight/actors/CrossingTrafficLightTest.java`
- [x] T008 [P] [US1] Write failing unit tests for `actors/Vehicle.java`
      (stop at LIGHT_POSITION on red, advance on green, edge forced turn,
      never-reverse direction rules, cells entered) in
      `src/test/java/jpnco/simula/samples/trafficlight/actors/VehicleTest.java`
- [x] T009 [P] [US1] Write failing unit tests for `actors/TrafficCoordinator.java`
      (tick grouping waits for all reports, corner cells stay green, snapshot
      assembly, crossing counters, duration stop) in
      `src/test/java/jpnco/simula/samples/trafficlight/actors/TrafficCoordinatorTest.java`

### Implementation for User Story 1

- [x] T010 [US1] Rewire `actors/` to be self-contained: replace every
      `import jpnco.simula.examples.trafficlight.*` with the local
      `jpnco.simula.samples.trafficlight.*` types in
      `actors/CrossingTrafficLight.java`, `actors/TrafficCoordinator.java`,
      `actors/Vehicle.java`, `actors/TrafficMonitor.java`
- [x] T011 [US1] Rewire `TrafficLightDemo.java` to use the local samples
      actors/states (remove `jpnco.simula.examples.trafficlight.*` imports)
      so the console run produces the outcome summary
- [x] T012 [US1] Verify console flow end to end: bounded duration,
      `=== OUTCOME (<mode>) ===` with `vehicles=12, crossings=<m>`, clean exit
      (FR-001/006, SC-001/002)

**Checkpoint**: User Story 1 fully functional and testable independently (MVP)

---

## Phase 4: User Story 2 - Visualize in a window (Priority: P2)

**Goal**: Open a graphical window that renders the grid, roads, lights, and
vehicles and repaints in real time until closed (FR-007; SC-004).

**Independent Test**: Launch with the GUI display; verify a window opens showing
the grid/roads/lights/vehicles, repaints as the simulation advances, and stops
when closed.

### Implementation for User Story 2

- [x] T013 [P] [US2] Write a focused unit test for `actors/TrafficLightGui.java`
      that the actor subscribes to `new-state` and stores the latest `GridState`
      (lightweight, no full Swing event loop) in
      `src/test/java/jpnco/simula/samples/trafficlight/actors/TrafficLightGuiTest.java`
- [x] T014 [US2] Rewire `actors/TrafficLightGui.java` to be self-contained:
      replace `jpnco.simula.examples.trafficlight.*` imports with local
      `jpnco.simula.samples.trafficlight.*` types
- [x] T015 [US2] Verify the GUI flow: window opens, renders grid/roads/lights/
      vehicles, repaints on new states, stops when closed (FR-007, SC-004)

**Checkpoint**: User Stories 1 AND 2 both work independently

---

## Phase 5: User Story 3 - Compare execution modes (Priority: P3)

**Goal**: The same seeded scenario produces an identical final outcome in both
execution modes (FR-008, FR-009; SC-003, SC-005).

**Independent Test**: Run the console demo in default (`virtual`) and `classic`
(`PLATFORM`) modes and verify the printed `vehicles=12, crossings=<m>` is
identical.

### Tests for User Story 3

- [x] T016 [P] [US3] Write a determinism test that runs the same seeded scenario
      in both `ExecutionMode.VIRTUAL` and `ExecutionMode.PLATFORM` and asserts
      the final outcome (total crossings) is identical, in
      `src/test/java/jpnco/simula/samples/trafficlight/DeterminismTest.java`

### Implementation for User Story 3

- [x] T017 [US3] Verify seeded reproducibility: `resolveMode`/`isGui` argument
      handling in `TrafficLightDemo.java` and fixed `RANDOM_SEED` yield an
      identical outcome across modes (FR-008/009, SC-003/005)

**Checkpoint**: All user stories independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements and obligations affecting the whole sample

- [x] T018 Update `docs/trafficligth-architecture.md` to describe the CURRENT
      `jpnco.simula.samples.trafficlight` architecture (packages, actors,
      topics, tick flow, thread-safety, determinism, CLI) with Mermaid
      diagrams and NO historical information (Constitution Principle IX)
- [x] T019 Run `specs/001-trafficlight/quickstart.md` validation end to end
      (build, tests, console run, GUI run, determinism check)
- [x] T020 Confirm JaCoCo reports ≥97% line and branch coverage for the sample;
      add missing tests to close any gap (SC-006 / Principle II)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - US1 (P1) first (MVP); US2 (P2) and US3 (P3) after
- **Polish (Final Phase)**: Depends on all user stories complete

### User Story Dependencies

- **User Story 1 (P1)**: After Foundational - no dependencies on other stories
- **User Story 2 (P2)**: After Foundational - uses US1 states/actors; independently testable
- **User Story 3 (P3)**: After Foundational - exercises US1 demo; independently testable

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- States before actors; actors before demo wiring
- Story complete before moving to next priority

### Parallel Opportunities

- Setup T002 [P], all Foundational test tasks (T004-T006) [P]
- All US1 test tasks (T007-T009) [P] can run in parallel
- T013 [P] and T016 [P] can run in parallel with their story work

---

## Parallel Example: User Story 1

```bash
# Launch all US1 tests together (write FIRST, confirm FAIL):
Task: "CrossingTrafficLight band-state tests in CrossingTrafficLightTest.java"
Task: "Vehicle movement/stop/direction tests in VehicleTest.java"
Task: "TrafficCoordinator grouping/assembly tests in TrafficCoordinatorTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (JaCoCo + build check)
2. Complete Phase 2: Foundational (self-contained states + tests)
3. Complete Phase 3: User Story 1 (rewire actors + demo, tests green)
4. **STOP and VALIDATE**: Test US1 independently (console run + outcome)
5. Confirm ≥97% coverage on US1 scope

### Incremental Delivery

1. Complete Setup + Foundational -> Foundation ready
2. Add User Story 1 -> Test independently -> Deploy/Demo (MVP!)
3. Add User Story 2 -> Test independently -> Deploy/Demo
4. Add User Story 3 -> Test independently -> Deploy/Demo
5. Polish: architecture doc, quickstart, coverage gate

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
- Constitution Principle II: coverage ≥97% line AND branch applies to the sample (no exemption)
- Constitution Principle IX: sample architecture doc in `docs/` must stay current with no history

---

## Phase 7: Convergence

**Purpose**: Close the remaining gaps between the constitution's MUST principles and the
current implementation. All functional, plan, and coverage requirements are met; only the
two constitution-governance obligations below remain.

- [x] T021 Add FR/SC requirement citations to the Javadoc of every production class and
      method under `src/main/java/jpnco/simula/samples/trafficlight/` so each method's
      documentation names the functional-requirement or success-criterion identifiers
      (e.g. FR-003, SC-003) it participates in implementing, per Constitution Principle VI
- [x] T022 Declare and apply an automated code formatter for the sample (e.g. add
      `fmt-maven-plugin` or Spotless with google-java-format to `pom.xml`) and run it so the
      sample code conforms to the project-declared format, per Constitution Principle V

---

## Phase 8: Convergence

**Purpose**: Close the remaining gaps found by the convergence review between the feature
artifacts (spec/plan/tasks), the project constitution, and the current code. All functional,
plan, and coverage requirements are met; the items below are a constitution MUST violation and
an unrequested implementation change that lack artifact traceability.

- [x] T023 Create a current root `architecture.md` describing the system as implemented,
      with all structural diagrams as Mermaid diagrams and no historical information, per
      Constitution Principle VIII (missing)
- [x] T024 Record and justify the `Barrier`-based synchronization now used by
      `TrafficCoordinator` (two simula `Barrier` actors firing `LIGHTS_READY`/`VEHICLES_READY`
      and the `getLastState()` pull on `CrossingTrafficLight`/`Vehicle`) in `plan.md`/`spec.md`,
      or revert to the counter-based design, so the feature artifacts match the code per
      plan/tasks traceability (unrequested)
