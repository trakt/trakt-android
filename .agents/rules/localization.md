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

Platform-specific suffixes:

- `_tv` — Android TV-only variant
- `_mobile` — phone-only variant when wording differs

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
