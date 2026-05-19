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

**Never reverse the graph.** `common` is the boundary — it has no
`Activity`, `Application`, or `Composable` consumers; only domain,
networking, DI, and reusable UI tokens.

## Version catalogue

Single source of truth at `gradle/libs.versions.toml`. Rules:

- **Add new deps to the catalogue first.** No literal coordinates in
  `build.gradle.kts` (`implementation("com.example:foo:1.0")`).
- Reference via alias: `implementation(libs.compose.material3)`,
  `implementation(libs.coil.compose)`.
- Group related deps in bundles (`libs.bundles.compose.testing`).
- Plugin aliases via the catalogue too:
  `alias(libs.plugins.kotlin.serialization)`.

## Adding a new external dependency

PR description must include:

1. **Why** — the need it serves that an existing dep or stdlib doesn't
   cover.
2. **Maintenance posture** — last release, contributor count, license.
3. **Footprint** — APK size impact (run `:app:reportDependencyGraphChange`
   or eyeball the AAR size), transitive deps, R8 friendliness.
4. **Exit plan** — how we'd remove it if it goes unmaintained.

Defaults to **rejected** until justified. AndroidX, Jetpack, Compose
ecosystem libs and well-established projects (Coil, Ktor, OkHttp,
Coroutines, Turbine) clear the bar trivially.

## Compose / Kotlin / Compiler alignment

- Compose BOM is the single version source for Compose deps. Don't
  pin individual `androidx.compose.*` versions.
- Kotlin and Compose Compiler must stay in sync via the
  `org.jetbrains.kotlin.plugin.compose` plugin (already in the
  catalogue). Don't override.
- `kotlinx-serialization`, `kotlinx-datetime`, `kotlinx-coroutines` —
  pin via the catalogue and use coroutine BOMs where they exist.

## Per-feature modules (future)

The codebase is intentionally flat today (5 modules). Per-feature
Gradle modules (e.g., `:feature:home`, `:feature:notes`) **may** be
introduced once a feature is genuinely self-contained, mirroring
patterns from Now in Android (`:feature:foo`) or Tivi
(`:ui:foo` + `:data:foo`).

Skeleton (if/when we go there):

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
- **Feature → feature is forbidden.** Cross-feature deps go through
  `:common` or a shared `:core:<name>` module.
- `internal` by default. Expose the umbrella screen composable + Koin
  module + navigation graph extension as `public`.

Not mandatory yet. The first feature to graduate establishes the
template.

## Convention plugins

`buildSrc/` already hosts `ValidateStringPlaceholdersTask`. As more
build logic accumulates, migrate to a Gradle composite build under
`build-logic/convention/` and ship the following plugin aliases:

- `trakt.android.application` — applies `com.android.application` +
  the shared `compileSdk`, `defaultConfig`, `compileOptions`,
  `signingConfigs` boilerplate.
- `trakt.android.library` — `com.android.library` equivalent.
- `trakt.android.library.compose` — extends `trakt.android.library`
  with `org.jetbrains.kotlin.plugin.compose`, `composeOptions`, and
  the stability-config wiring.
- `trakt.android.feature` — extends `trakt.android.library.compose`
  with the typical feature module deps (`:common`, `:resources`, Koin,
  Navigation Compose).
- `trakt.android.lint` — Android Lint config (`xmlReport`,
  `sarifReport`, `checkDependencies = true`, baseline file path).
- `trakt.jvm.library` — pure JVM module (`org.jetbrains.kotlin.jvm`).

After migration, each module's `build.gradle.kts` reads as ~10 lines
applying convention plugins and a `dependencies { … }` block — no
copy-pasted SDK / Compose / Kotlin config blocks. See
[android/nowinandroid `build-logic/convention/`](https://github.com/android/nowinandroid/tree/main/build-logic/convention)
for the canonical layout.

Element X Android's [`plugins/`](https://github.com/element-hq/element-x-android/tree/develop/plugins)
shows an alternative: small plugins delegating to a `extension/`
folder of helpers (`androidConfig`, `composeConfig`, `setupKover`).
Either is fine; pick one and stay consistent.

## Lint

Per-module setup:

- Every Android module owns a `lint-baseline.xml` (committed). The
  baseline lets existing warnings not block new work. **The baseline
  never grows from a PR** — new violations on changed lines must be
  fixed, not baselined.
- A `lint.xml` overlay exists only when a module needs rules that
  differ from the project defaults.
- The `trakt.android.lint` convention plugin sets:
  - `xmlReport = true`
  - `sarifReport = true`
  - `checkDependencies = true`
- CI uploads the SARIF output to GitHub Code Scanning so violations
  appear inline on PR diffs.

## Formatting pipeline

Today: `ktlint` invoked via `./gradlew ktlintFormat` /
`ktlintCheck`, wired into CI through `.github/workflows/master.yml`.

Aspirational: move to **Spotless** so formatting, ktlint, license
headers, and import ordering run in one pass — both NIA and Tivi do
this. Track in `legacy-zones.md`'s migration list. Until migrated,
honour `.editorconfig` rules and run ktlint before pushing.

## OpenAPI generator

Lives at the root `build.gradle.kts`. Three rules:

- **Spec is the source of truth** — edit `openapi/openapi.json`, not the
  generated Kotlin.
- **Re-run after editing the spec**: `./gradlew openApiGenerate`.
- **Commit the generated diff.** Don't expect downstream contributors
  to regenerate locally.

## Plugin / version pins of note

- `kotlin.code.style=official` set in `gradle.properties`.
- `android.useAndroidX=true`, `android.nonTransitiveRClass=true`.
- `org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8`.
- KSP over KAPT — new annotation-processed code should use KSP.

## Quick checklist

- [ ] New dep added to `gradle/libs.versions.toml` first
- [ ] Module dependency direction respected
- [ ] No literal coordinates in `build.gradle.kts`
- [ ] PR body justifies new external deps
- [ ] OpenAPI spec edit re-runs the generator and commits the diff
- [ ] No `Activity` / `Context` references in `:common`
