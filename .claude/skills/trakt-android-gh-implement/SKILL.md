---
name: trakt-android-gh-implement
description: Implement trakt-android GitHub issues one by one - ask which issues and the end state (local branches, draft PRs or ready-for-review PRs), move them to In Progress on the trakt Product board, assess each issue against trakt-web and the Android code, implement each in a fresh subagent, verify, commit, optionally open PRs and handle up to 3 Codex review cycles on them. Trigger: /trakt-android-gh-implement, "implement issues", "work on #499 #484", "take care of these issues".
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
| Android code | Repo root (`git rev-parse --show-toplevel`). Run all commands from there. |
| Web code | `$WEB`: `$TRAKT_WEB` if set, else `../trakt-web` next to the repo root (read only, `git pull --ff-only` is fine) |

If a field or option ID stops working, refresh it with `gh project field-list 11 --owner trakt --format json` and update this table.

## Step 1 - Ask

Use one `AskUserQuestion` call:

1. **Which issues?** Skip this question when the user already named issue numbers in the arguments.
   Otherwise offer the Backlog / Up Next trakt-android items from the board as options (fetch them
   first, see step 2), with "Other" for free input.
2. **End state?**
   - Local branches only - commit on a branch per issue, no push.
   - Draft PRs - push and open draft PRs.
   - Ready-for-review PRs - push and open PRs marked ready.
3. **Monitor Codex reviews?** Only applies to ready-for-review PRs; ignore the answer otherwise.
   - Yes - after the PRs are open, watch for the automatic Codex review and handle its comments (step 4), up to 3 cycles per PR.
   - No - stop once the PRs are open.

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
5. **Locate and update trakt-web.**
   ```bash
   WEB="${TRAKT_WEB:-$(git rev-parse --show-toplevel)/../trakt-web}"
   git -C "$WEB" rev-parse --show-toplevel || echo "trakt-web not found"
   git -C "$WEB" pull --ff-only -q
   ```
   If it is not found, ask the user for the path. Shell variables do not persist between calls, so
   use the printed absolute path in later commands.

## Step 3 - Per issue, one fresh subagent each

Each issue runs in its own subagent, so every issue starts with an empty context and earlier
issues do not crowd it. Running this skill is the explicit request for subagents that `CLAUDE.md`
asks for. Rules:

- One subagent per issue, in the order given. Never run two at once.
- The subagent spawns no agents of its own.
- The main thread does not redo or re-check the subagent's work. It only keeps the returned summary.

Main thread, per issue:

1. **Start the effort clock.** `date -u +%Y-%m-%dT%H:%M:%SZ > "$S/startN.txt"` right before
   spawning. Each issue gets its own window, so the effort report counts only that issue. The
   subagent transcript is counted too: `session_usage.py` reads the session's `subagents/` folder.
2. **Spawn the subagent** with the Agent tool, `subagent_type: "general-purpose"`, and this prompt
   with the placeholders filled in:
   ```
   Implement trakt-android issue #N. Read .claude/skills/trakt-android-gh-implement/SKILL.md and
   follow its "Issue steps" section exactly. Read the baseline rules CLAUDE.md lists first.

   - End state: <local branch | draft PR | ready-for-review PR>
   - Scratchpad S: <absolute $S path>
   - Web repo WEB: <absolute trakt-web path>
   - Effort window start: <contents of $S/startN.txt>

   Do not spawn agents. Do not touch other issues or the board. Do not monitor Codex reviews.
   When done, reply with: branch, PR URL or "none", what changed in plain words, what was already
   in place, deviations from web, tracking decision and event names, compile and ktlint results,
   blockers, and the effort JSON line if one was produced.
   ```
3. **Wait for it.** Do not start the next issue until it returns.
4. **Stop the effort clock.** `date -u +%Y-%m-%dT%H:%M:%SZ > "$S/endN.txt"`. Save the returned
   summary to `$S/resultN.md` for step 5.
5. **Check the tree.** `git status --short` must be empty and the branch must be `main`. If not,
   stop and ask before the next issue. If the subagent reported a blocker, note it and continue.

### Issue steps

The subagent runs these for its one issue. `$S`, `$WEB`, the end state and the effort window start
come from the prompt.

1. **Fresh `main`.** Repeat the pull and the `HEAD` = `FETCH_HEAD` check from step 2, so the branch
   starts from the latest `main`. Stop and report if it fails.
