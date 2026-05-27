---
trigger: glob
globs: '{resources/src/main/res/**,**/ui/**/*.kt,**/strings.xml}'
description: 'Crowdin source of truth, R.string namespacing (action_/common_/screen_/error_/a11y_), pluralStringResource, exhaustive API enum lookups returning @StringRes.'
applyTo: '{resources/src/main/res/**,**/ui/**/*.kt,**/strings.xml}'
---

# Localization

## Source of truth

- All user-facing strings live in
  `resources/src/main/res/values/strings.xml` (`en` source).
- Translations under `resources/src/main/res/values-<locale>/`
  (`values-de`, `values-fr`, …), populated by Crowdin via
  `.github/workflows/i18n_sync.yml`. Never hand-edit translation files.
- `buildSrc/.../ValidateStringPlaceholdersTask` validates placeholder
  parity across locales — run before merging strings PRs.

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

- **Never** inline raw string literals for user-facing text in composables. Debug logs (via Timber) not localised.
- Reach into `R.string.*` from `resources` package
  (`tv.trakt.trakt.resources.R`).

## Key namespacing

Aligned with existing keys in `values/strings.xml`:

| Prefix              | Used for                                          |
| ------------------- | ------------------------------------------------- |
| `action_*`          | Verbs on buttons / menu items                     |
| `common_*`          | Shared phrases reused across screens               |
| `a11y_*`            | Accessibility-only labels                          |
| `screen_<name>_*`   | Strings scoped to one screen                       |
| `error_*`           | User-facing error copy                             |

Platform-specific suffixes:

- `_tv` — Android TV-only variant
- `_mobile` — phone-only variant when wording differs

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
Never `if (count == 1) "1 episode" else "${count} episodes"` — breaks every non-English locale.

```xml
<plurals name="episodes_left">
    <item quantity="one">%d episode left</item>
    <item quantity="other">%d episodes left</item>
</plurals>
```

## Formatting (dates, numbers, durations)

- Dates via `kotlinx.datetime` + locale-aware helpers in
  `common/.../helpers/formatting/`.
- Numbers via `NumberFormat.getInstance(locale)` with compact notation for large values.
- Durations via existing duration helpers; don't hand-code
  `"$hours h $minutes m"`.

## API enum → translated UI text

When API enum surfaces as translated UI text (genre, episode type, status), build exhaustive lookup helper returning `@StringRes` id, not resolved `String`. Keeps mapper pure + testable without `Context`; ViewModels stay free of Android framework types (per `architecture.md`).

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

- `normalizeKey()` lower-cases + replaces separators so
  `Mid Season Finale` / `mid-season-finale` / `mid_season_finale` all resolve.
- Make `when` **exhaustive** for sealed enums you control. `else -> null` fallback exists only for open-ended API strings — call site decides whether to render raw key or placeholder.
- Recent bug across stacks
  (`keep tag(isLatestAired:provider:) switch exhaustive`) shows cost of swallowing new API values silently.

## TV-specific strings

- Add `_tv` suffix variant when TV wording differs (shorter text, no tap instructions).
- Reuse phone variant when wording identical.

## CrowdIn workflow

- Source-only edits in `values/strings.xml`.
- Crowdin sync runs daily; opens `feat(i18n): translations updates from CrowdIn` PR.
- New keys ship English-only; translations land via next Crowdin sync.
- `ValidateStringPlaceholdersTask` blocks merges with placeholder mismatches.

## Don'ts

- Don't hand-edit any `values-<locale>/strings.xml`. Crowdin owns them.
- Don't introduce keys without namespace prefix.
- Don't use `String.format` with positional args (`%s %s`) when keys could clash on translator interpretation — use named placeholders via `<xliff:g>` tags.
- Don't concatenate user-facing strings with `+`. Use formatted templates.
- Don't ship locale-specific code paths (`if (locale == "de") …`). All locale logic flows through resources and `Intl.*`-style helpers.