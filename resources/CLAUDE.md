# resources — Claude instructions

Part of the `trakt-android` monorepo. Root rules apply — see
`../AGENTS.md`.

## Overlay for this module

- **Role**: pure resource library. Strings, drawables, locale variants.
  No Kotlin sources.
- **Plugin**: `com.android.library`.
- **Hard rule**: depends on nothing. Bottom of the graph.
- **Active areas**:
  - `src/main/res/values/strings.xml` — `en` source. Hand-edit here.
  - `src/main/res/values-<locale>/strings.xml` — translations. **Never
    hand-edit.** Crowdin owns them; the
    `.github/workflows/i18n_sync.yml` job opens daily sync PRs.
  - `src/main/res/drawable/` — vector and raster assets. Reference via
    `R.drawable.<name>`.
- **String-key namespacing**: `action_*`, `common_*`, `a11y_*`,
  `screen_<name>_*`, `error_*`. `_tv` suffix for TV-only variants.
  See `../.agents/rules/localization.md`.
- **Placeholder parity**: `buildSrc/.../ValidateStringPlaceholdersTask`
  guards against placeholder mismatches across locales. Run before
  merging.

@../AGENTS.md
