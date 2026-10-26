---
name: trakt-android-release-prep
description: Prepare a trakt-android release - read the current production version name and code from the Play Store, ask which version name to use, propose and write the What's New entry in remote-config/whats_new_schema.json from the top features since the last release tag, bump versionName and versionCode in gradle/libs.versions.toml, commit, then tag HEAD locally. Trigger: /trakt-android-release-prep, "prep release", "prepare release", "release prep", "next release whats new".
---

# Release Prep

Prepares the repo for the next release. Releases are tagged in git with lightweight tags named
after the plain version (`3.24.0`, no `v` prefix).

- Repo: `/Users/michal/Documents/Android/trakt/trakt-android-2025`
- Schema: `remote-config/whats_new_schema.json`
- Play Store access: fastlane with `fastlane/Appfile` (`json_key_file "fastlane/service-account-supply.json"`, gitignored)

Run every command from the repo root. Never push tags or commits. Commit only after user approval. Never upload to the Play Store.

## 1. Check state

```bash
git status -sb
git branch --show-current
```

- Not on `main`: stop and ask before continuing.
- Working tree dirty: report the dirty files and ask before continuing. The tag must point at a known commit.

## 2. Production version on the Play Store

Read-only queries. Each takes a few seconds.

```bash
bundle exec fastlane run google_play_track_release_names track:production 2>&1 | grep 'Result:'
bundle exec fastlane run google_play_track_version_codes track:production 2>&1 | grep 'Result:'
```

- Missing `fastlane/service-account-supply.json` or an auth error: quote the shortest decisive error line and stop.
- More than one value in a result means a staged rollout or several releases. Report all, use the highest.

## 3. Last release tag and local version

```bash
LAST_TAG=$(git tag --merged HEAD --sort=-v:refname | head -1)
git log -1 --format='%h %s (%ad)' --date=short "$LAST_TAG"
git log --oneline "$LAST_TAG"..HEAD | wc -l
grep -E '^version(Code|Name) =' gradle/libs.versions.toml
```

Report a mismatch, do not fix it:

- Production version has no git tag (for example production `3.24.1`, last tag `3.24.0`). The feature range still starts at `LAST_TAG`.
- `versionName` / `versionCode` in `gradle/libs.versions.toml` are lower than production. Step 8 fixes this.

## 4. Ask the version name

Use AskUserQuestion. Base the options on the higher of the production version and `LAST_TAG`:

- Next minor (`3.25.0`), marked recommended.
- Next patch (`3.24.2`).
- Next major (`4.0.0`).

The user can type another value. Validate it as `X.Y.Z`, higher than production, and not an
existing tag (`git rev-parse -q --verify "refs/tags/$VERSION"`).

## 5. Gather features since the last release

```bash
git log --no-merges --format='%h %s' "$LAST_TAG"..HEAD | grep -E '^\w+ (feat|fix|perf)'
```

For squash-merged PRs (`(#123)` in the subject), read the PR body when the title is unclear:

```bash
gh pr view 123 --json title,body --jq '.title + "\n" + .body'
```

Pick the **top 3-4 user-facing features**. Default to 3. Add a 4th only when it is as strong as
the other three. Rank by user impact: new screens or flows first, then visible upgrades to
existing features.

Skip:

- `chore`, `refactor`, `ci`, `build`, `docs`, `style` commits.
- Translation syncs (`feat(i18n)`), analytics and HAL tracking, brand asset refreshes.
- Small UI tweaks, spacing, chevrons, internal plumbing.
- Features already listed in the current schema entry.
- TV-only changes, unless nothing bigger shipped on the phone.

Several related commits can merge into one note (for example a new view plus follow-up polish).

## 6. Propose the What's New entry

File shape: a JSON array of release entries, newest first. It keeps the new entry plus **up to 5
previous entries** (6 in total). Older entries are dropped. Never edit the previous entries.

