---
trigger: glob
globs: '{**/build.gradle.kts,**/settings.gradle.kts,gradle/libs.versions.toml,buildSrc/**,build-logic/**}'
description: 'Module dependency direction, version catalogue, external deps justification, per-feature module opt-in, convention plugins, lint baseline.'
applyTo: '{**/build.gradle.kts,**/settings.gradle.kts,gradle/libs.versions.toml,buildSrc/**,build-logic/**}'
---

# Gradle Modules & Dependencies

## Module map

| Module       | Plugin                      | Role                                          |
| ------------ | --------------------------- | --------------------------------------------- |
| `:app`       | `com.android.application`   | Phone app entry point + most feature screens  |
| `:tv`        | `com.android.library`       | Android TV variant entry + TV screens         |
| `:common`    | `com.android.library`       | Domain models, networking, DI, theme tokens   |
| `:resources` | `com.android.library`       | Strings, drawables, locale variants           |
| `:buildSrc`  | `kotlin-dsl`                | Gradle plugins (e.g., `ValidateStringPlaceholdersTask`) |

## Dependency direction (hard rule)

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

- `app` depends on `common`, `tv`, `resources`.
- `tv` depends on `common`, `resources`.
- `common` depends on `resources`.
- `resources` depends on nothing.

**Never reverse the graph.** `common` = boundary — no `Activity`, `Application`, or `Composable` consumers; only domain, networking, DI, reusable UI tokens.

## Version catalogue

Single source of truth: `gradle/libs.versions.toml`. Rules:

- **Add new deps to catalogue first.** No literal coordinates in `build.gradle.kts` (`implementation("com.example:foo:1.0")`).
- Reference via alias: `implementation(libs.compose.material3)`, `implementation(libs.coil.compose)`.
- Group related deps in bundles (`libs.bundles.compose.testing`).
- Plugin aliases via catalogue too: `alias(libs.plugins.kotlin.serialization)`.

## Adding new external dependency

PR description must include:

1. **Why** — need it serves that existing dep or stdlib doesn't cover.
2. **Maintenance posture** — last release, contributor count, license.
3. **Footprint** — APK size impact (run `:app:reportDependencyGraphChange` or eyeball AAR size), transitive deps, R8 friendliness.
4. **Exit plan** — how to remove if unmaintained.

Defaults to **rejected** until justified. AndroidX, Jetpack, Compose ecosystem libs and well-established projects (Coil, Ktor, OkHttp, Coroutines, Turbine) clear bar trivially.

## Compose / Kotlin / Compiler alignment

- Compose BOM = single version source for Compose deps. Don't pin individual `androidx.compose.*` versions.
- Kotlin and Compose Compiler must stay in sync via `org.jetbrains.kotlin.plugin.compose` plugin (already in catalogue). Don't override.
- `kotlinx-serialization`, `kotlinx-datetime`, `kotlinx-coroutines` — pin via catalogue, use coroutine BOMs where they exist.

## Per-feature modules (future)

Codebase intentionally flat today (5 modules). Per-feature Gradle modules (e.g., `:feature:home`, `:feature:notes`) **may** be introduced once feature is genuinely self-contained, mirroring patterns from Now in Android (`:feature:foo`) or Tivi (`:ui:foo` + `:data:foo`).

Skeleton (if/when):

```
:feature:notes
├── build.gradle.kts                 # com.android.library + kotlin.compose
└── src/main/java/tv/trakt/trakt/feature/notes/
    ├── NotesScreen.kt
    ├── NotesViewModel.kt
    ├── NotesState.kt
    ├── data/                        # repository, mappers, cache
    └── di/NotesModule.kt
```

Rules if/when adopted:

- Feature modules depend on `:common` and `:resources` only.
- **Feature → feature forbidden.** Cross-feature deps go through `:common` or shared `:core:<name>` module.
- `internal` by default. Expose umbrella screen composable + Koin module + navigation graph extension as `public`.

Not mandatory yet. First feature to graduate establishes template.

## Convention plugins

`buildSrc/` hosts `ValidateStringPlaceholdersTask`. As build logic grows, migrate to Gradle composite build under `build-logic/convention/` with these plugin aliases:

- `trakt.android.application` — applies `com.android.application` + shared `compileSdk`, `defaultConfig`, `compileOptions`, `signingConfigs` boilerplate.
- `trakt.android.library` — `com.android.library` equivalent.
- `trakt.android.library.compose` — extends `trakt.android.library` with `org.jetbrains.kotlin.plugin.compose`, `composeOptions`, stability-config wiring.
- `trakt.android.feature` — extends `trakt.android.library.compose` with typical feature module deps (`:common`, `:resources`, Koin, Navigation Compose).
- `trakt.android.lint` — Android Lint config (`xmlReport`, `sarifReport`, `checkDependencies = true`, baseline file path).
- `trakt.jvm.library` — pure JVM module (`org.jetbrains.kotlin.jvm`).

After migration, each module's `build.gradle.kts` = ~10 lines of convention plugins + `dependencies { … }` block — no copy-pasted SDK/Compose/Kotlin config blocks. See [android/nowinandroid `build-logic/convention/`](https://github.com/android/nowinandroid/tree/main/build-logic/convention) for canonical layout.

Element X Android's [`plugins/`](https://github.com/element-hq/element-x-android/tree/develop/plugins) shows alternative: small plugins delegating to `extension/` folder of helpers (`androidConfig`, `composeConfig`, `setupKover`). Either fine; pick one, stay consistent.

## Lint

Per-module setup:

- Every Android module owns `lint-baseline.xml` (committed). Baseline lets existing warnings not block new work. **Baseline never grows from PR** — new violations on changed lines must be fixed, not baselined.
- `lint.xml` overlay exists only when module needs rules differing from project defaults.
- `trakt.android.lint` convention plugin sets:
  - `xmlReport = true`
  - `sarifReport = true`
  - `checkDependencies = true`
- CI uploads SARIF output to GitHub Code Scanning — violations appear inline on PR diffs.

## Formatting pipeline

Today: `ktlint` via `./gradlew ktlintFormat` / `ktlintCheck`, wired into CI through `.github/workflows/master.yml`.

Aspirational: move to **Spotless** — formatting, ktlint, license headers, import ordering in one
pass (both NIA and Tivi do this). Until migrated, honour `.editorconfig` rules and run ktlint before
pushing.

## OpenAPI generator

Lives at root `build.gradle.kts`. Three rules:

- **Spec = source of truth** — edit `openapi/openapi.json`, not generated Kotlin.
- **Re-run after editing spec**: `./gradlew openApiGenerate`.
- **Commit generated diff.** Don't expect downstream contributors to regenerate locally.

## Plugin / version pins of note

- `kotlin.code.style=official` set in `gradle.properties`.
- `android.useAndroidX=true`, `android.nonTransitiveRClass=true`.
- `org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8`.
- KSP over KAPT — new annotation-processed code use KSP.

## Quick checklist

- [ ] New dep added to `gradle/libs.versions.toml` first
- [ ] Module dependency direction respected
- [ ] No literal coordinates in `build.gradle.kts`
- [ ] PR body justifies new external deps
- [ ] OpenAPI spec edit re-runs generator and commits diff
- [ ] No `Activity` / `Context` references in `:common`
