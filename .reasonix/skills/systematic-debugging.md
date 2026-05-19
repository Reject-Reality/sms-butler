---
name: systematic-debugging
description: 4-phase root cause debugging: investigate → pattern analysis → hypothesis test → fix with failing test first
---
# Systematic Debugging

**Core principle:** ALWAYS find root cause before attempting fixes. Symptom fixes are failure.

**NO FIXES WITHOUT ROOT CAUSE INVESTIGATION FIRST**

## The Four Phases — Complete each before proceeding

### Phase 1: Root Cause Investigation

BEFORE attempting any fix:
1. **Read error messages carefully** — stack traces, line numbers, error codes
2. **Reproduce consistently** — exact steps, reliable trigger, or gather more data
3. **Check recent changes** — `git diff`, recent commits, config/environment changes
4. **Gather evidence** — for multi-component systems, add diagnostic logging at each boundary
5. **Trace data flow** — where does bad value originate? Trace backward to source

### Phase 2: Pattern Analysis

1. **Find working examples** — similar code in the same codebase
2. **Compare against references** — read reference implementations completely
3. **Identify differences** — what's different between working and broken?
4. **Understand dependencies** — components, settings, config, environment assumptions

### Phase 3: Hypothesis & Test

1. **Form single hypothesis** — "I think X is the root cause because Y"
2. **Test minimally** — smallest possible change to test hypothesis, one variable at a time
3. **Verify** — worked? Go to Phase 4. Didn't work? Form NEW hypothesis.
4. **If you don't know** — say "I don't understand X" and ask for help

### Phase 4: Implementation

1. **Create failing test case** — simplest reproduction, automated if possible
2. **Implement single fix** — address the root cause, ONE change at a time
3. **Verify fix** — test passes, no other tests broken, issue actually resolved
4. **If fix doesn't work** — STOP after 3 attempts and question the architecture

**If 3+ fixes failed:** This is an architectural problem. Discuss with the user before continuing.

## Quick Reference

| Phase | Key Activities | Success |
|-------|---------------|---------|
| 1. Root Cause | Read errors, reproduce, check changes, gather evidence | Understand WHAT and WHY |
| 2. Pattern | Find examples, compare references | Identify differences |
| 3. Hypothesis | Form theory, test minimally | Confirmed or new hypothesis |
| 4. Implementation | Create test, fix, verify | Bug resolved, tests pass |

## Red Flags — STOP and return to Phase 1

- "Quick fix for now, investigate later"
- "Just try changing X and see if it works"
- "Skip the test, I'll manually verify"
- "It's probably X, let me fix that"
- Proposing solutions before tracing data flow
- 3+ failed fix attempts without architectural discussion

## Common Rationalizations

| "Excuse" | Reality |
|----------|---------|
| "Issue is simple" | Simple bugs have root causes too |
| "Emergency" | Systematic is FASTER than guess-and-check |
| "Multiple fixes at once" | Can't isolate what worked |
| "One more fix" (after 2+) | 3+ failures = architectural problem |
