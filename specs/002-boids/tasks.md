---

description: "Task list for the Boids Flocking Sample feature"
---

# Tasks: Boids Flocking Sample

**Input**: Design documents from `/specs/002-boids/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests ARE included. The spec requires â‰¥97% line and branch coverage
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
- Sample code under `src/main/java/fr/jpnco/simula/samples/boids/`

## Phase 1: Foundational (Blocking Prerequisites)

**Purpose**: Self-contained `states` package that ALL user stories depend on

- [x] B001 Write failing unit tests for `states/BoidModel.java` (separation,
      alignment, cohesion, configurable weights/radius/max speed, speed
      limiting, toroidal wrap-around) in
      `src/test/java/fr/jpnco/simula/samples/boids/states/BoidModelTest.java`
- [x] B002 Write failing unit tests for `states/FlockState.java`,
      `states/BoidView.java`, `states/BoidState.java` (getters, unmodifiable
      copy, immutability) in `src/test/java/fr/jpnco/simula/samples/boids/states/`
      (`FlockStateTest.java`, `BoidViewTest.java`, `BoidStateTest.java`)

**Checkpoint**: Foundation ready - the states package is self-contained and tested.

---

## Phase 2: User Story 1 - Run the flocking simulation (Priority: P1) MVP

**Goal**: Launch the console boids simulation end to end; it advances one tick
per simulated second, stops at the bounded duration, prints an outcome summary,
and exits cleanly (FR-001..FR-005; SC-001, SC-002).

### Tests for User Story 1 (write FIRST, confirm FAIL before implementation)

- [x] B003 [P] Write failing unit tests for `actors/Boid.java` (movement,
      velocity update, toroidal wrap-around) in
      `src/test/java/fr/jpnco/simula/samples/boids/actors/BoidTest.java`
- [x] B004 [P] Write failing unit tests for `actors/BoidsCoordinator.java`
      (tick grouping waits for all reports, snapshot assembly, duration stop,
      crossing counters) in
      `src/test/java/fr/jpnco/simula/samples/boids/actors/BoidsCoordinatorTest.java`

### Implementation for User Story 1

- [x] B005 [US1] Implement `states/BoidModel.java` and the `states/` package
- [x] B006 [US1] Implement `actors/Boid.java`, `actors/BoidsCoordinator.java`,
      `actors/Topics.java`, `actors/BoidsMonitor.java`
- [x] B007 [US1] Implement `BoidsDemo.java` (console run + outcome summary) and
      `BoidsCli.java`; verify console flow end to end (FR-001..005, SC-001/002)

**Checkpoint**: User Story 1 fully functional and testable independently (MVP).

---

## Phase 3: User Story 2 - Visualize the flock in a window (Priority: P2)

**Goal**: Open a graphical window that renders the moving boids and repaints in
real time until closed (FR-006; SC-004).

### Implementation for User Story 2

- [x] B008 [P] [US2] Write a focused unit test for `actors/BoidsGui.java` that
      the actor subscribes to `new-state` and stores the latest `FlockState`
      (lightweight, no full Swing event loop) in
      `src/test/java/fr/jpnco/simula/samples/boids/actors/BoidsGuiTest.java`
- [x] B009 [US2] Implement `actors/BoidsGui.java` (Swing rendering of the flock)
- [x] B010 [US2] Verify the GUI flow: window opens, renders moving boids,
      repaints on new states, stops when closed (FR-006, SC-004)

**Checkpoint**: User Stories 1 AND 2 both work independently.

---

## Phase 4: User Story 3 - Compare execution modes (Priority: P3)

**Goal**: The same seeded scenario produces an identical final outcome in both
execution modes (FR-007, FR-008; SC-003, SC-005).

### Tests for User Story 3

- [x] B011 [P] [US3] Write a determinism test that runs the same seeded scenario
      in both `ExecutionMode.VIRTUAL` and `ExecutionMode.PLATFORM` and asserts
      the final outcome (total distance) is identical, in
      `src/test/java/fr/jpnco/simula/samples/boids/DeterminismTest.java`

### Implementation for User Story 3

- [x] B012 [US3] Verify seeded reproducibility: `BoidsCli` argument handling and
      fixed `RANDOM_SEED` yield an identical outcome across modes (FR-007/008,
      SC-003/005)

**Checkpoint**: All user stories independently functional.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Improvements and obligations affecting the whole sample

- [x] B013 Create `docs/boids-architecture.md` describing the CURRENT boids
      architecture (packages, actors, topics, tick flow, thread-safety,
      determinism, CLI) with Mermaid diagrams and NO historical information
      (Constitution Principle IX)
- [x] B014 Update the root `architecture.md` to include the new boids sample
      (Constitution Principle VIII)
- [x] B015 Run `specs/002-boids/quickstart.md` validation end to end (build,
      tests, console run, GUI run, determinism check)
- [x] B016 Confirm JaCoCo reports â‰¥97% line and branch coverage for the sample;
      add missing tests to close any gap (SC-006 / Principle II)
- [x] B017 Add FR/SC requirement citations to the Javadoc of every production
      class and method under `src/main/java/fr/jpnco/simula/samples/boids/`
      (Constitution Principle VI)
- [x] B018 Apply the automated code formatter (`mvn fmt:format`) so the sample
      conforms to the project-declared format (Constitution Principle V)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Foundational (Phase 1)**: No dependencies - can start immediately
- **User Stories (Phase 2+)**: All depend on Foundational phase completion
  - US1 (P1) first (MVP); US2 (P2) and US3 (P3) after
- **Polish (Final Phase)**: Depends on all user stories complete

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- States before actors; actors before demo wiring
- Story complete before moving to next priority

### Parallel Opportunities

- All Foundational test tasks (B001-B002) [P]
- All US1 test tasks (B003-B004) [P] can run in parallel
- B008 [P] and B011 [P] can run in parallel with their story work

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Constitution Principle II: coverage �%�97% line AND branch applies to the sample
- Constitution Principle IX: sample architecture doc in `docs/` must stay current

---

## Phase 6: Convergence

- [x] B019 Expose the three flocking rule weights via the CLI (`--separation-weight=`, `--alignment-weight=`, `--cohesion-weight=`) in `actors/BoidsCli.resolveParameters` so FR-009's "configurable at runtime" holds on the command-line interface too, and add tests (`BoidsCliTest`) covering the new overrides per FR-009 (partial)
- [ ] B020 Document the unrequested GUI enhancements — the five live parameter sliders (with boundary/current value labels) and the direction-oriented triangle rendering of the boids — in `docs/boids-architecture.md` (and update the FR-009/SC-004 references there and in the spec/plan if applicable), or remove them, per SC-004 / Constitution VI (unrequested)

