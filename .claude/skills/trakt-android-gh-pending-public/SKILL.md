---
name: trakt-android-gh-pending-public
description: Keep the `pending-public` label on open trakt-android web-parity issues in sync with trakt-web feature flags. Adds the label when the matching web feature is behind a flag in `FeatureFlag.ts`, removes it once the flag is gone (feature public on web). Runs unattended as a daily scheduled task. Trigger: /trakt-android-gh-pending-public, "sync pending-public", "check web feature flags", "which parity issues are flagged on web".
---

# Pending-public label sync

Applies label changes without asking: labels are cheap to revert and the run is unattended. Anything uncertain is reported, never applied.

## Constants

| What | Value |
| --- | --- |
| Repo | `trakt/trakt-android` |
| Label | `pending-public` |
| Scope | open issues with label `web-parity`, excluding the `parent` tracker |
| Web code | `/Users/michal/Documents/Android/trakt/trakt-web` (read only, never modify, never checkout) |
| Flag enum | `projects/client/src/lib/features/feature-flag/models/FeatureFlag.ts` |
| Flag metadata | `projects/client/src/lib/features/feature-flag/models/featureFlagDefinitions.ts` (`addedAt`, `audience`) |

## Steps

1. **Refresh web without touching the working tree.** `git -C <web> fetch -q origin main`. Read everything from `origin/main` (`git -C <web> show origin/main:<path>`, `git -C <web> grep <pattern> origin/main -- projects/client/src`). Record the short SHA of `origin/main`. If the fetch fails, stop and report it.

2. **List flags.** Parse the `FeatureFlag` enum. For each flag, find where it gates UI: `git grep "FeatureFlag.<Name>\b" origin/main -- projects/client/src`, skipping `*.spec.ts` and the definitions file. Note `audience` from the definitions file.

3. **List issues.** `gh issue list -R trakt/trakt-android --state open --label web-parity --limit 200 --json number,title,labels,body`. Drop issues labelled `parent`.

4. **Match flags to issues.** An issue is flagged when the web feature it asks for renders only behind a flag. Use the issue title, the trakt-web PRs it links, and the gated files from step 2. Rules:
   - Check what the flag actually hides. If the same behaviour also ships through an unflagged path, the issue is not flagged (example: Reality Check renders in both the flagged `InsightsCard` and the public `SentimentCard`).
   - When unsure, open the linked trakt-web PR (`gh pr view <n> -R trakt/trakt-web --json title,files`) to see which components it touched.
   - If a match is still unclear, do not change the label. List it under "Needs review".

5. **Diff against current labels.**
   - Flagged, no label: add (`gh issue edit <n> -R trakt/trakt-android --add-label pending-public`).
   - Labelled, its flag no longer in the enum and the feature renders unconditionally on `origin/main`: remove (`--remove-label pending-public`).
   - Labelled, but no flag ever matched it (for example labelled by hand for a backend rollout): leave it, list it under "Needs review".

6. **Report.** Short, scannable:
   - Web SHA checked.
   - Added / removed labels (issue number, title, flag).
   - Needs review (with the reason).
   - Flags with no Android issue yet (flag name, `audience`, `addedAt`).
   - If nothing changed, say so in one line.

Do not create issues, edit issue bodies, or move board items.
