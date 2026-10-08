# Contract: Traffic Light Sample CLI

Phase 1 output of the `/speckit.plan` command. The sample exposes a command-line
interface through its runnable entry point.

## Invocation

```
java -cp <classpath> fr.jpnco.simula.samples.trafficlight.TrafficLightDemo [mode] [display]
```

Arguments are case-insensitive, order-independent, and optional.

| Argument | Values | Effect |
|----------|--------|--------|
| `mode` | `virtual` (default), `classic` | Selects `ExecutionMode.VIRTUAL` or `ExecutionMode.PLATFORM` |
| `display` | `console` (default), `gui` | Selects console rendering or a Swing window |

Unknown tokens are ignored (no error).

## Behavior

- Console mode: runs for a bounded number of simulated seconds, then prints a
  final outcome summary and exits.
- GUI mode: opens a Swing window that renders the grid, roads, lights, and
  vehicles in real time and runs until the window is closed.

## Outcome (console)

On completion the process prints a summary of the form:

```text
=== OUTCOME (<mode>) ===
vehicles=<n>, crossings=<m>
```

- `vehicles` = fixed fleet size.
- `crossings` = total per-cell vehicle arrivals across the run.

The printed values MUST be identical for `virtual` and `classic` given the same
fixed seed (SC-003).

## Exit behavior

- Console: exits cleanly (status 0) after printing the outcome; if the
  simulation does not complete within a bounded wait, it reports the timeout
  rather than hanging.
- GUI: exits when the window is closed.

## Non-goals

- No network interface, no persistence, no multi-user access.
