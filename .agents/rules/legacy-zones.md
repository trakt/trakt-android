---
trigger: glob
globs: '**'
description: 'Out-of-scope subprojects and generated paths, legacy patterns not to extend, architectural alternatives evaluated (Hilt / Circuit / kotlin-inject / Metro / SQLDelight / Room). Advisory across the repo.'
applyTo: '**'
---

# Legacy Zones

The prescriptive rules in this directory describe the **direction** new
code should take. Trakt-android is in good shape relative to typical
mid-life Android codebases (Compose-only, no XML, no Fragments, no
RxJava or LiveData detected), but a few areas remain off-limits or
auto-generated. Edits inside them must still compile and pass tests,
but reviewers should not flag them for failing to match the new-code
rules.

## Out-of-scope paths

| Path                                            | Reason                                           |
| ----------------------------------------------- | ------------------------------------------------ |
| `build/`                                        | Gradle build output                              |
| `.gradle/`, `.kotlin/`, `.idea/`                | Tooling-managed                                  |
| `build/generate-resources/`                     | OpenAPI generator output                         |
| `external/`                                     | Third-party drop-in source                       |
| `**/generated/**`                               | Code-generation outputs                          |
| `resources/src/main/res/values-*/strings.xml`   | Crowdin-managed translation files                |
| `**/*.proto`                                    | Schema source for protobuf — review via spec, not code |

The OpenAPI generator output (`build/generate-resources/...`) is
ignored by ktlint via `.editorconfig` and is **not edited by hand**.
Regenerate via `./gradlew openApiGenerate`.

## Architectural alternatives evaluated and not adopted

Reference projects (Now in Android, Tivi, Element X Android) ship
sound architectures that differ from ours. To prevent drive-by
proposals from AI agents that recently read those repos, the
decisions are explicit:

- **kotlin-inject** (compile-time DI; Tivi and Element X Android) —
  not adopted. We're on Koin's runtime DSL. The compile-time-graph
  ergonomics are tempting but require KSP regeneration on every DI
  change and would force a parallel migration. Revisit if KSP build
  times shrink dramatically.
- **Hilt** — not adopted. The codebase standardised on Koin before
  this rule existed; do not propose porting in new code.
- **Metro** (KSP DI; Element X Android) — too new, single-vendor.
  Same calculus as kotlin-inject but with less track record.
- **Circuit / Molecule** (Compose-as-Presenter; Tivi, Element X
  Android) — not adopted. Contradicts our "no `mutableStateOf` in
  ViewModel" rule and adds a framework dep. Keep MVVM + UDF.
- **Appyx navigation** (Element X Android) — not adopted. Compose
  Navigation 2.9 typed routes meet our needs.
- **SQLDelight / Room** — not adopted. Today the data layer is
  DataStore + in-memory + file caches. Revisit if entity volume forces
  the issue. If we ever adopt one, Room is the more likely choice for
  Android-only and SQLDelight for KMP.

These are recorded calls, not invitations to reopen. New evidence is
welcome via a written architecture proposal, not a drive-by refactor.

## Patterns we accept but do not extend

These are not present in the codebase today (it's Compose-only), but
the rules below codify what would be considered legacy if they appear:

- **XML layouts** (`res/layout/*.xml`) — do not add. New screens are
  `@Composable`.
- **`Fragment` / `AppCompatActivity` extending `Activity`** — do not
  add. `MainActivity` and `TvActivity` are the only Activity classes;
  navigation is Compose-based.
- **`LiveData`** — do not add. Use `StateFlow` / `Flow` instead. If a
  legacy library forces `LiveData`, isolate the conversion at the
  boundary (`liveData.asFlow()`).
- **RxJava / RxKotlin** — do not add. Coroutines + Flow only.
- **KAPT** — new annotation-processed code uses KSP.
- **`SharedPreferences`** direct usage — do not add. Use DataStore.
- **`runBlocking` in production code** — do not add. Tests-only.
- **`GlobalScope`** — do not add. Anywhere.
- **`!!` non-null assertions** in production code — do not add. Use
  `requireNotNull(value) { "…" }` with a message, or guard with `?:`.
- **Mocking libraries** (Mockito, MockK) — do not add for new tests.
  Use hand-written fakes implementing production interfaces (Now in
  Android pattern).
- **Data Binding / View Binding** — forbidden in new code. The
  codebase is Compose-only; the binding generators do not apply.

## Files that are large enough to be hazardous

The Android codebase doesn't currently have a `CommentsViewController`-
sized legacy file. Any future Kotlin file exceeding **~40 KB** is a
smell — it should be split before growing further. New code aims for
≤ ~300 lines per ViewModel and ≤ ~500 lines per composable file.

## CI-managed paths

- `.github/workflows/master.yml` runs ktlint on every push to main.
  Edits to `.editorconfig` propagate to the next CI run; no need to
  change the workflow.
- `.github/workflows/i18n_sync.yml` opens daily Crowdin-sync PRs.
- `.github/workflows/openapi_sync.yml` (if/when present) regenerates
  the API client.

## When reviewing PRs

- A new file outside the in-scope source dirs? Suggest relocating, but
  don't block for style nits.
- A line of new code in a generated file? Flag it as a process issue
  ("this should be regenerated"), not a style nit.
- A new file added under `core/<feature>/` that doesn't follow these
  rules? Flag normally — that's the active surface.

## Migration tracking (informational)

These items are not actively in-flight, but should they become so, here
is where the codebase is heading:

- KSP adoption across all modules (some still rely on plugin defaults).
- Per-feature Gradle modules (currently flat).
- Roborazzi screenshot tests for the design system.
- Detekt as a secondary linter (today only ktlint is wired).
- Compose Multiplatform — not on the roadmap; flagged here so newcomers
  don't speculate.

No timelines, no mandates. The rules describe direction, not a
deadline.
