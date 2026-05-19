# app — Claude instructions

Part of the `trakt-android` monorepo. Root rules apply — see
`../AGENTS.md`.

## Overlay for this module

- **Role**: phone app entry point. Hosts the main `NavHost`, the
  `MainActivity`, billing flow, and most user-facing feature screens.
- **Plugin**: `com.android.application` + `kotlin.compose`
  + `kotlin.serialization` + Firebase plugins.
- **Active areas (new code preferred here)**:
  - `Trakt/core/<feature>/` — feature folders (home, calendar,
    search, profile, billing). Each owns `Screen`, `State`,
    `ViewModel`, `di/<Feature>Module.kt`.
  - `Trakt/ui/` — app-level theme glue, snackbar host, extensions.
  - `Trakt/helpers/`, `Trakt/analytics/`.
- **Architecture**: MVVM + UDF. `koinViewModel()` retrieves
  ViewModels. `NavigationStack(path:)`-equivalent via Compose
  Navigation typed routes. See `../.agents/rules/architecture.md`.
- **TV variant**: TV screens live in `:tv`. Share ViewModels and
  state types via `:common`; render with TV-specific Compose layouts.
- **No** new XML layouts, Fragments, or `AppCompatActivity` subclasses.

@../AGENTS.md
