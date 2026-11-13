---
name: trakt-android-release-notes
description: Keep the "Trakt Android Releases" artifact in sync with the repo - one human-readable entry per release tag plus an "Up Next" entry for unreleased commits on main. Summarizes the commits between tags into New / Improved / Fixed / TV / Under the hood and writes them to the artifact database. Runs unattended as a daily scheduled task. Trigger: /trakt-android-release-notes, "sync release notes", "update release history", "backfill release notes".
---

# Release notes sync

Writes database rows without asking: rows are cheap to fix and the run is unattended. Never edits, commits or pushes anything in the repo. A sync never republishes the page, it only writes rows.

## Constants

| What | Value |
| --- | --- |
| Repo | `trakt/trakt-android` |
| Artifact title | `Trakt Android Releases` |
| Page source | `page.html` next to this file |
| Capabilities | `{"db": {"rules": [{"path": "", "read": "view", "write": "admin"}]}}` |
| Release rows | collection `releases`, doc id = version (`3.28.0`) |
| Up Next row | collection `upnext`, doc id `current` |
| Sync state | collection `meta`, doc id `sync` |
| What's New source | `remote-config/whats_new_schema.json` |

The artifact link is not stored here. Use the link given in the request; otherwise find it with the Artifact tool (`action: "list"`, `scope: "all"`) by title. If none exists, go to "First run".

## Row shapes

Release (`releases/<version>`):

```json
{
  "date": "YYYY-MM-DD", "prev": "X.Y.Z", "prevDate": "YYYY-MM-DD",
  "commits": 54, "prs": 14, "sortKey": 3028000,
  "headline": "short phrase naming the 1-3 biggest changes",
  "summary": "1-2 plain sentences",
  "highlights": [{ "title": "2-3 words", "text": "one sentence" }],
  "sections": [{ "key": "new", "items": [{ "text": "one change", "prs": [629] }] }]
}
```

Up Next (`upnext/current`): same `commits`, `prs`, `headline`, `summary`, `sections`, with `"highlights": []` and, instead of the date fields, `"since": "X.Y.Z"`, `"sinceDate"`, `"asOf": "<today>"`, `"head": "<short sha of main>"`.

Sync state (`meta/sync`): `{"checkedAt": "YYYY-MM-DD", "latestTag": "X.Y.Z"}`.

- `sortKey` = major * 1000000 + minor * 1000 + patch.
- `prs` count = commit subjects ending in `(#N)`.
- Section keys, in this order, empty ones omitted: `new` (new user-visible features), `improved` (changes to existing features), `fixed` (bug fixes), `tv` (anything scoped to Android TV), `internal` (refactors, analytics, build, tooling).

## Writing rules

- Drop noise commits: CrowdIn and i18n syncs, openapi regen, deps and version bumps, CI, docs and README, agent rules and skills, "prepare release", lint, strings cleanup, pure "UI tweak" commits.
- Plain English for the whole team, user-facing wording, no commit prefixes, no trailing periods on items, no em-dashes.
- Merge related commits into one item and list all their PR numbers.
- Describe only what the commits or PRs state. Do not guess at what an unclear change does. Only when a PR-backed subject is unclear, read it with `gh pr view <n> --json title,body`. Do not read every PR.
- Highlights: when `whats_new_schema.json` has an entry for the version, use its notes (light rewording allowed). Otherwise write 0-4 yourself.
- A change that affects who can install or use the app (min SDK, removed feature) goes in the `summary` as well.

## Steps

1. **Fetch.** `git -c credential.helper='!gh auth git-credential' fetch --tags https://github.com/trakt/trakt-android.git main`. MAIN = `FETCH_HEAD`. If the fetch fails, use local `main` and local tags and say so in the report.

2. **List tags.** `git tag --sort=-creatordate`, keep only tags shaped `X.Y.Z`. LATEST = the newest one.

3. **Read the store.** Load the `ArtifactData` tool. `list` collection `releases`, `get` `meta/sync`, `get` `upnext/current`. Keep each `version` and pass it as `if_version` on every write to a row that already exists.

4. **Add missing releases.** Missing = tags newer than the newest stored version that have no row. Never rewrite an existing release row. For each missing tag (prev = the tag right before it in the sorted list):
   - `git log --format=%s prev..tag`, `git rev-list --count prev..tag`, dates from `git log -1 --format=%cs <tag>`.
   - If `upnext/current` has `since` equal to prev, start from its items instead of rewriting from scratch.
   - `set` the release row.

5. **Refresh Up Next.** HEAD_SHA = `git rev-parse --short MAIN`.
   - Stored `head` equals HEAD_SHA and `since` equals LATEST: leave it alone.
   - Stored `since` equals LATEST: read only the new commits (`git log --format=%s <stored head>..MAIN`), merge them into the existing sections, refresh the counts from `LATEST..MAIN`.
   - Otherwise (new release, or no row yet): rebuild from `git log --format=%s LATEST..MAIN`.
   - With zero commits after LATEST, write `commits: 0` and `sections: []`. The page hides the entry.

6. **Stamp the run.** `update` `meta/sync` with today's date and LATEST.

7. **Report.** Versions added with item counts, whether Up Next changed, anything skipped or failed. If nothing changed, one line.

## First run

No artifact with that title: publish `page.html` with the capabilities above and icon `tag`, then run the steps with the last 10 `X.Y.Z` tags treated as missing (or the count the user asked for). Give the user the link and say it stays private until they share it.

## Backfill

Only on request ("backfill release notes to 3.10.0"). Add rows for the older tags the same way as step 4. This is the only case that writes tags older than the oldest stored version.

## Page changes

The page holds no data, it reads the rows live. To change layout or behaviour: edit `page.html`, read the artifact (`action: "read"`), then publish `page.html` with its `url`. Leave `capabilities` out so the stored declaration carries over.
