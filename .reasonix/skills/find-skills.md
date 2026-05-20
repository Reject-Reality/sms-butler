---
name: find-skills
description: Discover skills from the open agent skills ecosystem (skills.sh), fetch their source, and install them as Reasonix skills
---

# Find Skills

Discover skills from the **open agent skills ecosystem** at [skills.sh](https://skills.sh/), fetch their open source content, and install them **into Reasonix** (`.reasonix/skills/`).

**Source:** Adapted from [vercel-labs/skills](https://github.com/vercel-labs/skills)

---

## When to Use This Skill

Use this skill when the user:

- Asks "how do I do X" where X might be a common task with an existing skill
- Says "find a skill for X", "is there a skill for X", "帮我找一个能……的 skill"
- Wants to search for tools, templates, or workflows from the broader agent ecosystem
- Mentions a specific domain (design, testing, deployment, React, Android, etc.)
- Wants to install an external skill into Reasonix

---

## How to Find and Install Skills

### Step 1: Understand Their Need

Identify:
1. The **domain** (Android, React, testing, design, marketing, etc.)
2. The **specific task** (mobile app design, writing tests, SEO audit, etc.)

### Step 2: Fetch the Leaderboard from skills.sh

Use `web_fetch("https://skills.sh/")` to get the current leaderboard. Key data per skill:
- **Name** and **source** (`owner/repo`)
- **Install count** (popularity signal)
- **Category** inferred from name/description

### Step 3: Check the Leaderboard First

The leaderboard ranks skills by total installs. Common top sources:

| Source | Focus Area | Examples |
|--------|-----------|---------|
| `vercel-labs/skills` | Meta: skill discovery | find-skills (1.6M) |
| `anthropics/skills` | Design, content, agents | frontend-design (433K), skill-creator (219K), pptx, pdf |
| `vercel-labs/agent-skills` | Web dev, React, Next.js, design | react-best-practices (412K), web-design-guidelines (331K) |
| `microsoft/azure-skills` | Azure cloud, infrastructure | microsoft-foundry (331K), 20+ Azure skills |
| `mattpocock/skills` | Dev workflows, code quality | improve-codebase-architecture (135K), tdd (130K), diagnose |
| `obra/superpowers` | Planning, debugging, review | brainstorming (168K), systematic-debugging (103K) |
| `coreyhaines31/marketingskills` | Marketing, SEO, content | seo-audit (114K), copywriting (103K), 30+ skills |
| `supabase/agent-skills` | Backend, database | supabase-postgres-best-practices (177K) |
| `firebase/agent-skills` | Firebase/GCP | firebase-basics (58K), auth, hosting |
| `pbakaus/impeccable` | Code/design critique & polish | impeccable (106K), critique, polish, audit |
| `leonxlnx/taste-skill` | Frontend visual design | design-taste-frontend (64K), high-end-visual-design (56K) |
| `agentspace-so/runcomfy-agent-skills` | AI media generation | flux-kontext (107K), ai-video-generation, ai-image-generation |
| `sleekdotdesign/agent-skills` | Mobile app design | sleek-design-mobile-apps (146K) |
| `expo/skills` | React Native / Expo | building-native-ui, native-data-fetching, upgrading-expo |

### Step 4: Search Specific Terms

If the leaderboard doesn't cover the user's need, use `web_search` to find specific skills.

### Step 5: Verify Quality Before Recommending

Check:
1. **Install count** — Prefer 50K+ installs. Be cautious under 10K.
2. **Source reputation** — Known orgs are more trustworthy.
3. **Match relevance** — Does the skill clearly match the user's task?

### Step 6: Present Options to the User

Format:

```
**Top matches for:** <task>

1. **<skill-name>** — <source> [<installs> installs]
   - <what it does>
   - GitHub: <repo-url>

2. **<skill-name>** — ...
```

### Step 7: Install into Reasonix

Once the user confirms which skill to install, the installation process is:

**A) Find the source repo** — identify the GitHub repository (usually `github.com/<source>`)

**B) Fetch the SKILL.md** — the skills.sh ecosystem stores each skill's content in a `SKILL.md` file. Fetch it from the raw GitHub URL:

```
https://raw.githubusercontent.com/<owner>/<repo>/main/skills/<skill-name>/SKILL.md
```

Or via the GitHub API:
```
https://api.github.com/repos/<owner>/<repo>/contents/skills/<skill-name>
```

**C) Convert to Reasonix format** — the original content is written for generic AI agents. Adapt it:
- Keep the core workflow and logic intact
- Add Reasonix YAML frontmatter (`name`, `description`, `runAs`, etc.)
- Add a `**Source:**` attribution line crediting the original
- Save to `.reasonix/skills/<skill-name>.md`

Example conversion:

```markdown
---
name: <skill-name>
description: <one-line summary of what it does>
---
# <Skill Name>

**Source:** Adapted from [<owner/repo>](https://github.com/<owner>/<repo>)

<original content adapted for Reasonix>
```

