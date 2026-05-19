---
name: test-driven-development
description: RED-GREEN-REFACTOR cycle: write failing test first, minimal code to pass, then refactor
---
# Test-Driven Development (TDD)

Write the test first. Watch it fail. Write minimal code to pass. Refactor.

**Core principle:** If you didn't watch the test fail, you don't know if it tests the right thing.

## The Iron Law

```
NO PRODUCTION CODE WITHOUT A FAILING TEST FIRST
```

Write code before the test? Delete it. Start over. No exceptions.

## Red-Green-Refactor

### RED — Write Failing Test

Write one minimal test showing what should happen:
- One behavior per test
- Clear name describing the behavior
- Use real code (no mocks unless unavoidable)

### Verify RED — Watch It Fail

**MANDATORY. Never skip.**

Use `run_command` to run the test. Confirm:
- Test fails (not errors)
- Failure message is expected
- Fails because feature is missing, not due to typos

If test passes → you're testing existing behavior. Fix the test.
If test errors → fix error, re-run until it fails correctly.

### GREEN — Write Minimal Code

Write the simplest code to pass the test. Don't add features, refactor other code, or "improve" beyond what the test requires. YAGNI.

### Verify GREEN — Watch It Pass

**MANDATORY.** Use `run_command` to run the test. Confirm:
- New test passes
- All other tests still pass
- Clean output (no warnings or errors)

If test fails → fix code, not the test.
If other tests fail → fix them now.

### REFACTOR — Clean Up

Only after green:
- Remove duplication
- Improve names
- Extract helpers

Keep tests green throughout. Don't add behavior.

### Repeat

Next failing test for the next feature. Continue until all functionality is covered.

## Good Tests

| Quality | Good | Bad |
|---------|------|-----|
| Minimal | One thing. "and" in name? Split it. | `test('validates email and domain')` |
| Clear | Name describes behavior | `test('test1')` |
| Shows intent | Demonstrates desired API | Obscures what code should do |

## Test Execution in Reasonix Code

For running tests, use `run_command` with the project's test runner:
```
run_command("npm test -- path/to/test.test.ts")
run_command("pytest tests/test_file.py -v")
run_command("cargo test")
```

For long test suites, use `run_background` + `wait_for_job`.

## Verification Checklist

Before marking work complete:
- [ ] Every new function/method has a test
- [ ] Watched each test fail before implementing
- [ ] Wrote minimal code to pass each test
- [ ] All tests pass
- [ ] Clean output (no errors/warnings)
- [ ] Tests use real code (mocks only if unavoidable)

## When Stuck

| Problem | Solution |
|---------|----------|
| Don't know how to test | Write the wished-for API. Write assertion first. |
| Test too complicated | Design too complicated. Simplify interface. |
| Must mock everything | Code too coupled. Use dependency injection. |
| Test setup huge | Extract helpers. Still complex? Simplify design. |

## Final Rule

```
Production code → test exists and failed first
Otherwise → not TDD
```

No exceptions without the user's explicit permission.
