---
name: trakt-android-backlog-sizing
description: Size trakt-android Backlog issues on the trakt Product board (XS/S/M/L) from issue scope, linked trakt-web PRs and the current Android code, publish a review table artifact, then write the approved sizes to the board's Size field. Default run sizes only Backlog items with no Size yet. Trigger: /trakt-android-backlog-sizing, "size the backlog", "re-run backlog sizing", "assess backlog sizes", "size new backlog issues".
---

# Backlog sizing

Proposes a Size for every trakt-android issue in the Backlog column of the org Product board, shows the proposal as a review table, and writes the sizes only after the user approves.

## Constants

| What | Value |
| --- | --- |
| Board | `gh project` owner `trakt`, number `11` ("Product") |
| Project node ID | `PVT_kwDOAE01zc4BTaq0` |
| Size field ID | `PVTSSF_lADOAE01zc4BTaq0zhBtZqU` |
| Size option IDs | XS `00353ce8`, S `a4ea46fe`, M `7e093684`, L `6b54fab6` (XL `c60556e2` exists but is not used) |
| Repo filter | `https://github.com/trakt/trakt-android` |
| Android code | `/Users/michal/Documents/Dev/Android/trakt/trakt-android-2025` |
| Web code | `/Users/michal/Documents/Dev/Web/trakt-web` (read only, never modify) |
| Review artifact | `https://claude.ai/artifact/92H6Mvtjn8s5QP6oA5nPWg` |
| Page template | `review-template.html` next to this file |

If a field or option ID stops working, refresh it with `gh project field-list 11 --owner trakt --format json` and update this table.

## Modes

Read the user's arguments:

- **No arguments (default):** Backlog trakt-android items that have no Size yet.
- **`all`:** every Backlog trakt-android item, including already sized ones. Existing sizes are proposals to keep or change, never silently overwritten.
- **Issue numbers (`#519 #524`):** only those issues, whatever their size.

## Steps

Do the work inline. Do not spawn subagents (project rule). Keep raw dumps in the session scratchpad directory, referred to below as `$S`.

1. **Check auth.**
   ```bash
   gh auth status 2>&1 | grep -i scopes
   ```
   Reading needs `read:project`. Writing sizes in step 8 needs `project`. If a scope is missing, ask the user to run `! gh auth refresh -h github.com -s project` and wait. The `-h` flag is required because the `!` prompt is not interactive. If it still fails, the user runs the same command in a normal terminal.

2. **Fetch board items.**
   ```bash
   gh project item-list 11 --owner trakt --limit 3000 --format json > "$S/items.json"
   jq '[.items[] | select(.status=="Backlog" and .repository=="https://github.com/trakt/trakt-android")] | length' "$S/items.json"
   ```
   If the count of all items returned equals the limit, raise the limit and fetch again.
   Select the target items for the mode. Keep each item's `id` (needed in step 8), `content.number`, `title`, `assignees` and current `size`.

3. **Fetch issue bodies, skip closed issues.**
   ```bash
   gh issue list -R trakt/trakt-android --state open --limit 500 --json number,title,body,labels,assignees > "$S/issues.json"
   ```
   Board items whose issue is closed are left out of the table and listed in the footer ("closed as duplicate / completed"). They may still sit in the Backlog column until the board archives them.

4. **Size the linked web PRs.** Extract `trakt-web#N` and `trakt-web/pull/N` references from each body, then fetch all stats in one GraphQL call with aliases (`p2376: pullRequest(number: 2376) { number additions deletions changedFiles }`). In zsh, split the number list with `xargs -n1` or `${=VAR}`; a plain `for p in $VAR` does not split. Sum additions, deletions and files per issue. For big or unclear items, read the PR file list with `gh pr view N -R trakt/trakt-web --json files`.

5. **Check the Android code.** For each issue:
   - Open the paths cited under "Checked on `main` at ..." and confirm they still match. Code may have changed since the issue was written.
   - Grep for the feature's key terms in `app/`, `common/` and `tv/` (quote globs in zsh: `--include='*.kt'`).
   - Work out the **remaining Android gap**. Web PR size is only a hint. Android often has part of the feature already (examples: GIFs existed except categories, stories existed except the Today overview).

6. **Pick a size and flags** using the rubric below. Write one plain-English reason per issue that says what is left to build.

