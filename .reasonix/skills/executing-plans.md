---
name: executing-plans
description: Execute implementation plans task-by-task with checkpoints and human review gates
---
# Executing Plans

Execute a written implementation plan task-by-task with checkpoints.

**Announce:** "I'm using the executing-plans skill to implement this plan."

## Process

### Step 1: Load and Review Plan
1. Read the plan file
2. Review critically — identify any concerns
3. Create `todo_write` with all tasks

### Step 2: Execute Tasks

For each task:
1. Mark as `in_progress` in todo_write
2. Follow each step exactly (the plan has bite-sized steps)
3. Run verifications as specified using `run_command`
4. When stuck: STOP and ask for help — don't guess
5. Mark as `completed` in todo_write

### Step 3: Complete Development

After all tasks complete:
- Invoke `finishing-a-development-branch` skill
- Verify tests, present merge/PR/keep/discard options

## When to Stop

- Hit a blocker (missing dependency, test fails, instruction unclear)
- Plan has critical gaps preventing progress
- Verification fails repeatedly

## Red Flags

- Skipping verifications
- Starting on main/master without user consent
- Forcing through blockers — stop and ask
