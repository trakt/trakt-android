---
trigger: glob
globs: '**'
description: 'Conventional Commits with android scope enum, PR title rules, no AI-generated trailers, no em-dashes.'
applyTo: '**'
---

# Commit Standards

The codebase already follows Conventional Commits — recent history
shows `feat:`, `fix:`, `refactor:`, `chore:`, `feat(i18n):`. These rules
codify the convention and define the allowed scope enum.

## Format

```
<type>(<scope>): <subject>

[optional body]
[optional footer(s)]
```

- Subject: lowercase, imperative, no trailing period, ≤ 72 chars.
- Body: optional, wrap at 100 chars, explain **why** not **what**.
- Footers: `Refs: #123`, `Co-Authored-By: …` (only when human asks).

## Allowed types

| Type       | Use for                                                    |
| ---------- | ---------------------------------------------------------- |
| `feat`     | A user-visible feature.                                    |
| `fix`      | A bug fix.                                                 |
| `perf`     | Performance improvement without behaviour change.          |
| `refactor` | Code change that neither adds a feature nor fixes a bug.   |
| `chore`    | Tooling, dependency bumps, generated files (e.g. OpenAPI). |
| `test`     | Tests only.                                                |
| `docs`     | Documentation only.                                        |
| `style`    | Whitespace / formatting only, no semantic change.          |
| `build`    | Build system or external dependency changes.               |
| `ci`       | CI configuration only.                                     |

## Allowed scopes

Drawn from the actual module list + recurring concerns:

| Scope        | Path coverage                                              |
| ------------ | ---------------------------------------------------------- |
| `app`        | `app/`                                                     |
| `tv`         | `tv/`                                                      |
| `common`     | `common/`                                                  |
| `resources`  | `resources/`                                               |
| `openapi`    | `openapi/`, generated client                               |
| `widget`     | Android home-screen widget                                 |
| `i18n`       | Crowdin syncs, `values-*/strings.xml`                      |
| `ci`         | `.github/workflows/`, Fastlane                             |
| `deps`       | `gradle/libs.versions.toml`, dependency bumps              |
| `gradle`     | `build.gradle.kts`, `buildSrc/`, Gradle plugins            |
| `agents`     | `.agents/`, `AGENTS.md`, `CLAUDE.md`                       |
| `gemini`     | `.gemini/`                                                 |

Multi-scope changes pick the most-affected scope or use the broader
one:

```
feat(app): add notes drawer
fix(tv): TV get-upcoming case fix
refactor(common): derive isLatestAired from remaining episode count
chore(i18n): translations updates from CrowdIn
chore(deps): bump compose BOM to 2026.06.00
chore(openapi): regenerate from spec for new credits endpoint
ci: switch ktlint job to v1.7.1
```

## Examples drawn from current history

Good (already in use):

```
feat: surface specific episode type in details meta info
fix: hide stale mid-season episode tags on up-next cards
refactor: derive isLatestAired from remaining episode count
chore: update locales
feat(i18n): translations updates from CrowdIn (#149)
```

Better (add scope from the enum above for clarity):

```
feat(app): surface specific episode type in details meta info
fix(app): hide stale mid-season episode tags on up-next cards
refactor(common): derive isLatestAired from remaining episode count
chore(i18n): update locales
```

Bad:

```
fix tag staleness                            # missing type+scope, vague
update some things                           # missing everything
feat: notes                                  # missing scope and verb
```

## Body content

Use the body to record **why** the change is needed:

```
fix(common): handle 204 from credits endpoint

The credits endpoint returns 204 when a person has no recorded credits
for the queried role. The legacy mapping treated 204 as an error,
surfacing an empty screen with a stale error toast.
```

Avoid:

- Restating the subject in the body.
- Describing the diff line-by-line.
- AI-generated `Generated with Claude Code` footers.
- `Co-Authored-By: Claude <noreply@anthropic.com>` trailer — only add
  when explicitly requested by the human author.
- Em-dashes / en-dashes in commit messages, PR titles, or PR bodies.
  Use plain hyphens (`-`).

## PR titles

PR titles follow the same Conventional Commits format. They show up on
`main` once squash-merged, so they must be self-contained:

```
feat(app): add notes drawer (#234)
chore(deps): bump compose BOM to 2026.06.00 (#235)
```

## Pre-merge sanity

- One Conventional Commits scope per commit when possible. Squash-merge
  rolls multiple WIP commits into a clean message at merge time.
- No "fix typo", "address review", "wip" commits on main — squash or
  fixup before merging.