7. **Publish the review table.** Fill `review-template.html`:
   - `/*ROWS*/`: one JS array per issue, `[number, "title", who, "SIZE", "reason", "web", ["flags"]],` where `who` is `ME` (michaldrabik), `KC` (kevincador), `NA` (unassigned) or a quoted login, and `web` is `"+A −D"` or `""`. Escape double quotes in titles and reasons.
   - `/*DATE*/`: today's date.
   - `/*EXCLUDED*/`: closed or merged issues left out, plus "#496 is the web parity parent issue and gets no size." if it is in scope.
   - In `all` mode, add a `Was X` flag on rows whose proposed size differs from the current one.
   Write the page to `$S/backlog-sizing.html`. Read the review artifact once (`Artifact` action `read`, url above), then publish the file with that `url` so the link stays the same. If the read fails because the artifact was deleted, publish without `url` and update the Constants table with the new link.
   Reply with the link, the size counts and any issues that need a decision (triage, scope, overlaps). Then stop and wait.

8. **Apply after approval.** Apply the user's corrections first, then write each size:
   ```bash
   gh project item-edit --project-id PVT_kwDOAE01zc4BTaq0 --id <ITEM_ID> \
     --field-id PVTSSF_lADOAE01zc4BTaq0zhBtZqU --single-select-option-id <OPTION_ID>
   ```
   Run them in a loop, collect failures, and retry each failure once. Never change any other field. Afterwards re-fetch the board and confirm every target item shows the expected Size.

## Rubric

Size by complexity, effort, amount of change and review weight. Never by days.

- **XS:** a tweak to one screen or file. Uses existing data. No new logic.
- **S:** one feature area following an existing pattern. Light new UI, a few strings, little review.
- **M:** several screens or layers, new state or logic that needs tests, or a new remote call through existing clients. Medium review.
- **L:** a new flow or many new screens, VIP or permission edge cases, data-removing or auth-sensitive work, cross-cutting changes across most screens, or many strings. Heavy review.

Rules:

- **Ignore API availability.** A missing endpoint or a field absent from `openapi/openapi.json` does not raise the size and gets no flag. Size the Android client work only.
- Size the remaining Android gap, not the web PR.
- Assume one developer. Include review, not QA.
- Parent or tracking issues (like #496) get no size.
- `Needs triage` items are sized and written like every other item. The flag only marks them for a decision.
- An item too big even for L is still L, with a `Split candidate` flag. Do not use XL.

## Flags

Use only these, so the table filters stay useful:

- `Needs triage`: the body asks someone to confirm the item belongs on mobile.
- `Needs scope`: the body is empty or open-ended.
- `Split candidate`: an L that bundles several separable features.
- `Overlaps #N`: another open Backlog issue covers the same work. Suggest merging.
- `Depends on #N`: cannot start before another issue.
- `VIP`, `Preview flag`: gated features.
- `Data removal`, `Auth`, `New dependency`: risky areas that need extra review.
- `Verify first`: the app may already do this; check before building.
- `Merged #N`, `Was X`: bookkeeping.

## Calibration (October 2026 run)

Use these as anchors so sizes stay consistent between runs:

- **XS:** #484 highlight current episode, #520 mark watched in Continue Watching menu, #527 watched tag on own reviews, #531 two creators in header, #499 add 10 country slugs.
- **S:** #524 change username (existing settings edit pattern), #535 watchlist reorder (reuses list drag reorder), #513 locked account 423 screen, #463 track action in 3 more menus, #525 GIF category tiles.
- **M:** #519 progress sorting (3 screens, saved sort, Smart sort logic with tests), #530 notes CRUD, #504 spoiler images everywhere, #497 logout revoke and refresh errors (auth), #528 Today overview screen.
- **L:** #509 list collaborators (new flow, picker, limits, leave), #501 Mixtape, #474 app-wide undo toasts, #10 offline and connectivity errors across all screens, #67 landscape for every screen.

## Report

- Mode used and number of items sized.
- Review artifact link.
- Counts per size.
- Items needing a decision, grouped by flag.
- After applying: items written, failures, and the verification result.

## Notes

- Never edit issues, labels, assignees or any board field other than Size.
- Do not commit this skill's output anywhere. The review page lives only as an artifact.