2. **Read the issue.** `gh issue view N -R trakt/trakt-android --json title,body,labels,comments > "$S/iN.json"`. Read comments too: maintainers often extend the scope there (for example a follow-up web PR).
3. **Read the web source.** For each `trakt-web#N` or `trakt-web/pull/N` link, read `gh pr view N -R trakt/trakt-web --json title,body,files` and the relevant part of `gh pr diff`. Then read the current web files, not only the diff, because later PRs may have changed them. Match web logic, ordering and edge cases.
4. **Assess against Android.** Check every claim in the issue against current Android code. Issues often list items that already exist. Note those for the PR body instead of re-implementing them.
5. **Load rules.** Read the `.agents/rules/` files that match the touched area, as `CLAUDE.md` lists.
6. **Branch.** `git checkout -b <type>/<short-slug>` (for example `feat/season-rail-highlight`,
   `fix/season-progress-aired`).
7. **Implement.** Reuse existing helpers and tokens. Keep the diff small.
8. **Sync tracking with web.** Read `analytics.md` first. Check whether the web code for this feature tracks anything:
   ```bash
   W=$WEB/projects/client/src/lib
   grep -rnE "useTrack|AnalyticsEvent\." <$WEB files touched by the linked PRs and their callers>
   grep -n "<event>" $W/features/analytics/events/AnalyticsEvent.ts $W/features/analytics/events/AnalyticsEventDataMap.ts
   ```
   - Web tracks it: add the same event on Android. Same event name, dims, metrics and value strings, fired at the same moment as web. Check whether Android already has the event before adding it.
   - Web does not track it, and the change is a new user-facing feature: add HAL tracking per `analytics.md` and `implementation.md`.
   - Web does not track it, and the change tweaks an existing screen: no tracking.
   - State the decision and the event names in the PR body and the report.
9. **Verify.**
   - Compile: `./gradlew -q :app:compilePlaystoreDebugKotlin` (plain `compileDebugKotlin` is ambiguous because of flavors). Add `:tv:compileDebugKotlin` when `tv/` or shared code changed.
   - Lint: the Gradle `ktlintCheck` task is not wired. Run the CLI on changed files: `ktlint -F $(git diff --name-only main; git ls-files -o --exclude-standard)`, then `ktlint` again to confirm it is clean.
   - Fix all errors before committing. Do not claim device testing that did not happen.
10. **Commit.** Follow `commits.md`: Conventional Commits, body explains why, `Refs: #N` footer, no
   AI attribution, no em-dashes.
11. **End state.**
   - Local branches only: skip to step 12.
   - PRs: push with
     `git -c credential.helper='!gh auth git-credential' push -q https://github.com/trakt/trakt-android.git <branch>`,
     then
     `gh pr create -R trakt/trakt-android --base main --head <branch> --title "<commit subject>" --body-file "$S/prN.md"`,
     adding `--draft` for draft PRs.
   - PR body follows `commits.md`: one short paragraph on what changed for the user and why, then up
     to ~3 bullets for edge cases, deviations from web, or parts of the issue that were already in
     place. Do not write "ported from" in the prose. Then a line listing the issue and every web PR the
     change mirrors: `Refs #N, trakt/trakt-web#A, trakt/trakt-web#B`, followed by a `Closes #N` line so
     merging the PR closes the issue. When web shipped a plain commit, find its PR with
     `gh api repos/trakt/trakt-web/commits/<sha>/pulls`. No AI attribution, no `Generated with` footer.
   - **Effort report.** Right before `gh pr create`, count this issue's tokens. The session ID is the name of the scratchpad's parent folder, so pass the transcript explicitly instead of letting the script guess the newest session:
     ```bash
     SID=$(basename "$(dirname "$S")")
     T=$(ls ~/.claude/projects/*/"$SID".jsonl | head -1)
     python3 .claude/skills/trakt-android-gh-implement/scripts/session_usage.py --since "<effort window start>" --session "$T"
     ```
     It prints one JSON line with tokens per model, subagents included. Append one line at the very
     end of the PR body, after the `Closes` line:
     `<!-- convergence-run {"web_pr": <first linked trakt-web PR number or null>, "issue": "trakt-android#N", "client": "android", "lead_model": "<model id>", "subagents": <its "subagents">, "started": "<start time>", "pr_opened": "<now, UTC>", "tokens": <its "tokens">, "tokens_by_model": <its "models">} -->`
     A daily job reads this block to measure how long ports take. When the script prints `"tokens": null`, write `null`. Never estimate.
12. **Back to `main`.** `git checkout main`, then reply with the summary the prompt asks for.

## Step 4 - Monitor Codex reviews

Only when the user chose ready-for-review PRs and monitoring in step 1. Start after all PRs are
open. Opening a ready PR triggers the automatic Codex review. Codex posts as a bot whose login
contains `codex` (for example `chatgpt-codex-connector[bot]`): a PR review with inline comments when
it finds issues, or a PR comment or a 👍 reaction on the PR when it finds none.

Track per PR: cycle count (starts at 1) and `SINCE`, the UTC time the PR was opened or the last
`@codex review` comment was posted.

