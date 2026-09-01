# Plan Check-off

## When an implementation plan (IP) is finished

* Mark the IP as `COMPLETED` in **every** place it appears in the Feature Plan:
    * the Implementation Plan Overview table (section 6)
    * the heading of its own section (section 7)
    * the Dependency Graph (section 8)
    * any list of completed plans, if present
* Mark the IP as `COMPLETED` in the Feature status file and recalculate overall progress.
* Mark the IP as `COMPLETED` in its own IP status file.
* In the IP's Feature Plan section, note where the delivered result differs from the plan
  (moved module boundary, widened constant, changed order).
* Remove the finished IP's plan and status files with `git rm`; update `FP-<NNN>-Overview.md`.
* Never remove or tick an IP that is not actually finished.

## Verifying "already implemented"

* An IP counts as implemented only if its IP status file says `COMPLETED`.
* Do not infer completion from source code or git history alone.
