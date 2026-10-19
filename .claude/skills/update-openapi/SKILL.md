---
name: update-openapi
description: Refresh the Android OpenAPI spec from the trakt-api repo - fast-forward trakt-api to latest master, run the Bun `generate` script, copy the resulting openapi.json into this repo's `openapi/` folder. Trigger: "update openapi", "refresh openapi spec", "pull latest api spec", "sync openapi.json".
---

# Update OpenAPI spec

Pulls the latest API contract from the `trakt-api` repo and drops it into `openapi/openapi.json`.

- API repo: `/Users/michal/Documents/Android/trakt/trakt-api` (default branch is `master`, not `main`)
- Generator: `projects/openapi/generate.ts`, run via `bun run generate` in `projects/openapi` (repo uses Bun, install with `brew install oven-sh/bun/bun`)
- Output: `projects/openapi/openapi.json`
- Destination: `/Users/michal/Documents/Android/trakt/trakt-android-2025/openapi/openapi.json`

## Steps

Set `API=/Users/michal/Documents/Android/trakt/trakt-api` and run every git command with `git -C "$API"`.

1. **Check state.**
   ```bash
   git -C "$API" status -sb
   git -C "$API" branch --show-current
   ```
   - Branch is not `master`: stop and ask the user before switching. They may have work in progress.
   - Working tree dirty: stash it so generation runs on clean `master`.
     ```bash
     git -C "$API" stash push -u -m "update-openapi skill"
     ```
     Remember that a stash was created. Restore it in step 5.

2. **Fast-forward to latest master.** Authenticate through `gh` over HTTPS, not SSH.
   ```bash
   git -C "$API" -c credential.helper='!gh auth git-credential' fetch https://github.com/trakt/trakt-api.git +refs/heads/master:refs/remotes/origin/master
   git -C "$API" merge --ff-only origin/master
   git -C "$API" log -1 --format='%h %s (%cr)'
   ```
   The explicit refspec keeps `origin/master` in sync. A bare URL fetch only sets `FETCH_HEAD`, and `git status` then reports `master` as falsely ahead.
   `--ff-only` fails when local `master` has diverged. Stop and report. Never reset or force.

3. **Generate.**
   ```bash
   cd "$API" && bun install && cd projects/openapi && bun run generate
   ```
   On failure: quote the shortest decisive error line, restore the stash (step 5), stop.

4. **Copy into Android repo.**
   ```bash
   cp "$API/projects/openapi/openapi.json" /Users/michal/Documents/Android/trakt/trakt-android-2025/openapi/openapi.json
   git -C /Users/michal/Documents/Android/trakt/trakt-android-2025 diff --stat -- openapi/openapi.json
   ```

5. **Restore stash** (only if step 1 created one).
   ```bash
   git -C "$API" stash pop
   ```
   On conflict: leave the stash in place and tell the user.

6. **Regenerate Kotlin client** (skip when step 4 shows no spec changes).
   ```bash
   cd /Users/michal/Documents/Android/trakt/trakt-android-2025 && ./gradlew openApiGenerate
   ```
   Output lands in `build/generate-resources/`, which is gitignored. Only `openapi/openapi.json` shows up in `git status`.
   On failure: quote the shortest decisive error line and stop.

## Report

- trakt-api commit used (hash + subject).
- `openapi.json` diff stat, or "no changes".
- Whether a stash was created and restored.
- `openApiGenerate` result (success or failure).

## Notes

- Do not commit anything in either repo unless the user asks.
