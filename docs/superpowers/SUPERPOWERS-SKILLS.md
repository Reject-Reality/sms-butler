# Superpowers Skills for Reasonix Code

> 将 obra/superpowers 方法论移植为 Reasonix Code 技能集

## 概述

本套技能将 [Superpowers](https://github.com/obra/superpowers) 的完整软件开发方法论移植到了 Reasonix Code 的 Skills 系统中。包含 12 个可组合的技能，覆盖从需求讨论到分支收尾的完整开发流程。

### 为什么是 "Superpowers"？

Superpowers 是一套帮助 AI 编程智能体**先思考后编码**的方法论——在写代码之前先讨论设计、制定计划、写好测试，然后有条不紊地执行。它不是约束，而是赋予你的智能体**系统性地工作**的能力。

---

## 技能总览

### 🔄 核心工作流（按执行顺序）

| 步骤 | 技能 | 调用方式 | 作用 |
|------|------|----------|------|
| 1 | **brainstorming** | `run_skill("brainstorming", "...")` | 需求讨论 → 设计文档 |
| 2 | **writing-plans** | `run_skill("writing-plans", "...")` | 设计文档 → 实现计划 |
| 3 | **subagent-driven-development** | `run_skill("subagent-driven-development", "...")` | 实现计划 → 任务执行 |
| 4 | **executing-plans** | `run_skill("executing-plans", "...")` | 实现计划 → 人工检查点执行 |
| 5 | **finishing-a-development-branch** | `run_skill("finishing-a-development-branch")` | 任务完成 → 合并/PR |

### 🛠 开发辅助

| 技能 | 调用方式 | 作用 |
|------|----------|------|
| **test-driven-development** | `run_skill("test-driven-development", "...")` | RED-GREEN-REFACTOR 完整循环 |
| **requesting-code-review** | `run_skill("requesting-code-review", "...")` | 提交审查前的自查 |
| **receiving-code-review** | `run_skill("receiving-code-review", "...")` | 如何响应审查反馈 |
| **using-git-worktrees** | `run_skill("using-git-worktrees", "...")` | 创建隔离工作分支 |

### 🔍 调试与验证

| 技能 | 调用方式 | 作用 |
|------|----------|------|
| **systematic-debugging** | `run_skill("systematic-debugging", "...")` | 4 阶段根因调试 |
| **verification-before-completion** | `run_skill("verification-before-completion")` | 声明完成前必须验证 |
| **dispatching-parallel-agents** | `run_skill("dispatching-parallel-agents", "...")` | 并发独立问题调查 |

---

## 详细说明

### 1. brainstorming — 设计讨论

**用途：** 在写任何代码之前，对需求进行结构化讨论。澄清目标、探索方案、输出设计文档。

**核心规则：** HARD GATE — 未经用户批准设计，不允许写任何代码。

**执行流程：**
1. 探索项目上下文（文件、文档、最近提交）
2. 逐个提问（一次一个问题），澄清需求
3. 提供 2-3 种方案及推荐
4. 分部分展示设计，逐部分获得批准
5. 将设计保存到 `docs/superpowers/specs/`
6. 自我审查（占位符、矛盾、歧义）
7. 请用户审查设计文档
8. 转交给 writing-plans skill

**示例：**
```
run_skill("brainstorming", "We need to add a file upload feature to the API")
```

---

### 2. writing-plans — 编写计划

**用途：** 根据设计文档生成详细的、分步的、可执行的实现计划。

**核心原则：**
- 每个步骤 2-5 分钟（单一操作）
- 精确文件路径、完整代码、验证命令
- 没有占位符（TBD、TODO、稍后实现）
- DRY、YAGNI、TDD、频繁提交

**计划格式：**
- 保存到 `docs/superpowers/plans/YYYY-MM-DD-<feature>.md`
- 每个任务包含：文件、步骤（带复选框）、代码、命令

**执行交接：** 完成后提供两个选项：
- A) Subagent-driven — 每个任务派生子智能体 + 审查
- B) Inline execution — 在当前会话中逐步执行

---

### 3. test-driven-development — 测试驱动开发

**用途：** RED-GREEN-REFACTOR 完整循环。

**铁律：** 没有先写失败的测试，就不能写生产代码。

**流程：**
1. **RED** — 写一个失败的测试（验证它确实失败）
2. **GREEN** — 写最简代码使其通过（验证它确实通过）
3. **REFACTOR** — 清理代码，保持测试通过

**关键原则：**
- 先写测试再写代码。写了代码再补测试的，删掉重来
- 没看到测试失败 = 不知道测试是否有效
- 一次一个行为，用 `run_command` 执行测试

---

### 4. subagent-driven-development — 子智能体驱动开发

**用途：** 将实现计划逐任务执行，每个任务派发独立上下文，附带两阶段审查。

**流程：**
1. 读取计划文件，提取所有任务
2. 用 `todo_write` 创建任务追踪
3. 对每个任务：
   a. 读取相关上下文 → b. 实现（TDD）→ c. 对照规格自审 → d. 用 `review` subagent 审查 → e. 修复问题 → f. 标记完成
4. 全部完成后调用 `finishing-a-development-branch`

**注意：** 不要在任务之间停顿询问用户是否继续——除非阻塞、任务完成、或计划本身有问题。