```json
[
  {
    "id": 28,
    "version": "3.24.1",
    "notes": [
      { "title": "Today", "text": "See what your friends watched ..." }
    ]
  }
]
```

Build the new entry:

- `id`: current highest `id` + 1. The app compares ids to decide whether a user already dismissed the entry.
- `version`: the version name from step 4.
- `notes`: 3-4 items, matching step 5.
  - `title`: 1-3 words, Title Case, feature name as the user sees it in the app.
  - `text`: 1-2 short sentences, under ~160 characters. Say what the user can do and where to find it. Plain everyday English, second person, no internal names, no em-dashes (use "-").

Match the tone of earlier entries: `git log -p -5 -- remote-config/whats_new_schema.json`.

Patch version caveat: the app matches the entry by `major.minor` only (`LoadWhatsNewUseCase.releaseLine`).
For a patch release, tell the user that a new entry with a higher `id` shows again to everyone on that
line, and ask whether to keep the current entry instead.

Show the proposal in chat as a short list: title + text per note, plus the commits or PRs behind
each note. Offer 1-2 alternative features that just missed the cut. Wait for approval or edits.
Do not write the file before approval.

## 7. Write the schema

Insert the approved entry at the top of the array. Keep the 5 newest previous entries (by `id`)
and drop the rest. If the user chose to keep the current entry for a patch release, skip this step.

Keep 2-space indentation and a trailing newline. Validate:

```bash
python3 -c "
import json
data = json.load(open('remote-config/whats_new_schema.json'))
ids = [e['id'] for e in data]
assert len(data) <= 6, len(data)
assert ids == sorted(ids, reverse=True) and len(set(ids)) == len(ids), ids
print(ids)
" && git diff --stat
```

## 8. Bump the app version

Edit `gradle/libs.versions.toml`:

- `versionName` = `$VERSION`.
- `versionCode` = highest of (Play Store production code, local `versionCode`) + 1. Every upload needs a new code, so never reuse one.

The Gradle `afterEvaluate` block in `app/build.gradle.kts` renames the single changelog file in
`fastlane/metadata/android/en-US/changelogs/` to `<versionCode>.txt` on the next sync. Rename it now
with `git mv` so the commit carries it (earlier bumps did the same, for example `442.txt` -> `444.txt`):

```bash
ls fastlane/metadata/android/en-US/changelogs/
git mv fastlane/metadata/android/en-US/changelogs/<old>.txt fastlane/metadata/android/en-US/changelogs/<newCode>.txt
grep -E '^version(Code|Name) =' gradle/libs.versions.toml
```

Skip the rename when the file already has the new name. Leave `default.txt` alone.

## 9. Commit and tag

The tag must include the What's New and version changes, so commit them first. Show `git diff --stat`
and ask for approval before committing:

```bash
git add remote-config/whats_new_schema.json gradle/libs.versions.toml fastlane/metadata/android/en-US/changelogs/
git commit -m "chore(app): prepare release $VERSION" -- remote-config/whats_new_schema.json gradle/libs.versions.toml fastlane/metadata/android/en-US/changelogs/
```

Pass the paths to `git commit` so unrelated staged files stay out of the release commit.

Use a commit message without AI attribution, per `.agents/rules/commits.md`.

Then tag the new HEAD with a lightweight tag, matching existing tags:

```bash
git tag "$VERSION"
git log -1 --format='%h %s %d' "$VERSION"
```

If the user declines the commit, do not tag. Leave the changes uncommitted and say so.

## Report

- Play Store production: version name + code.
- Last tag and commit count since it.
- Version mismatches found in step 3.
- New `versionName` / `versionCode`.
- Final What's New notes (titles only).
- Commit hash and the tag with its commit.
- Next steps:
  - Push the commit and tag when ready: `git -c credential.helper='!gh auth git-credential' push https://github.com/trakt/trakt-android.git main "$VERSION"`
  - Paste the schema into the Firebase Remote Config `mobile_whats_new` parameter.
