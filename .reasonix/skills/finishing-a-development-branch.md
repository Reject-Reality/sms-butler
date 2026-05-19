---
name: finishing-a-development-branch
description: Complete development branch: verify tests, present merge/PR/keep/discard options, execute choice
---
# Finishing a Development Branch

Guide completion of development work — verify tests, present options, execute choice, clean up.

**Announce:** "I'm using the finishing-a-development-branch skill to complete this work."

## Step 1: Verify Tests

Run the project's test suite: `run_command("<test-command>")`

**If tests fail:** Stop. Fix failures first. Don't proceed.

**If tests pass:** Continue.

## Step 2: Present Options

Use `ask_choice` to present exactly these 4 options:

> **A — Merge back to main locally**
> Checkout main, merge branch, verify tests on merged result, clean up worktree, delete branch.

> **B — Push and create a Pull Request**
> Push branch to origin, create PR via gh CLI. Preserve worktree for iteration.

> **C — Keep branch as-is**
> Leave everything in place. You'll handle it later.

> **D — Discard this work**
> Permanently delete the branch and all commits. Requires typed "discard" confirmation.

## Step 3: Execute Choice

### A — Merge locally
```bash
git checkout main
git pull
git merge <feature-branch>
<test-command>
git branch -d <feature-branch>
```

### B — Create PR
```bash
git push -u origin <feature-branch>
gh pr create --title "<title>" --body "<summary>"
```
Do NOT clean up worktree — user needs it for PR iteration.

### C — Keep
Report the branch name and path. No cleanup.

### D — Discard
Require typed "discard" confirmation. Then:
```bash
git branch -D <feature-branch>
```

## Red Flags

- Proceeding with failing tests
- Merging without verifying tests on merged result
- Deleting work without confirmation
- Force-pushing without explicit request
