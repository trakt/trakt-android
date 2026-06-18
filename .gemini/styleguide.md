# Project Coding Standards

Style guide for AI-assisted review of the trakt-android monorepo
(phone + Android TV, Compose-first, Kotlin 2.x). Mirrors
`.agents/rules/*.md` — those are authoritative; this is the digest
Gemini uses when reviewing pull requests.

## Tech stack

- Kotlin 2.3.x, JVM target 11, Compose BOM `2026.05.00`.
- Min SDK 28, Compile/Target SDK 37, AGP 9.2.x.
- Jetpack Compose for all UI. No XML, no Fragments, no `LiveData`,
  no RxJava.
- Coroutines + Flow. `StateFlow` for screen state; `SharedFlow` for
  one-shot events.
- **DI: Koin 4.2.x** with module DSL. No Hilt, no Dagger.
- Networking: Ktor 3.4.x + OpenAPI-generated client + Moshi for
  generated DTOs + kotlinx.serialization for typed routes / protobuf.
- Persistence: DataStore (Preferences / typed Proto). No
  SharedPreferences in new code, no Realm, no SQLDelight.
- Images: Coil 3.x.
- Modules: `:app`, `:tv`, `:common`, `:resources`, `:buildSrc`.

## Review scope

- Apply rules to **new files** and **substantially rewritten files**.
- Skip nits inside legacy/generated zones: `build/`, `external/`, generated
  OpenAPI sources, `values-*/strings.xml` translation files.
- Don't flag the same pattern repeatedly across legacy files — once is
  enough.

## Naming conventions

- **PascalCase** — types, Composables, file names matching the primary
  type.
- **camelCase** — properties, functions, locals.
- **SCREAMING_SNAKE_CASE** — top-level / companion `const val` only.
- **Acronyms** — official Kotlin style: treat as words. `Url` / `Id`
  / `Json` for type names; `url`, `id`, `json` for properties.
- **Package names** — lowercase, no underscores.
- **File naming**:
  - Composable screens → `*Screen.kt`
  - ViewModels → `*ViewModel.kt`
  - Sealed UI state → `*State.kt`
  - Koin modules → `*Module.kt` under `di/`
  - Mappers → `*Mapper.kt` or `mapTo*.kt`
  - Tests → `*Test.kt` (JUnit) or matching class name + `Test` suffix

## Commit standards

Conventional Commits with android-scoped enum. See
`.agents/rules/commits.md` for full enum. Allowed scopes: `app`, `tv`,
`common`, `resources`, `openapi`, `widget`, `i18n`, `ci`, `deps`,
`gradle`, `agents`, `gemini`.

```
feat(app): add notes drawer
fix(common): handle 204 from credits endpoint
chore(i18n): translations updates from CrowdIn
chore(deps): bump compose BOM
```

Subject lowercase, no trailing period, ≤ 72 chars. PR titles follow
the same format. No em-dashes anywhere in commits / PR text — use
plain hyphens.

## Code principles

- **Pure functions** for data transformation. Side effects (network,
  disk, Firebase, analytics) live at the edges — repositories,
  use-cases, ViewModel collectors. Never inside mappers or composables.
- **Immutability**: prefer `val`. `data class`es for models.
  `ImmutableList` / `PersistentList` for collections held in state.
- **Early exits** via guard `if (x == null) return`. No nested `let { }`
  pyramids.
- **No `!!`** in production code. Use `requireNotNull(value) { … }` or
  `?:`. `!!` is acceptable in tests and previews.
- **DI via constructor.** Koin `module { }` blocks wire dependencies.
  No `KoinJavaComponent.getKoin().get<T>()` inside type bodies.
- **Functions with 3+ params take a single `data class` param.**
- **Single responsibility** per function and per type. ViewModels >
  300 lines are smells.

## Architecture

- **MVVM + UDF.** ViewModel exposes `StateFlow<UiState>` built via
  `stateIn(viewModelScope, WhileSubscribed(5_000), Initial)`.
