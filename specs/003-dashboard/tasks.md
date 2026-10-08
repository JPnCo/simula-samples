---

description: "Task list for the Dashboard Simula Sample feature"
---

# Tasks: Dashboard Simula Sample

**Input**: Design documents from `/specs/003-dashboard/`

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

- **Single project**: `src/main/java/`, `src/test/java/` at repository root
- Sample code under `src/main/java/fr/jpnco/simula/samples/dashboard/`

## Phase 1: Foundational (Blocking Prerequisites)

**Purpose**: Self-contained `model` + parameters package that ALL user stories
depend on.

- [x] D001 Write failing unit tests for `model/Sample.java` (immutable record
      with `simTime` int and `value` double; getters), `model/TimeSeries.java`
      (`append`, `size`, `first`, `last`, `isEmpty`, immutability), and
      `model/MetricExtractor.java` (extracts `getVehicleCount()` from `GridState`
      and `getBoidCount()` from `FlockState`) in
      `src/test/java/fr/jpnco/simula/samples/dashboard/model/`
      (`SampleTest.java`, `TimeSeriesTest.java`, `MetricExtractorTest.java`)
- [x] D002 Write failing unit tests for `DashboardParameters.java` (immutable
      configuration: execution mode, target sample, display, optional export path;
      defaults and getters) in
      `src/test/java/fr/jpnco/simula/samples/dashboard/DashboardParametersTest.java`

**Checkpoint**: Foundation ready - the model and parameters packages are
self-contained and tested.

---

## Phase 2: User Story 1 - Attach a dashboard and collect a time series (Priority: P1) MVP

**Goal**: Launch an existing simulation (trafficlight or boids) with the
dashboard attached; it records one sample per tick from the `new-state` topic,
prints a console summary, and exits cleanly (FR-001..FR-004, FR-007..FR-009;
SC-001, SC-002).

**Independent Test**: Launch a bounded console run (e.g. `DashboardDemo boids
console`); verify one sample is recorded per tick with monotonically increasing
simulated time and a summary `samples=<n>, first=<t0>, last=<tN>` is printed,
then the process exits cleanly.

### Tests for User Story 1 (write FIRST, confirm FAIL before implementation)

- [x] D003 [P] Write failing unit tests for `actors/Dashboard.java` (subscribes
      to `new-state`, reads `event.getParameters()[0]`, records a `Sample` per
      received snapshot, feeds the `TimeSeries`, handles an empty series) in
      `src/test/java/fr/jpnco/simula/samples/dashboard/actors/DashboardTest.java`
- [x] D004 [P] Write failing unit tests for `actors/DashboardMonitor.java`
      (renders a per-tick line from a `Sample` and a final summary from the
      `TimeSeries`, including an empty series) in
      `src/test/java/fr/jpnco/simula/samples/dashboard/actors/DashboardMonitorTest.java`

### Implementation for User Story 1

- [x] D005 [US1] Implement the `model/` package: `model/Sample.java`,
      `model/TimeSeries.java`, `model/MetricExtractor.java`
- [x] D006 [US1] Implement `actors/Dashboard.java`, `actors/DashboardMonitor.java`,
      and a `Topics`/`NEW_STATE` constant (reuse the target sample's `new-state`
      topic string) under
      `src/main/java/fr/jpnco/simula/samples/dashboard/actors/`
- [x] D007 [US1] Implement `DashboardCli.java`, `DashboardParameters.java`, and
      `DashboardDemo.java` (builds the target simulation on its own root engine
      using the existing sample's coordinator/actors, registers the `Dashboard`
      before `root.start()`, console run + summary) and verify the console flow
      end to end (FR-001..004, FR-007..009, SC-001/002)

**Checkpoint**: User Story 1 fully functional and testable independently (MVP).

---

## Phase 3: User Story 2 - Render the time series in a window (Priority: P2)

**Goal**: Open a graphical window that plots the collected time series and
repaints in real time until closed (FR-005; SC-004).

**Independent Test**: Launch with the GUI display requested (e.g. `DashboardDemo
boids gui`); verify a window opens that plots the collected samples and updates
over time until the window is closed.

### Implementation for User Story 2

- [x] D008 [P] [US2] Write a focused unit test for `actors/DashboardGui.java`
      that the actor subscribes to `new-state` and stores the latest
      `TimeSeries` (lightweight, no full Swing event loop) in
      `src/test/java/fr/jpnco/simula/samples/dashboard/actors/DashboardGuiTest.java`
- [x] D009 [US2] Implement `actors/DashboardGui.java` (Swing plot: JFrame + a
      `GridPanel` subclass painting the series with Java2D + a ~100 ms `Timer`
      repaint loop, following the `BoidsGui` pattern)
- [x] D010 [US2] Wire the GUI into `DashboardDemo`/`DashboardCli` and verify the
      GUI flow: window opens, plots samples, repaints on new states, stops when
      closed (FR-005, SC-004)

**Checkpoint**: User Stories 1 AND 2 both work independently.

---

## Phase 4: User Story 3 - Export the time series to a file (Priority: P3)

**Goal**: Save the collected time series to a machine-readable file, one
`simTime,value` line per tick (FR-006; SC-005).

**Independent Test**: Run a bounded console run with `export=<path>`; verify a
file is written with a header line and one `simTime,value` line per recorded
tick, and that an empty run reports nothing was exported.