1. **Wait for the review.** Use the Monitor tool with an until-loop that polls every 60 s, never a
   foreground `sleep`. The review is in when any of these has a Codex author and a time after `SINCE`:
   ```bash
   R=repos/trakt/trakt-android
   gh api "$R/pulls/N/reviews" --jq '.[] | select(.user.login|test("codex";"i")) | .submitted_at'
   gh api "$R/issues/N/comments" --jq '.[] | select(.user.login|test("codex";"i")) | .created_at'
   gh api "$R/issues/N/reactions" --jq '.[] | select(.user.login|test("codex";"i")) | select(.content=="+1") | .created_at'
   ```
   Time out after 30 min. On timeout, stop monitoring that PR and note it in the report.
2. **Collect new comments.** Unresolved Codex threads, with IDs for replying and resolving:
   ```bash
   gh api graphql -f query='query { repository(owner:"trakt", name:"trakt-android") { pullRequest(number:N) {
     reviewThreads(first:100) { nodes { id isResolved path line
       comments(first:1) { nodes { databaseId author { login } body } } } } } } }' > "$S/threadsN.json"
   ```
   No unresolved Codex threads: the PR is clean. Stop monitoring it.
   Comments found: open the Codex effort window for this PR's issue before reading them:
   `date -u +%Y-%m-%dT%H:%M:%SZ > "$S/codexN-start.txt"`.
3. **Assess each comment critically.** Codex is a reviewer, not the spec. Treat each comment as a
   claim to verify, not an instruction:
   - Reproduce the failure path in the code. If it cannot happen (guarded elsewhere, framework
     already handles it, wrong assumption about the flow), decline.
   - Hold the implementation plan. The issue scope, the linked web behaviour and the deviations
     recorded in the PR body win over a reviewer's preference. Decline comments that ask to
     expand scope, change agreed behaviour, or undo a deliberate deviation, and cite which one.
   - Fix only real defects: bugs, crashes, broken edge cases, and violations of `.agents/rules/`.
     Keep each fix as small as the defect, no drive-by refactors.
   - Weigh severity labels (P1, P2) as hints only.

   Decide per comment: fix, or decline with a concrete reason (wrong, out of scope, intended
   behaviour, or conflicts with the plan).
4. **Fix.** On the PR branch: apply the fixes, verify as in "Issue steps" 9, commit per `commits.md`
   (`Refs: #N`), push with the same `push` command as "Issue steps" 11. Never force-push for review fixes.
5. **Reply and resolve every handled thread.** Reply with what changed and the commit SHA, or why it
   was declined, then resolve:
   ```bash
   gh api "$R/pulls/N/comments/<databaseId>/replies" -f body="<reply>"
   gh api graphql -f query='mutation { resolveReviewThread(input:{threadId:"<thread id>"}) { thread { isResolved } } }'
   ```
   Close the window:
   `echo "$(cat "$S/codexN-start.txt")/$(date -u +%Y-%m-%dT%H:%M:%SZ)" >> "$S/codexN-windows.txt"`.
6. **Next cycle.** If the cycle count is below 3, comment `gh pr comment N -R trakt/trakt-android --body "@codex review"`,
   set `SINCE` to now, add 1 to the cycle count and go back to 4.1. After the 3rd cycle, do not
   request another review. Stop monitoring and leave the PR as is.

With several PRs, poll all of them in one loop. Handle one PR at a time, steps 4.2 to 4.5 in full,
so Codex effort windows never overlap and each one counts only its own issue. Polling time sits
outside all windows. Run `git checkout main` when done.

## Step 5 - Report

Build it from the `$S/resultN.md` summaries. Lead with the result. Per issue: link (PR or branch name), what changed in plain words, what was already in place, any deviation from web. Then:

- Tracking: events added and whether they mirror web, or why none.
- Effort: tokens per issue, never one total for the run. PR runs: the numbers from each effort
  report. Local-branch runs have no PR body, so run the script per issue with both ends of its window:
  `--since "$(cat "$S/startN.txt")" --until "$(cat "$S/endN.txt")"`. A run without `--until`
  would also count every later issue.
- Codex effort, when monitored: tokens per PR, kept apart from the build effort above. Pass every
  window of that PR: `--window` once per line of `$S/codexN-windows.txt`.
- Verified: compile and ktlint per branch.
- Not verified: device or emulator runs, unless done.
- Blockers: missing scopes, issues not on the board, anything skipped.
- Board status of each issue.
- Codex review, when monitored: cycles run per PR, comments fixed or declined, and the final state (clean, 3-cycle limit reached, or timed out).

## Gotchas

- `gh` without `project` scope fails on any board query. Ask early, in step 2.
- In zsh, quote globs such as `--include='*.kt'`, or the command fails with `no matches found`.
- Large issue bodies can get truncated in tool output. Dump to `$S` and print with `python3 -c` or `jq -r` when a read looks cut off.
- To change a pushed commit message, amend and push with `-f` only on the feature branch, never on `main`.
