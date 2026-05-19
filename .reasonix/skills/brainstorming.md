---
name: brainstorming
description: Design refinement before coding: explore context, clarify intent, propose approaches, get approval, write spec
---
# Brainstorming Ideas Into Designs

Turn ideas into fully formed designs and specs through collaborative dialogue.

**HARD GATE:** Do NOT write any code, scaffold anything, or take any implementation action until you have presented a design AND the user has approved it. This applies to EVERY project regardless of perceived simplicity.

## Anti-Pattern: "This Is Too Simple To Need A Design"

Every project goes through this. A todo list, a single-function utility, a config change — all of them. "Simple" projects are where unexamined assumptions cause the most wasted work.

## Checklist (complete in order)

1. **Explore project context** — use `read_file`, `list_directory`, `search_content`, `git log` to understand the current state
2. **Ask clarifying questions** — one at a time. Understand purpose, constraints, success criteria
3. **Propose 2-3 approaches** — with trade-offs and your recommendation
4. **Present design** — in sections scaled to complexity, get user approval after each section
5. **Write design doc** — save to `docs/superpowers/specs/YYYY-MM-DD-<topic>-design.md`
6. **Spec self-review** — check for placeholders, contradictions, ambiguity
7. **User reviews spec** — ask user to review before proceeding
8. **Transition** — invoke `writing-plans` skill (or `submit_plan` tool) to create implementation plan

## Key Principles

- **One question at a time** — don't overwhelm with multiple questions
- **Multiple choice preferred** — use `ask_choice` tool when possible
- **YAGNI ruthlessly** — remove unnecessary features from all designs
- **Explore alternatives** — always propose 2-3 approaches before settling
- **Incremental validation** — present design sections, get approval before moving on
- **Be flexible** — go back and clarify when something doesn't make sense

## Design for Isolation

Break the system into smaller units that each have one clear purpose, communicate through well-defined interfaces, and can be understood and tested independently. For each unit you should be able to answer: what does it do, how do you use it, and what does it depend on?

## Working in Existing Codebases

- Explore the current structure before proposing changes. Follow existing patterns.
- If a file has grown unwieldy, include targeted improvements — but don't propose unrelated refactoring.

## Spec Self-Review

After writing the spec:
1. **Placeholder scan:** Any "TBD", "TODO", incomplete sections? Fix them.
2. **Internal consistency:** Do any sections contradict each other?
3. **Scope check:** Is this focused enough for a single implementation plan?
4. **Ambiguity check:** Could any requirement be interpreted two different ways?

## User Review Gate

After the self-review passes, use `ask_choice` or a direct message:
> "Spec written to `<path>`. Please review it and let me know if you want changes before we start the implementation plan."

Only proceed once the user approves.
