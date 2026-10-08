# Contract: Dashboard Sample CLI

Phase 1 output of the `/speckit.plan` command. The sample exposes a command-line
interface through its runnable entry point.

## Invocation

```
java -cp <classpath> fr.jpnco.simula.samples.dashboard.DashboardDemo [target] [mode] [display] [export=<path>]
```

Arguments are case-insensitive, order-independent, and optional.

| Argument | Values | Effect |
|----------|--------|--------|
| `target` | `trafficlight`, `boids` | Which existing simulation to attach the dashboard to (default `boids`) |
| `mode` | `virtual` (default), `classic` | Selects `ExecutionMode.VIRTUAL` or `ExecutionMode.PLATFORM` |
| `display` | `console` (default), `gui` | Selects console summary or a Swing plot window |
| `export=<path>` | any file path | Enables writing the collected series to a CSV file at the given path |

Unknown tokens are ignored (no error).

## Behavior

- The sample builds the selected target simulation on its own root engine (using
  the existing sample's coordinator/actors) and registers the `Dashboard` actor
  alongside it before `root.start()`.
- Console mode: runs for a bounded number of simulated seconds, records one sample
  per tick, prints a per-tick line and a final summary, then exits.
- GUI mode: opens a Swing window that plots the collected series and updates in
  real time; runs until the window is closed.
- When `export=<path>` is given, the collected series is written to the file on
  completion (console) or on window close (GUI).

## Outcome (console)

On completion the process prints a summary of the form:

```text
=== OUTCOME (<target>, <mode>) ===
samples=<n>, first=<simTime0>, last=<simTimeN>
```

- `samples` = number of recorded samples (one per tick).
- `first` / `last` = simulated time of the first / last recorded sample.
- If no states were collected, the sample reports an empty series (`samples=0`)
  rather than failing.

## Export format

Each line of the export file is one sample: `simTime,value` (a header line is
written first). The format is documented and consistent (FR-006).

## Exit behavior

- Console: exits cleanly (status 0) after printing the outcome; if the simulation
  does not complete within a bounded wait, it reports the timeout rather than
  hanging.
- GUI: exits when the window is closed.

## Non-goals

- No network interface, no persistence beyond the optional file export, no
  multi-user access.