---

### 5. executing-plans — 计划执行（人工检查点）

**用途：** 作为 subagent-driven-development 的替代方案。在同一个会话中逐步执行计划，每个任务完成后有人工检查点。

**流程：**
1. 加载并审查计划
2. 逐个执行任务
3. 任务完成后调用 `finishing-a-development-branch`

**阻塞时：** STOP 并寻求帮助——不要猜测。

---

### 6. requesting-code-review — 请求代码审查

**用途：** 在提交代码审查前，运行自查清单。

**自查清单：**
- [ ] 测试全部通过
- [ ] 实现与规格一致
- [ ] 没有遗留的调试代码（console.log, debugger, TODO）
- [ ] 没有 YAGNI 违规（多余功能）

**调用方式：** 自查后调用内置的 `review` subagent：
```
run_skill("review", "Review changes for task X: <summary>")
```

---

### 7. receiving-code-review — 响应审查反馈

**用途：** 怎样以技术严谨的态度而非虚情假意地响应反馈。

**核心规则：**
- 先验证再实施，先提问再假设
- 不要"Absolutely right"或"Great point"——直接行动
- 意见不合适就理性反驳
- 逐个修复，逐个测试

---

### 8. systematic-debugging — 系统化调试

**用途：** 遇到任何 bug、测试失败、意外行为时——先找根因再修复。

**4 个阶段（必须依次完成）：**

| 阶段 | 关键活动 | 成功标准 |
|------|----------|---------|
| 1. 根因 | 读错误、复现、查变更、收集证据 | 理解 WHAT 和 WHY |
| 2. 模式 | 找工作示例、对比参考实现 | 识别差异 |
| 3. 假设 | 形成理论、最小测试 | 确认或新假设 |
| 4. 修复 | 创建失败测试、修复、验证 | bug 解决、测试通过 |

**3 次修复失败后：** 停止，质疑架构设计，与用户讨论。

---

### 9. verification-before-completion — 完成前验证

**用途：** 没有新的验证结果，就不能宣称任何工作已完成。

**门控函数：**
1. 识别：什么命令能证明这个声明？
2. 运行：执行完整的命令
3. 读取：全部输出、检查退出码
4. 验证：输出是否确认声明？
5. 然后：做出声明

**反模式：** "Should pass now"、"Looks correct"、"It works"（没有验证输出）

---

### 10. using-git-worktrees — 使用 Git Worktrees

**用途：** 在隔离的 git worktree 中工作，保护当前分支。

**流程：**
1. 检测是否已在隔离工作区
2. 询问用户是否需要创建 worktree
3. 创建：`git worktree add .worktrees/<branch> -b <branch>`
4. 运行项目初始化（npm install / cargo build 等）
5. 验证基础测试通过

---

### 11. finishing-a-development-branch — 完成开发分支

**用途：** 验证测试通过 → 呈现选项 → 执行选择 → 清理。

**4 个选项（用 `ask_choice` 呈现）：**
- **A) 合并到 main** — checkout main → merge → 验证测试 → 删除分支
- **B) 创建 PR** — push → gh pr create（保留 worktree）
- **C) 保持原样** — 什么也不做
- **D) 丢弃** — 需要用户输入 "discard" 确认

---

### 12. dispatching-parallel-agents — 并发调度子智能体

**用途：** 当有多个独立的问题（不同子系统的故障、不同测试文件的失败）时，并发分派子智能体分别调查。

**使用条件：**
- 3+ 个独立问题，不同根因
- 彼此不需要上下文
- 无共享状态

**不适合：**
- 问题相关（修复一个可能解决其他）
- 需要理解全系统状态
- 智能体会互相干扰（编辑相同文件）

---

## 建议的工作流

### 从零开始的新功能

```
run_skill("brainstorming", "Add feature X")
    → run_skill("writing-plans", "From the design doc")
        → run_skill("subagent-driven-development", "Execute the plan")
            → run_skill("finishing-a-development-branch")
```

### 修复 Bug

```
run_skill("systematic-debugging", "Bug: ...")
    → run_skill("test-driven-development", "Implement fix with TDD")
        → run_skill("verification-before-completion")
```

### 代码审查

```
run_skill("requesting-code-review")
    → run_skill("review", "Review changes")
        → run_skill("receiving-code-review")  # 处理反馈
```

---

## 与原生 Reasonix Code 工具的关系

| Reasonix Code 原生能力 | 对应的 Superpowers skill |
|------------------------|--------------------------|
| `submit_plan` 工具 | `writing-plans` (更详细的版本) |
| `ask_choice` 工具 | 用于 brainstorming 和 finishing-a-development-branch |
| `todo_write` 工具 | 用于 subagent-driven-development 的任务追踪 |
| `review` [🧬 subagent] | 用于 requesting-code-review 的审查步骤 |
| `test` skill | `test-driven-development` (更完整的 TDD 哲学) |
| 系统提示中的探索约束 | `brainstorming` (更结构化的版本) |

---

## 安装位置

所有技能安装在 `.reasonix/skills/` 目录下。可通过 `run_skill({ name: "<skill-name>", arguments: "<task>" })` 调用。

下次 `/new` 或重启后，它们会出现在 pinned Skills index 中。
