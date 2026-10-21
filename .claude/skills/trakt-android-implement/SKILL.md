---
name: trakt-android-implement
description: Implement trakt-android GitHub issues one by one - ask which issues and the end state (local branches, draft PRs or ready-for-review PRs), move them to In Progress on the trakt Product board, assess each issue against trakt-web and the Android code, implement, verify, commit and optionally open PRs that never auto-close issues. Trigger: /trakt-android-implement, "implement issues", "work on #499 #484", "take care of these issues".
---

# Implement issues

Takes a set of trakt-android issues from the board to branches or PRs, one issue at a time.

## Constants

| What | Value |
| --- | --- |
| Repo | `trakt/trakt-android` |
| Board | `gh project` owner `trakt`, number `11` ("Product") |
| Status field ID | `PVTSSF_lADOAE01zc4BTaq0zhAqgtk` |
| Status option IDs | Backlog `f75ad846`, Up Next `607d3ebd`, In Progress `47fc9ee4`, Under Review `df73e18b`, Done `98236657` |
| Android code | `/Users/michal/Documents/Android/trakt/trakt-android-2025` |
| Web code | `/Users/michal/Documents/Android/trakt/trakt-web` (read only, `git pull --ff-only` is fine) |

If a field or option ID stops working, refresh it with `gh project field-list 11 --owner trakt --format json` and update this table.

## Hard rule: never close issues

**Done on the Android board means released to the public.** A merged PR or commit that closes an issue moves it to Done too early.

- Never write a closing keyword followed by an issue reference in a PR title, PR body or commit message. Closing keywords: `close`, `closes`, `closed`, `fix`, `fixes`, `fixed`, `resolve`, `resolves`, `resolved` (any case).
- Reference issues with `Refs #N` in PR bodies and a `Refs: #N` footer in commits.
- Do not link the issue through the PR "Development" sidebar either.
- Before every push and PR create/edit, grep the text: `grep -inE '\b(close[sd]?|fix(e[sd])?|resolve[sd]?)\b:? *(trakt/trakt-android)?#[0-9]+'`. Any hit blocks the step.

## Board status rule

An issue moves to Under Review (`df73e18b`) only after its PR is merged. Until then it stays In Progress (`47fc9ee4`).

- This skill never merges PRs, so it always leaves its issues In Progress. Local branches, draft PRs and ready-for-review PRs all stay In Progress.
- After every PR create, push or ready-flip, check the board item. If automation moved the issue to Under Review before the merge, move it back to In Progress.

## Step 1 - Ask

Use one `AskUserQuestion` call:

1. **Which issues?** Skip this question when the user already named issue numbers in the arguments.
   Otherwise offer the Backlog / Up Next trakt-android items from the board as options (fetch them
   first, see step 2), with "Other" for free input.
2. **End state?**
   - Local branches only - commit on a branch per issue, no push.
   - Draft PRs - push and open draft PRs.
   - Ready-for-review PRs - push and open PRs marked ready.

## Step 2 - Prepare

Keep raw dumps in the session scratchpad directory, referred to below as `$S`.

1. **Auth.** `gh auth status 2>&1 | grep -i scopes` must list `project`. If not, ask the user to run `! gh auth refresh -h github.com -s project` and wait.
2. **Clean, latest `main`.** `git status --short` must be empty. If the tree is dirty, stop and ask. Then switch to `main` and bring it up to date:
   ```bash
   git checkout main
   git -c credential.helper='!gh auth git-credential' pull --ff-only https://github.com/trakt/trakt-android.git main
   git -c credential.helper='!gh auth git-credential' fetch -q https://github.com/trakt/trakt-android.git main
   [ "$(git rev-parse HEAD)" = "$(git rev-parse FETCH_HEAD)" ] || echo "main not at origin"
   ```
   If the pull is not a fast-forward (local `main` has its own commits), stop and ask. Never reset or rebase `main` without approval.
3. **Board items.**
   ```bash
   gh project item-list 11 --owner trakt --limit 3000 --format json > "$S/items.json"
   ```
   Find each target issue's item `id` (`content.number` plus repository `trakt/trakt-android`). If an issue is not on the board, tell the user and continue without the board step for it.
4. **Move all target issues to In Progress before any code work.**
   ```bash
   gh project item-edit --project-id PVT_kwDOAE01zc4BTaq0 --id <item-id> \
     --field-id PVTSSF_lADOAE01zc4BTaq0zhAqgtk --single-select-option-id 47fc9ee4
   ```
5. **Update trakt-web.** `git -C <web> pull --ff-only -q`.

## Step 3 - Per issue, in the order given

Finish one issue completely before starting the next. Before each new branch, repeat the pull and the `HEAD` = `FETCH_HEAD` check from step 2, so every branch starts from the latest `main`.