**D) Report result** — tell the user the skill is now available as a Reasonix skill, invocable via `run_skill({ name: "<skill-name>" })` or `/skill <skill-name>`.

If the source repo doesn't have a `SKILL.md` or the structure is different, fall back to reading the skill page on skills.sh for the summary and reconstructing a Reasonix version from that information.

### Step 8: When No Relevant Skills Found

1. Acknowledge no match was found
2. Offer to help directly with Reasonix tools
3. Suggest creating a custom Reasonix skill with `install_skill` if the task is reusable

---

## Quick-Reference: Top Skills by Category

### 🎨 Design & Frontend
| Skill | Source | Installs | GitHub |
|-------|--------|----------|--------|
| frontend-design | anthropics/skills | 433K | anthropics/skills |
| web-design-guidelines | vercel-labs/agent-skills | 331K | vercel-labs/agent-skills |
| ui-ux-pro-max | nextlevelbuilder/ui-ux-pro-max-skill | 173K | nextlevelbuilder/ui-ux-pro-max-skill |
| sleek-design-mobile-apps | sleekdotdesign/agent-skills | 146K | sleekdotdesign/agent-skills |
| design-taste-frontend | leonxlnx/taste-skill | 64K | leonxlnx/taste-skill |
| high-end-visual-design | leonxlnx/taste-skill | 56K | leonxlnx/taste-skill |
| minimalist-ui | leonxlnx/taste-skill | 52K | leonxlnx/taste-skill |
| tailwind-design-system | wshobson/agents | 43K | wshobson/agents |

### ⚛️ Web Development (React / Next.js)
| Skill | Source | Installs | GitHub |
|-------|--------|----------|--------|
| vercel-react-best-practices | vercel-labs/agent-skills | 412K | vercel-labs/agent-skills |
| remotion-best-practices | remotion-dev/skills | 319K | remotion-dev/skills |
| vercel-composition-patterns | vercel-labs/agent-skills | 180K | vercel-labs/agent-skills |
| next-best-practices | vercel-labs/next-skills | 89K | vercel-labs/next-skills |
| shadcn | shadcn/ui | 149K | shadcn/ui |

### 📱 Mobile
| Skill | Source | Installs | GitHub |
|-------|--------|----------|--------|
| sleek-design-mobile-apps | sleekdotdesign/agent-skills | 146K | sleekdotdesign/agent-skills |
| vercel-react-native-skills | vercel-labs/agent-skills | 121K | vercel-labs/agent-skills |
| building-native-ui | expo/skills | — | expo/skills |
| native-data-fetching | expo/skills | — | expo/skills |
| upgrading-expo | expo/skills | — | expo/skills |

### 🧪 Testing & Code Quality
| Skill | Source | Installs | GitHub |
|-------|--------|----------|--------|
| tdd | mattpocock/skills | 130K | mattpocock/skills |
| diagnose | mattpocock/skills | 106K | mattpocock/skills |
| improve-codebase-architecture | mattpocock/skills | 135K | mattpocock/skills |
| webapp-testing | anthropics/skills | 73K | anthropics/skills |

### ☁️ Cloud & Backend
| Skill | Source | Installs | GitHub |
|-------|--------|----------|--------|
| microsoft-foundry | microsoft/azure-skills | 331K | microsoft/azure-skills |
| supabase-postgres-best-practices | supabase/agent-skills | 177K | supabase/agent-skills |
| firebase-basics | firebase/agent-skills | 58K | firebase/agent-skills |
| convex-quickstart | get-convex/agent-skills | 50K | get-convex/agent-skills |

### 📈 Marketing & SEO
| Skill | Source | Installs | GitHub |
|-------|--------|----------|--------|
| seo-audit | coreyhaines31/marketingskills | 114K | coreyhaines31/marketingskills |
| copywriting | coreyhaines31/marketingskills | 103K | coreyhaines31/marketingskills |
| content-strategy | coreyhaines31/marketingskills | 71K | coreyhaines31/marketingskills |
| marketing-psychology | coreyhaines31/marketingskills | 76K | coreyhaines31/marketingskills |

### 🤖 AI & Media Generation
| Skill | Source | Installs | GitHub |
|-------|--------|----------|--------|
| flux-kontext | agentspace-so/runcomfy-agent-skills | 107K | agentspace-so/runcomfy-agent-skills |
| browser-use | browser-use/browser-use | 75K | browser-use/browser-use |
| ai-video-generation | agentspace-so/runcomfy-agent-skills | 51K | agentspace-so/runcomfy-agent-skills |
| ai-image-generation | agentspace-so/runcomfy-agent-skills | 50K | agentspace-so/runcomfy-agent-skills |

---

## Tips for Effective Matching

1. **Use the leaderboard first** — faster, shows most battle-tested skills
2. **Check install counts** — 100K+ is highly battle-tested; 10K-100K is established
3. **Recommend at most 3 options** — too many choices overwhelms
4. **Always include the GitHub source** — needed for fetching and installing
