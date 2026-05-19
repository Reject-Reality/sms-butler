---
name: verification-before-completion
description: Never claim completion without fresh verification — run the command, read output, THEN report
---
# Verification Before Completion

**Core principle:** Evidence before claims. Always.

**NO COMPLETION CLAIMS WITHOUT FRESH VERIFICATION EVIDENCE**

If you haven't run the verification command in this message, you cannot claim it passes.

## The Gate Function

BEFORE claiming any status or expressing satisfaction:

1. **IDENTIFY** — What command proves this claim?
2. **RUN** — Execute the FULL command (fresh, complete) using `run_command`
3. **READ** — Full output, check exit code, count failures
4. **VERIFY** — Does output confirm the claim?
   - No: State actual status with evidence
   - Yes: State claim WITH evidence
5. **ONLY THEN** — Make the claim

Skip any step = misreporting, not verifying.

## Common Verification Gates

| Claim | Requires |
|-------|----------|
| Tests pass | `run_command("<test>")` output: 0 failures |
| Linter clean | `run_command("<linter>")` output: 0 errors |
| Build succeeds | `run_command("<build>")` exit 0 |
| Bug fixed | Test original symptom: passes |
| Requirements met | Line-by-line checklist matching the spec |

## Red Flags

- Using "should", "probably", "seems to"
- Expressing satisfaction before verification ("Great!", "Perfect!", "Done!")
- About to commit/push/PR without verification
- Trusting subagent success reports without checking `git diff`
- Thinking "just this once"
- ANY wording implying success without having run verification

## Key Pattern

```
❌ "Should pass now" / "Looks correct"
✅ run_command("<test>") → "All 34/34 tests pass"
```
