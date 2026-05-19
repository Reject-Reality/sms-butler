---
name: using-git-worktrees
description: Create isolated git worktree for safe parallel development without affecting main branch
---
# Using Git Worktrees

Ensure work happens in an isolated workspace.

**Core principle:** Detect existing isolation first, use native tools, fall back to git.

## Step 0: Detect Existing Isolation

Check if already in an isolated workspace:
```
run_command("git rev-parse --git-dir")
run_command("git rev-parse --git-common-dir")
```

If already in a linked worktree, skip to project setup.

## Step 1: Ask User

If no existing isolation, ask the user: "Would you like me to set up an isolated git worktree? It protects your current branch."

If declined, work in place.

## Step 2: Create the Worktree

```bash
git worktree add .worktrees/<branch-name> -b <branch-name>
cd .worktrees/<branch-name>
```

If `git worktree add` fails (sandbox denial), work in place.

## Step 3: Project Setup

Auto-detect and run setup:
- `package.json` → `npm install`
- `Cargo.toml` → `cargo build`
- `requirements.txt` → `pip install -r requirements.txt`
- `go.mod` → `go mod download`

## Step 4: Verify Clean Baseline

Run tests: `run_command(<test-command>)`

Report: "Worktree ready at `<path>`. Tests passing. Ready to implement."

## Red Flags

- Creating a worktree when already isolated
- Skipping .gitignore verification for project-local worktrees
- Proceeding with failing baseline tests