### Tests for User Story 3

- [x] D011 [P] [US3] Write failing unit tests for `export/CsvExporter.java`
      (writes a header line then one `simTime,value` line per sample; handles an
      empty series without writing a malformed file) in
      `src/test/java/fr/jpnco/simula/samples/dashboard/export/CsvExporterTest.java`

### Implementation for User Story 3

- [x] D012 [US3] Implement `export/CsvExporter.java`
- [x] D013 [US3] Wire export into `DashboardCli`/`DashboardDemo` and verify the
      file is written on completion with the documented format (FR-006, SC-005;
      see `contracts/dashboard-cli.md`)

**Checkpoint**: All user stories independently functional.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Improvements and obligations affecting the whole sample

- [x] D014 Create `docs/dashboard-architecture.md` describing the CURRENT
      dashboard architecture (packages, actors, topics, sampling flow,
      thread-safety, CLI, export) with Mermaid diagrams and NO historical
      information (Constitution Principle IX)
- [x] D015 Update the root `architecture.md` to include the new dashboard sample
      (Constitution Principle VIII)
- [x] D016 Run `specs/003-dashboard/quickstart.md` validation end to end (build,
      tests, console run, GUI run, export, cross-sample check)
- [x] D017 Confirm JaCoCo reports ≥97% line and branch coverage for the sample;
      add `DashboardDemo.class`, `DashboardGui.class`, and
      `DashboardGui$GridPanel.class` to the JaCoCo excludes in `pom.xml` (as the
      existing GUI/demo classes are); add missing tests to close any gap
      (SC-006 / Principle II)
- [x] D018 Add FR/SC requirement citations to the Javadoc of every production
      class and method under `src/main/java/fr/jpnco/simula/samples/dashboard/`
      (Constitution Principle VI)
- [x] D019 Apply the automated code formatter (`mvn fmt:format`) so the sample
      conforms to the project-declared format (Constitution Principle V)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Foundational (Phase 1)**: No dependencies - can start immediately
- **User Stories (Phase 2+)**: All depend on Foundational phase completion
  - US1 (P1) first (MVP); US2 (P2) and US3 (P3) after
- **Polish (Final Phase)**: Depends on all user stories complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 1) - No dependencies
  on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 1) - builds on US1's
  `Dashboard`/`TimeSeries` but is independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 1) - builds on US1's
  `TimeSeries` but is independently testable

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Models before actors; actors before demo wiring
- Story complete before moving to next priority

### Parallel Opportunities

- All Foundational test tasks (D001-D002) [P]
- All US1 test tasks (D003-D004) [P] can run in parallel
- D008 [P] and D011 [P] can run in parallel with their story work

---

## Parallel Example: User Story 1

```bash
# Launch the US1 test tasks together (write FIRST, confirm FAIL):
Task: "Write failing unit tests for actors/Dashboard.java"
Task: "Write failing unit tests for actors/DashboardMonitor.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Foundational
2. Complete Phase 2: User Story 1
3. **STOP and VALIDATE**: Test User Story 1 independently
4. Deploy/demo if ready

### Incremental Delivery

1. Complete Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Constitution Principle II: coverage ≥97% line AND branch applies to the sample
- Constitution Principle IX: sample architecture doc in `docs/` must stay current
- The dashboard is a self-contained observer actor; `SimulaSupervisor` does not
  exist in the framework and is NOT used (see `research.md`)

---

## Phase 6: Convergence

- [ ] T020 Replace the raw numeric literals in
      `src/main/java/fr/jpnco/simula/samples/dashboard/actors/DashboardGui.java`
      (window-height offset 40, status border insets 8/12, status font size 14,
      plot stroke width 2.0, value-span epsilon 1e-9, padding multiplier 2,
      colour components) with named constants per Constitution VII (contradicts)
- [ ] T021 Print the contract-specified `=== OUTCOME (<target>, <mode>) ===`
      summary header before the `samples=…, first=…, last=…` line on console
      runs in `DashboardMonitor`/`DashboardDemo` per contracts/dashboard-cli.md
      Outcome (FR-004, FR-009) (partial)
- [ ] T022 Write the collected series to the `export=<path>` file when the GUI
      window is closed: thread `params.getExportPath()` through
      `DashboardDemo.runGui` and `DashboardGui` per
      contracts/dashboard-cli.md Behavior and the window+export edge case
      (FR-006) (missing)
- [ ] T023 Report that nothing was exported and skip writing the file when the
      collected series is empty in
      `DashboardDemo.exportIfRequested`/`export/CsvExporter.java` and update
      `CsvExporterTest` per US3/AC3 (FR-006, FR-008) (partial)
- [ ] T024 Handle an unknown snapshot payload on `new-state` without failing the
      run (record nothing or skip with a warning instead of throwing) in
      `model/MetricExtractor.java`/`actors/Dashboard.java` per the spec Edge
      Cases for unsupported simulations (FR-007, FR-008) (partial)
- [ ] T025 Review the unrequested `src/main/java/module-info.java` (absent from
      the plan's project structure and not exporting
      `fr.jpnco.simula.samples.dashboard`): add the dashboard export or remove the
      file, and reconcile with the plan per plan: project structure
      (unrequested)
- [ ] T026 Review the untracked `jars/` directory (not referenced by the plan):
      remove it or justify and gitignore it per plan: project structure
      (unrequested)
