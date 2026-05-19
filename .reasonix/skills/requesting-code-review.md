---
name: requesting-code-review
description: Pre-review checklist before asking for code review — verify completeness, test coverage, and readiness
---
# Requesting Code Review

Review early, review often. Before asking for a review, run through this checklist to catch obvious issues yourself.

## When to Request Review

**Mandatory:**
- After each task in subagent-driven development
- After completing a major feature
- Before merge to main

**Optional but valuable:**
- When stuck (fresh perspective)
- Before refactoring (baseline check)
- After fixing a complex bug

## Pre-Review Checklist

Before dispatching a review:

1. **Run tests** — `run_command` to ensure the test suite passes
2. **Read your diff** — `git diff` or `run_command("git diff --stat")` to see what changed
3. **Check against the plan** — does the implementation match every spec requirement?
4. **Check for leftover debug code** — no `console.log`, `debugger`, `TODO`, or commented-out code
5. **Check for YAGNI violations** — no features that weren't requested

## How to Request Review

1. **Run tests** to confirm everything passes
2. **Get the diff** context:
   ```
   run_command("git log --oneline -5")
   run_command("git diff --stat HEAD~1")
   ```
3. **Invoke review** — use the built-in `review` subagent skill:
   ```
   run_skill("review", "Review the changes for task X: <summary>. Focus on: spec compliance, code quality, edge cases.")
   ```
4. **Act on feedback:**
   - Fix Critical issues immediately
   - Fix Important issues before proceeding
   - Note Minor issues for later
   - Push back with reasoning if the reviewer is wrong

## Red Flags

- Skipping review because "it's simple"
- Ignoring Critical issues
- Proceeding with unfixed Important issues
- Leaving debug code in the diff
