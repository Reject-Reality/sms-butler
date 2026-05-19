---
name: subagent-driven-development
description: Execute plan tasks with fresh context per task, two-stage review (spec compliance + code quality), continuous iteration
---
# Subagent-Driven Development

Execute plans by working through tasks sequentially with two-stage review after each: spec compliance first, then code quality.

**Core principle:** One task at a time + two-stage review (spec then quality) = high quality, fast iteration.

**Continuous execution:** Do NOT pause between tasks unless BLOCKED, genuinely ambiguous, or all tasks complete. "Should I continue?" wastes the user's time — they asked you to execute the plan.

## Process

1. **Read the plan file** — extract all tasks with full text, file paths, and context
2. **Create task tracker** — use `todo_write` with all tasks
3. **For each task:**
   a. **Read necessary context** — relevant files, existing code the task references
   b. **Implement** — follow TDD (write failing test first, then minimal code), use `test-driven-development` skill
   c. **Self-review against spec** — does the implementation match every requirement? Nothing extra?
   d. **Send for review** — use the `review` subagent skill for code quality review
   e. **Fix issues** — address review findings, re-review until clean
   f. **Mark task complete** — flip `todo_write` status to completed
4. **Final review** — review the whole implementation holistically
5. **Branch finish** — invoke `finishing-a-development-branch` skill

## Handling Blockers

| Status | Action |
|--------|--------|
| **DONE** | Proceed to review |
| **NEEDS_CONTEXT** | Research and provide context, then continue |
| **BLOCKED** | Assess: more context needed? Task too large? Break it up. Plan wrong? Escalate to user. |

Never force retry without changing the approach. If stuck, something needs to change.

## Task Debrief

After each task, answer these questions:
1. Is every spec requirement addressed?
2. Are tests passing?
3. Any concerns about the approach?

## Red Flags

- Starting on main/master without user consent
- Skipping review
- Moving to next task with open issues
- Parallel implementation tasks (risk of conflicts)
- Skipping TDD

## Required Skills

- `test-driven-development` — for implementation
- `review` (subagent) — for code quality review
- `finishing-a-development-branch` — when all tasks are done
