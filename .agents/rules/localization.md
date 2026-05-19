---
trigger: glob
globs: '{resources/src/main/res/**,**/ui/**/*.kt,**/strings.xml}'
description: 'Crowdin source of truth, R.string namespacing (action_/common_/screen_/error_/a11y_), pluralStringResource, exhaustive API enum lookups returning @StringRes.'
applyTo: '{resources/src/main/res/**,**/ui/**/*.kt,**/strings.xml}'
---

# Localization

## Source of truth

- All user-facing strings live in
  `resources/src/main/res/values/strings.xml` (the `en` source).
- Translations land under `resources/src/main/res/values-<locale>/`
  (`values-de`, `values-fr`, …), populated by Crowdin via
  `.github/workflows/i18n_sync.yml`. Never hand-edit translation files.
- `buildSrc/.../ValidateStringPlaceholdersTask` validates placeholder
  parity across locales — run it before merging strings PRs.

## Reading strings in Compose

Use `stringResource` / `pluralStringResource` from
`androidx.compose.ui.res`:

```kotlin
Text(stringResource(R.string.screen_movie_summary_title))

Text(
    pluralStringResource(
        id = R.plurals.episodes_left,
        count = remaining,
        remaining,
    )
)
```

- **Never** inline raw string literals for user-facing text in
  composables. Debug logs (via Timber) are not localised.
- Reach into `R.string.*` from `resources` package
  (`tv.trakt.trakt.resources.R`).

## Key namespacing

Borrowed from web/iOS conventions and aligned with what already exists
in the `values/strings.xml`:

| Prefix              | Used for                                          |
| ------------------- | ------------------------------------------------- |
| `action_*`          | Verbs on buttons / menu items                     |
| `common_*`          | Shared phrases reused across screens               |
| `a11y_*`            | Accessibility-only labels                          |
| `screen_<name>_*`   | Strings scoped to one screen                       |
| `error_*`           | User-facing error copy                             |

Platform-specific suffixes:

- `_tv` — Android TV-only variant
- `_phone` — phone-only variant when wording differs

Example keys:

```
screen_movie_summary_title
screen_movie_summary_cta_play_tv
action_remove_from_watchlist
error_credits_load_failed
a11y_rating_picker
```

## Pluralisation

Use `<plurals>` in `strings.xml` and `pluralStringResource` in code.
Never `if (count == 1) "1 episode" else "${count} episodes"` — it
breaks every non-English locale.

```xml
<plurals name="episodes_left">
    <item quantity="one">%d episode left</item>
    <item quantity="other">%d episodes left</item>
</plurals>
```

## Formatting (dates, numbers, durations)

- Dates via `kotlinx.datetime` + locale-aware helpers in
  `common/.../helpers/formatting/`.
- Numbers via `NumberFormat.getInstance(locale)`. Mirror trakt-web's
  `toHumanNumber` (compact notation) when porting.
- Durations via existing duration helpers; don't hand-code
  `"$hours h $minutes m"`.

## API enum → translated UI text

When an API enum surfaces as translated UI text (genre, episode type,
status), build an exhaustive lookup helper that returns a `@StringRes`
id, not a resolved `String`. This keeps the mapper pure and testable
without a `Context`, and lets ViewModels stay free of Android
framework types (per `architecture.md`).

```kotlin
@StringRes
fun episodeTypeLabel(raw: String): Int? = when (raw.normalizeKey()) {
    "standard"          -> R.string.common_episode_type_standard
    "mid_season_finale" -> R.string.common_episode_type_mid_season_finale
    "season_finale"     -> R.string.common_episode_type_season_finale
    "series_finale"     -> R.string.common_episode_type_series_finale
    else                -> null
}

// In a composable:
val labelRes = episodeTypeLabel(episode.type)
Text(
    text = labelRes?.let { stringResource(it) } ?: episode.type,
)
```

- `normalizeKey()` lower-cases and replaces separators so
  `Mid Season Finale` / `mid-season-finale` / `mid_season_finale` all
  resolve.
- Make the `when` **exhaustive** for sealed enums you control. The
  `else -> null` fallback exists only for open-ended API strings — the
  call site decides whether to render the raw key or a placeholder.
- Recent bug across stacks
  (`keep tag(isLatestAired:provider:) switch exhaustive`) shows the
  cost of swallowing new API values silently.

## TV-specific strings

- Add a `_tv` suffix variant when the TV wording differs (shorter
  text, no instructions referencing tapping).
- Reuse the phone variant when wording is identical.

## CrowdIn workflow

- Source-only edits in `values/strings.xml`.
- Crowdin sync workflow runs daily; opens a `feat(i18n): translations
  updates from CrowdIn` PR.
- New keys ship with English only; translations land via the next
  Crowdin sync.
- `ValidateStringPlaceholdersTask` blocks merges that introduce
  placeholder mismatches.

## Don'ts

- Don't hand-edit any `values-<locale>/strings.xml` file. Crowdin owns
  them.
- Don't introduce keys without a namespace prefix.
- Don't use `String.format` with positional args (`%s %s`) when keys
  could clash on translator interpretation — use named placeholders
  via `<xliff:g>` tags.
- Don't concatenate user-facing strings with `+`. Use formatted
  templates.
- Don't ship locale-specific code paths (`if (locale == "de") …`).
  All locale logic flows through resources and `Intl.*`-style helpers.
