---
name: dispatching-parallel-agents
description: Dispatch independent issues (test failures, subsystems) to parallel subagents for concurrent investigation
---
# Dispatching Parallel Agents

Dispatch one subagent per independent problem domain. Let them work concurrently.

**Core principle:** When you have multiple unrelated issues (different subsystems, different test files, different bugs), investigating them sequentially wastes time.

## When to Use

**Use when:**
- 3+ independent issues with different root causes
- Each issue can be understood without context from others
- No shared state between investigations

**Don't use when:**
- Issues are related (fixing one might fix others)
- Need to understand full system state
- Agents would interfere with each other (editing same files)

## The Pattern

### 1. Identify Independent Domains
Group failures by what's broken. Each domain is independent.

### 2. Create Focused Agent Tasks
Each subagent gets:
- **Specific scope** — one test file or subsystem
- **Clear goal** — what to achieve
- **Constraints** — what NOT to change
- **Expected output** — summary of findings and changes

### 3. Dispatch in Parallel
Use `run_skill` with a subagent for each domain:
```
run_skill("explore", "Investigate and fix test failures in <file A>: <details>")
run_skill("explore", "Investigate and fix test failures in <file B>: <details>")
```

Note: Subagent skills run sequentially in Reasonix Code but each gets isolated context.

### 4. Review and Integrate
- Read each subagent's summary
- Verify fixes don't conflict
- Run full test suite
- Integrate all changes

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| Too broad scope | One test file or subsystem per agent |
| No context provided | Include error messages and test names |
| No constraints | Specify what NOT to change |
| Vague output expectations | "Return summary of root cause and changes" |

## Red Flags

- Issues are related (fix one might fix others)
- Agents would edit the same files (conflicts)
- Need to understand full system state first