- **Sealed `UiState`** — `Loading` / `Loaded(...)` / `Error(...)`.
  Not flat `data class` with three nullable fields.
- **Stateful `Screen` + stateless `Content`** split. Previews and
  snapshot tests target the `Content` variant.
- **Compose Navigation 2.9+** with `@Serializable` typed routes. No
  string-route DSL for new screens.
- **Repository** is the public data boundary: `Flow<T>` for reads,
  `suspend fun` returning `Result<…>` for writes. Offline-first where
  possible.
- **UseCases optional** — only when combining multiple repositories or
  encapsulating shared business logic.

## State management

- **`StateFlow`** for screen state. No `mutableStateOf` in ViewModels
  (Compose-coupled, hard to test).
- **`remember { mutableStateOf(...) }`** for transient UI that dies
  with the composable.
- **`rememberSaveable`** for transient state that survives config
  change.
- **Don't expose `MutableStateFlow` publicly.** Mutate from inside the
  ViewModel; expose `StateFlow` only.
- **One-shot events** via `SharedFlow(replay = 0)` collected in
  `LaunchedEffect`. Don't fire events from inside `body { }`.
- **`update { … }`** on `MutableStateFlow` for atomic mutations.
- **`@Immutable`** on data classes held in state.

## Compose

- **`when` over sealed `UiState` is exhaustive.** No `else ->` branches
  swallowing new cases. Recent bug across stacks
  (`keep tag(isLatestAired:provider:) switch exhaustive`) shows the
  cost.
- **`Modifier` is the second parameter** with default
  `modifier: Modifier = Modifier`.
- **Slot APIs use `@Composable` lambda parameters** —
  `content: @Composable () -> Unit`.
- **Side effects** in `LaunchedEffect(key)` / `DisposableEffect(key)`.
  Never start a coroutine from `body { }` directly.
- **State hoisting**: state in, events out as lambdas.

## Networking

- **OpenAPI client is generated.** Never edited by hand.
- **Mappers are pure**, named `…ToDomain` / `mapTo<Domain>`. Live in
  `common/.../<entity>/` or `common/.../networking/mappers/`.
- **Status 204 is success.** Mappers handle 204 by returning
  empty/default values.
- **Typed errors** at the boundary via an `ApiError` sealed type.
  Raw `IOException` / `HttpException` don't escape into ViewModels.
- **Repositories** expose `Flow<…>` reads + `suspend` writes. No
  `Activity` / `Context` references in repositories.

## Persistence

- **DataStore** for new preferences. **No** `SharedPreferences` direct
  usage, **no** Realm, **no** SQLDelight in new code.
- Keys live in a `companion object` next to the store.
- Reads return `Flow<T>`; writes are `suspend`.
- Auth tokens go through `AuthStorage`, never DataStore directly.

## Theming

- All colours via `TraktTheme.colors.*`. **Forbid raw `Color(0xFF…)` /
  `Color(red = …)`** and `MaterialTheme.colorScheme.*` direct reads
  in feature code.
- All spacing via `TraktTheme.spacing.*` when available. Allow `Modifier.padding(16.dp)`
  for misc cases.
- All sizes via `TraktTheme.size.*`.
- All typography via `TraktTheme.typography.*`. No inline `TextStyle`
  literals.
- Images via Coil 3 + `R.drawable.*`. **No Glide / Fresco / Picasso.**
- Animations respect reduced-motion preferences.

## Localization

- All user-facing strings live in `resources/.../values/strings.xml`.
  Translation `values-<locale>/` files are Crowdin-managed.
- **`L10n` equivalent** is the generated `R.string.*` reference; do
  not hand-edit `values-*` translation files.
- **No inline raw string literals** for user-facing text in
  composables (debug logs are not localised).
- Keys are namespaced: `action_*`, `common_*`, `a11y_*`,
  `screen_<name>_*`, `error_*`. Add `_tv` suffix for TV-only variants.
