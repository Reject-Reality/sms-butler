---
name: receiving-code-review
description: Respond to code review feedback with verification and technical rigor, not performative agreement
---
# Receiving Code Review

How to respond to code review feedback — with technical rigor, not performative agreement.

**Core principle:** Verify before implementing. Ask before assuming. Technical correctness over social comfort.

## The Response Pattern

1. **READ** — Complete feedback without reacting
2. **UNDERSTAND** — Restate requirement in your own words
3. **VERIFY** — Check against codebase reality
4. **EVALUATE** — Technically sound for THIS codebase?
5. **RESPOND** — Technical acknowledgment or reasoned pushback
6. **IMPLEMENT** — One item at a time, test each

## Forbidden Responses

**NEVER:**
- "You're absolutely right!" (performative)
- "Great point!" / "Excellent feedback!"
- "Let me implement that now" (before verification)

**INSTEAD:**
- Restate the technical requirement
- Ask clarifying questions
- Push back with technical reasoning if wrong
- Just start working (actions > words)

## Handling Unclear Feedback

If any item is unclear: STOP. Ask for clarification on ALL unclear items before implementing anything. Items may be related — partial understanding = wrong implementation.

## Implementation Order

1. Clarify anything unclear FIRST
2. Then implement: blocking issues → simple fixes → complex fixes
3. Test each fix individually
4. Verify no regressions

## When to Push Back

Push back when:
- Suggestion breaks existing functionality
- Reviewer lacks full context
- Violates YAGNI (unused feature)
- Technically incorrect for this stack

Use technical reasoning, not defensiveness.

## Acknowledging Correct Feedback

When feedback IS correct:
- "Fixed. [Brief description]"
- "Good catch — [specific issue]. Fixed in [location]."

No thanks, no gratitude expressions. Actions speak.
