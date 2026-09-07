# FPL Ultimate API

The backend for FPL Ultimate — a fully standalone Fantasy Premier League platform with custom scoring rules and team management, built as an alternative to the official (and deeply frustrating) `draft.premierleague.com`. Currently operates as a read-only proxy of the official FPL API while the standalone platform is in development.

---

### Claude Code Setup

This repo uses a `CLAUDE.md` file to provide AI context when working with Claude Code. The parent directory (`fpl-ultimate/`) contains a top-level `CLAUDE.md` that imports both this file and the frontend's `CLAUDE.md`, so Claude has full project context when activated from the parent.

To replicate this for a new developer:

1. Clone both repos (`fpl-ultimate-api` and `fpl-ultimate-frontend`) into a shared parent directory
2. Create a `CLAUDE.md` in the parent directory with the following content:

```markdown
@import fpl-ultimate-api/CLAUDE.md
@import fpl-ultimate-frontend/CLAUDE.md
```

The sub-repo `CLAUDE.md` files are tracked in version control. The parent-level file is local only.