- Use `<plurals>` + `pluralStringResource` for counts. No `+`
  concatenation.

## Testing

- **Unit tests** in `*/src/test/`, Android tests in `*/src/androidTest/`.
- **Turbine** for Flow assertions, **Truth** for general assertions,
  **`runTest`** for coroutine virtual time.
- **No mocking libraries** for new tests (Mockito / MockK). Use
  hand-written fakes implementing production interfaces.
- **Mandatory unit coverage**: mappers, ViewModels (state emissions),
  repositories, pure helpers, use-cases.
- **Compose UI tests** with `ComponentActivity` (not `MainActivity`)
  for component-level tests.
- Every new `<Feature>Content` composable has at least one `@Preview`
  covering `.Loaded`. Add `.Loading` / `.Error` previews when
  applicable.
- Roborazzi screenshot tests recommended for the future; baselines
  generated by CI, not workstation.

## Packages / Gradle

- Module direction: `app → tv/common → common → resources`. Never
  reverse.
- **New deps added to `gradle/libs.versions.toml` first.** No literal
  coordinates in `build.gradle.kts`.
- Plugin aliases via the catalogue.
- New external dep requires PR-body justification (need, maintenance
  posture, footprint, exit plan).
- KSP > KAPT for new annotation processing.

## Anti-patterns to flag in review

Severity-prioritised list:

**High (block-worthy):**

- New XML layouts, Fragments, or `AppCompatActivity` subclasses.
- DataBinding / ViewBinding in new code (Compose-only project).
- `LiveData`, `RxJava`, `Observable` in new code.
- `!!` non-null assertions in production code.
- `runBlocking` / `GlobalScope` outside tests.
- `KAPT` for newly introduced annotation processing — KSP only.
- New `SharedPreferences` direct usage.
- Raw `Color(0xFF…)` / `Color(red = …)` / `MaterialTheme.colorScheme.*`
  in feature code.
- Inline `TextStyle(fontSize = 14.sp)` literals.
- `Image("string-literal")` for design-system images.
- Hand-edits to `values-<locale>/strings.xml` translation files.
- Hand-edits to generated OpenAPI sources.
- `KoinJavaComponent.getKoin().get<T>()` inside type bodies.
- Mocking libraries (Mockito, MockK) in new tests.
- `mutableStateOf` inside a ViewModel.
- Exposing `MutableStateFlow` publicly.
- Repository emitting remote responses without writing to local first
  (breaks the offline-first invariant).
- Composable / mapper that takes a `Context` purely to resolve a
  string — return `@StringRes Int?` instead.
- `Timber` calls that interpolate PII (emails, tokens, OAuth codes,
  request bodies) into the message.

**Medium:**

- Inline `Spacer(Modifier.height(8.dp))` instead of arrangement
  spacing.
- Implicit (default) Compose Material colours when a Trakt token
  exists.
- ViewModels > 300 lines.
- Functions with 4+ positional parameters.
- `default:` / `else ->` branches on sealed domain enums.
- `Task { … }`/`launch { … }` inside composable `body { }` (use
  `LaunchedEffect`).

**Low (advisory):**

- `Modifier.padding(16.dp)` magic numbers.
- `Modifier.size(64.dp)` magic numbers.
- File naming that doesn't match the primary type.
- `forEach` used for transformation (use `map`).

## Review tone

Match the user's GitHub voice (per their global preferences):

- Direct, friendly, no preamble.
- Plain hyphens — **no em-dashes / en-dashes anywhere**.
- Short paragraphs. Contractions OK.
- Use `Adopted - <what changed>.` and `Leaving as-is - <reason>.`
  patterns when responding to PR author replies.
- No "Generated with Claude Code" footers. No
  `Co-Authored-By: Claude` trailers unless the human asks.
- Critique is welcome; flattery is not (`This looks great!` is not a
  review comment).
