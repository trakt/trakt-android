# tv — Claude instructions

Part of the `trakt-android` monorepo. Root rules apply — see
`../AGENTS.md`.

## Overlay for this module

- **Role**: Android TV variant. Hosts `TvActivity`, `TvSplashActivity`,
  and a Compose-only UI tuned for D-pad navigation. Reuses `:common`
  ViewModels and domain types where possible.
- **Plugin**: `com.android.library` + `kotlin.compose`
  + `kotlin.serialization`.
- **Active areas (new code preferred here)**:
  - `tv/trakt/trakt/app/core/<feature>/` — TV feature flows (home,
    lists, auth, streamings, episodes, comments).
  - `tv/trakt/trakt/app/ui/theme/` — TV-specific `TraktTheme` wrapper
    over `androidx.tv.material3.MaterialTheme`.
- **TV-specific considerations**:
  - 10-foot legibility — larger touch targets, larger typography
    (`TraktTheme.typography.title` one tier up from phone).
  - Honour the focus engine: `.focusable`, `.focused`, `.focusEffect`.
    Don't fight platform focus indicators.
  - D-pad navigation comes free with `Modifier.clickable`; do not
    override with custom gesture detection.
  - `androidx.tv.material3` `Surface` / `Button` — not the phone
    Material 3 equivalents.
- **State**: same as phone — `StateFlow<UiState>` + sealed states.
  Share types via `:common`.
- **Navigation**: typed Compose Navigation routes per feature, same
  conventions as `:app`.

@../AGENTS.md
