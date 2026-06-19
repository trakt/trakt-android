---
trigger: glob
globs: '**'
description: 'Tech stack, module layout, dependency graph, tooling. Apply to all files.'
applyTo: '**'
---

# Project Guidelines

## Tech Stack

Trakt Android codebase. Phone + Android TV, Compose-first, Kotlin 2.x, Gradle KTS with version catalogue, OpenAPI-generated client.

- **Language**: Kotlin 2.3.x. `kotlin.code.style=official` set in `gradle.properties`.
- **JVM target**: 11 (`jvmToolchain(11)`).
- **UI**: Jetpack Compose for **everything**. Material 3 + adaptive window size classes. No XML layouts, no Fragments, no `AppCompatActivity` views.
- **Compose BOM**: `2026.05.00` (Material3 `1.5.0-alpha19`).
- **Min SDK 28, Compile/Target SDK 37, AGP 9.2.x.**
- **Async**: Coroutines + Flow. No RxJava, no `LiveData` in new code.
- **DI**: Koin 4.2.x (module DSL). No Hilt, no Dagger — codebase intentionally standardised on Koin.
- **Networking**: Ktor 3.4.x client with OkHttp engine. API surface generated from `openapi/openapi.json` via `openapi-generator` Gradle plugin. Generated sources land in `build/generate-resources/`.
- **Persistence**: DataStore (Preferences) + entity caches. No SharedPreferences in new code, no Realm, no SQLDelight.
- **Images**: Coil 3 (`io.coil-kt.coil3`) with Ktor 3 engine adapter and SVG decoder.
- **Serialization**: kotlinx.serialization for protobuf and typed navigation routes; Moshi for OpenAPI-generated DTOs.
- **Firebase**: Crashlytics, Analytics, Remote Config (seasonal themes + feature flags).
- **Media**: Media3 (ExoPlayer) for video; `youtube-player` (thirdspark) for embedded trailers.
- **Logging**: Timber.

## Project Structure

```
trakt-android/
├── app/                 # com.android.application — phone entry point
│   └── src/main/java/tv/trakt/trakt/
│       ├── core/<feature>/  # feature folders: home, calendar, search, …
│       ├── ui/              # theme, components, snackbar, extensions
│       ├── helpers/
│       └── analytics/
├── tv/                  # com.android.library — Android TV variant
│   └── src/main/java/tv/trakt/trakt/app/
│       ├── core/<feature>/  # TV-specific feature flows
│       └── ui/theme/
├── common/              # com.android.library — domain, networking, DI
│   └── src/main/java/tv/trakt/trakt/common/
│       ├── networking/      # Ktor client factory, interceptors
│       ├── firebase/
│       ├── helpers/
│       ├── di/              # Koin modules wired in TraktApplication
│       └── ui/              # cross-platform Compose primitives
├── resources/           # com.android.library — strings, drawables, i18n
├── buildSrc/            # ValidateStringPlaceholdersTask + future plugins
├── external/            # third-party drop-in source
├── openapi/             # openapi.json spec + generator config
├── gradle/libs.versions.toml
└── fastlane/
```

## Module Dependency Direction

```
            app
           /   \
          ▼     ▼
         tv    common
          \   /   |
           ▼ ▼    ▼
        common  resources
           |
           ▼
        resources
```

- **`app` depends on**: `common`, `tv`, `resources`.
- **`tv` depends on**: `common`, `resources`.
- **`common` depends on**: `resources`.
- **`resources` depends on**: nothing.

Hard rule: **never reverse graph.** `common` cannot import from `app` or `tv`. `resources` cannot import from anything.

## Architecture Patterns

### Compose-first
Every screen = `@Composable` suffixed `Screen`. State hoisted out of view; `ViewModel` owns it, exposes `StateFlow`.

### MVVM + UDF
ViewModels expose `StateFlow<UiState>`. Composables call `viewModel.state.collectAsStateWithLifecycle()`, pass user events back as lambdas. Side effects in `LaunchedEffect`.

### Feature folders
Group by feature (`core/home`, `core/calendar`, `core/search`, `core/profile`, `core/billing`). Each feature carries `Screen.kt`, `State.kt`, `ViewModel.kt`, `di/<Feature>Module.kt` Koin module.

### Custom theme tokens

All visual tokens in `common/.../ui/theme/`: `TraktTheme.colors`, `TraktTheme.typography`,
`TraktTheme.spacing`, `TraktTheme.size`. Exposed via `CompositionLocal`. Semantic/theme-aware
colours go through `TraktTheme.colors`; raw `Color(…)` allowed only for brand, decorative, preview,
or computed values (see `theming.md`). No magic-number paddings in feature code.

### OpenAPI-driven data layer
API DTOs generated from `openapi/openapi.json`. **Not** hand-edited. Hand-written mappers in `common/.../networking/` translate generated DTOs into domain models defined in `common/.../model/`.

## Commit Standards

Conventional Commits with android-scoped enum. See `commits.md` for full allowed scope list.

```
feat(app): add notes drawer to summary
fix(tv): TV get-upcoming case fix
refactor(common): derive isLatestAired from remaining episode count
chore(i18n): translations updates from CrowdIn
```

## Styling & Theming

- All visual values via `TraktTheme.*` tokens.
- Material 3 base with `TraktTheme` overlays for colours and typography.
- Light/dark + seasonal theme overrides (Halloween → orange, Christmas → red) flow through Firebase Remote Config + `CustomThemeUseCase` — wired in `MainActivity`.
- Images via Coil 3 + Trakt placeholders; do not introduce Glide / Fresco.

## Logging

- **Timber for all logging.** Never `android.util.Log.*` directly.
- **Lazy formatting**: `Timber.d("user=%s", user.id)` — Timber elides formatting when log level disabled. Never `Timber.d("user=" + user.id)` or string-interpolation (`Timber.d("user=${user.id}")`) for messages built every call.
- **Tag implicitly** via calling class. Avoid `Timber.tag("X")` except inside helper utilities that lose class context.
- **Never log secrets, tokens, OAuth codes, refresh tokens, or request/response bodies** that may include any. Trakt IDs (movie IDs, show IDs, slugs, list IDs) safe to log. User emails / display names **not** — PII.
- `Timber.plant()` only in debug builds; release builds plant Crashlytics tree forwarding `WARN`+ to Firebase Crashlytics. Wired in `TraktApplication.setupTimber()`.

## Tooling

- **Build**: `./gradlew :app:assembleDebug`, `./gradlew :tv:assembleDebug`.
- **Format**: `./gradlew ktlintFormat` (where wired; honour `.editorconfig` ktlint rules).
- **Lint**: `./gradlew ktlintCheck` (gated by `.github/workflows/master.yml` ktlint job).
- **OpenAPI regeneration**: `./gradlew openApiGenerate` rebuilds client from `openapi/openapi.json` — committed generated sources stay in step.
- **i18n sync**: Crowdin → `resources/src/main/res/values-*/strings.xml` via `.github/workflows/i18n_sync.yml`.
- **Releases**: Fastlane (`fastlane/`) — 7 lanes covering Firebase distribution + Play Store internal/beta/production tracks.