0. **Start the effort clock.** `date -u +%Y-%m-%dT%H:%M:%SZ > "$S/startN.txt"`. Each issue gets its own start time, so the effort report in step 10 counts only that issue.
1. **Read the issue.** `gh issue view N -R trakt/trakt-android --json title,body,labels,comments > "$S/iN.json"`. Read comments too: maintainers often extend the scope there (for example a follow-up web PR).
2. **Read the web source.** For each `trakt-web#N` or `trakt-web/pull/N` link, read `gh pr view N -R trakt/trakt-web --json title,body,files` and the relevant part of `gh pr diff`. Then read the current web files, not only the diff, because later PRs may have changed them. Match web logic, ordering and edge cases.
3. **Assess against Android.** Check every claim in the issue against current Android code. Issues often list items that already exist. Note those for the PR body instead of re-implementing them.
4. **Load rules.** Read the `.agents/rules/` files that match the touched area, as `CLAUDE.md` lists.
5. **Branch.** `git checkout -b <type>/<short-slug>` (for example `feat/season-rail-highlight`, `fix/season-progress-aired`).
6. **Implement.** Reuse existing helpers and tokens. Keep the diff small.
7. **Sync tracking with web.** Read `analytics.md` first. Check whether the web code for this feature tracks anything:
   ```bash
   W=<web>/projects/client/src/lib
   grep -rnE "useTrack|AnalyticsEvent\." <web files touched by the linked PRs and their callers>
   grep -n "<event>" $W/features/analytics/events/AnalyticsEvent.ts $W/features/analytics/events/AnalyticsEventDataMap.ts
   ```
   - Web tracks it: add the same event on Android. Same event name, dims, metrics and value strings, fired at the same moment as web. Check whether Android already has the event before adding it.
   - Web does not track it, and the change is a new user-facing feature: add HAL tracking per `analytics.md` and `implementation.md`.
   - Web does not track it, and the change tweaks an existing screen: no tracking.
   - State the decision and the event names in the PR body and the report.
8. **Verify.**
   - Compile: `./gradlew -q :app:compilePlaystoreDebugKotlin` (plain `compileDebugKotlin` is ambiguous because of flavors). Add `:tv:compileDebugKotlin` when `tv/` or shared code changed.
   - Lint: the Gradle `ktlintCheck` task is not wired. Run the CLI on changed files: `ktlint -F $(git diff --name-only main; git ls-files -o --exclude-standard)`, then `ktlint` again to confirm it is clean.
   - Fix all errors before committing. Do not claim device testing that did not happen.
9. **Commit.** Follow `commits.md`: Conventional Commits, body explains why, `Refs: #N` footer, no AI attribution, no em-dashes. Run the closing-keyword grep on the message.
10. **End state.**
   - Local branches only: stop here, `git checkout main`.
   - PRs: push with `git -c credential.helper='!gh auth git-credential' push -q https://github.com/trakt/trakt-android.git <branch>`, then `gh pr create -R trakt/trakt-android --base main --head <branch> --title "<commit subject>" --body-file "$S/prN.md"`, adding `--draft` for draft PRs. Run the closing-keyword grep on the body file first.
   - PR body follows `commits.md`: one short paragraph on what changed for the user and why, then up to ~3 bullets for edge cases, deviations from web, or parts of the issue that were already in place. Do not write "ported from" in the prose. Then a line listing the issue and every web PR the change mirrors: `Refs #N, trakt/trakt-web#A, trakt/trakt-web#B`. When web shipped a plain commit, find its PR with `gh api repos/trakt/trakt-web/commits/<sha>/pulls`. No AI attribution, no `Generated with` footer.
   - **Effort report.** Right before `gh pr create`, count this issue's tokens. The session ID is the name of the scratchpad's parent folder, so pass the transcript explicitly instead of letting the script guess the newest session:
     ```bash
     SID=$(basename "$(dirname "$S")")
     T=$(ls ~/.claude/projects/*/"$SID".jsonl | head -1)
     python3 .claude/skills/trakt-android-implement/scripts/session_usage.py --since "$(cat "$S/startN.txt")" --session "$T"
     ```
     It prints one JSON line with tokens per model, subagents included. Append one line at the very end of the PR body, after the `Refs` line:
     `<!-- convergence-run {"web_pr": <first linked trakt-web PR number or null>, "issue": "trakt-android#N", "client": "android", "lead_model": "<model id>", "subagents": <its "subagents">, "started": "<start time>", "pr_opened": "<now, UTC>", "tokens": <its "tokens">, "tokens_by_model": <its "models">} -->`
     A daily job reads this block to measure how long ports take. When the script prints `"tokens": null`, write `null`. Never estimate.
11. **Board after PR.** Follow the board status rule below.
12. `git checkout main` and continue with the next issue.

## Step 4 - Report

Lead with the result. Per issue: link (PR or branch name), what changed in plain words, what was already in place, any deviation from web. Then:

- Tracking: events added and whether they mirror web, or why none.
- Effort: tokens per issue from the effort report. For local-branch runs, run the script anyway and report the totals here, since there is no PR body to hold them.
- Verified: compile and ktlint per branch.
- Not verified: device or emulator runs, unless done.
- Blockers: missing scopes, issues not on the board, anything skipped.
- Board status of each issue.

## Gotchas

- `gh` without `project` scope fails on any board query. Ask early, in step 2.
- In zsh, quote globs such as `--include='*.kt'`, or the command fails with `no matches found`.
- Large issue bodies can get truncated in tool output. Dump to `$S` and print with `python3 -c` or `jq -r` when a read looks cut off.
- To change a pushed commit message, amend and push with `-f` only on the feature branch, never on `main`.
