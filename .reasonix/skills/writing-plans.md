---
name: writing-plans
description: Create detailed bite-sized implementation plans from a spec — exact files, code, tests, commands
---
# Writing Plans

Write comprehensive implementation plans. Assume the executor has zero project context and questionable taste. Document everything: exact file paths, code, tests, verification commands. DRY. YAGNI. TDD. Frequent commits.

**Announce:** "I'm using the writing-plans skill to create the implementation plan."

**Save plans to:** `docs/superpowers/plans/YYYY-MM-DD-<feature-name>.md`

## Scope Check

If the spec covers multiple independent subsystems, suggest breaking into separate plans — one per subsystem. Each plan should produce working, testable software on its own.

## File Structure

Before defining tasks, map out which files will be created or modified:
- Design units with clear boundaries and well-defined interfaces
- Prefer smaller, focused files over large ones
- Files that change together should live together
- Follow existing patterns in the codebase

## Bite-Sized Task Granularity

Each step is one action (2-5 minutes):
- "Write the failing test" - one step
- "Run it to verify it fails" - one step
- "Implement minimal code to pass" - one step
- "Run tests to verify pass" - one step
- "Commit" - one step

## Plan Header

```markdown
# [Feature] Implementation Plan

**Goal:** One sentence

**Architecture:** 2-3 sentences

**Tech Stack:** Key technologies
---
```

## Task Structure

Each task in the plan:
- Files to create/modify with exact paths
- Steps with checkbox syntax `- [ ]`
- Complete code in every step (no placeholders)
- Exact commands with expected output
- Test-first approach

## No Placeholders — NEVER write:
- "TBD", "TODO", "implement later"
- "Add appropriate error handling" (show the handling)
- "Write tests for the above" (show the test code)
- "Similar to Task N" (repeat the code)
- Steps that describe without showing code

## Self-Review

After writing the plan:
1. **Spec coverage** — can every spec requirement point to a task?
2. **Placeholder scan** — search for "TBD", "TODO", vague phrases
3. **Type consistency** — do method signatures match across tasks?

Fix issues inline. If a spec requirement has no task, add one.

## Execution Handoff

After saving the plan, offer:

> "Plan saved to `<path>`. How should I execute? (A) Subagent-driven — dispatch per task with review, or (B) Inline execution with checkpoints"

If user chooses subagent-driven, invoke `run_skill("subagent-driven-development", "...")`.
If inline, invoke `run_skill("executing-plans", "...")`.
