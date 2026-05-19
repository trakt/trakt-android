---
trigger: glob
globs: '**/*.kt'
description: 'Naming, file layout, type safety, async / coroutines, navigation, error handling, search-first reuse for new Kotlin code.'
applyTo: '**/*.kt'
---

# Implementation Guidelines

## Before Writing Code

- **Search existing patterns first.** The codebase already has feature
  folders, Koin modules, and `TraktTheme` tokens — look for a working
  example before inventing.
- Check whether a new helper already exists in `common/.../helpers/`,
  `common/.../ui/`, or another feature.
- Prefer referencing real files as examples over abstract descriptions.

## When Establishing New Patterns

- When a pattern diverges from or extends existing conventions, note
  it so the rules can be updated.
- A helper used in 2+ places earns a home in `common/.../helpers/`
  (logic) or `common/.../ui/` (Compose).
- If you refactor shared logic, leave a one-line note in the PR body
  pointing at the new home.

## Naming Conventions

- **PascalCase** — types, top-level Composables, file names matching the
  primary type.
- **camelCase** — properties, functions, local variables, lambda
  parameters.
- **SCREAMING_SNAKE_CASE** — top-level / companion constants only
  (`const val MAX_RETRIES = 3`).
- **Acronyms** — follow the official Kotlin style guide: treat acronyms
  as words. `Url` / `Id` / `Json` for type names; `url`, `id`, `json`
  for properties. ktlint's `function-naming` would flag the all-caps
  variant.

### File Naming

| Kind                                     | Example file                            |
| ---------------------------------------- | --------------------------------------- |
| Top-level Composable screen              | `MovieSummaryScreen.kt`                 |
| Other Composable view                    | `MovieSummaryContent.kt` / `MovieCard.kt` |
| State data / sealed class for a screen   | `MovieSummaryState.kt`                  |
| ViewModel                                | `MovieSummaryViewModel.kt`              |
| Koin module                              | `MovieSummaryModule.kt` (in `di/`)      |
| Use-case                                 | `GetMovieSummaryUseCase.kt`             |
| Repository                               | `MovieRepository.kt`                    |
| Mapper                                   | `MovieMapper.kt` or `mapToMovie.kt`     |
| Domain model                             | `Movie.kt`                              |
| Test                                     | `MovieSummaryViewModelTest.kt`          |

### Package Naming

- Lower-case, no underscores: `tv.trakt.trakt.core.search`,
  `tv.trakt.trakt.common.networking`. Match the file path.

### Folder Layout per Feature

```
core/<feature>/
├── <Feature>Screen.kt
├── <Feature>State.kt
├── <Feature>ViewModel.kt
├── components/        # composables specific to this feature
├── data/              # local stores, mappers, DTO→domain
├── domain/            # use cases, domain models (if needed)
└── di/<Feature>Module.kt
```

## Web/iOS ↔ Android Idiom Map

For contributors moving between the trakt-web (Svelte) / trakt-apple
(SwiftUI) stacks and Android:

| Web / iOS                        | Android / Compose                                       |
| -------------------------------- | ------------------------------------------------------- |
| Zod schema / `Codable`           | `@Serializable` data class (kotlinx.serialization)      |
| `$state(value)` / `@State`       | `var x by remember { mutableStateOf(value) }`           |
| `$derived(expr)` / `var x: T`    | `val x by remember(...) { derivedStateOf { expr } }`    |
| `useFoo()` hook / `FooStore`     | `FooViewModel` + `koinViewModel()`                      |
| `goto(url, { replaceState })`    | `navController.navigate(route) { popUpTo(...) }`        |
| `RenderFor audience="member"`    | `TraktGate(audience = Audience.Member) { … }`           |
| `time.hours(3)` / `TraktTime`    | `kotlin.time.Duration.Companion.hours(3)`               |
| `defineQuery(...)`               | Repository function returning `Flow<…>`                 |
| `goto` with typed params         | Compose Navigation typed routes via `@Serializable`     |

## Type Safety

- No `Any` types in new code unless interop demands it. Use generics or
  sealed hierarchies.
- Prefer non-nullable types. Reach for `T?` only when the absence is
  semantically meaningful.
- Optional chaining: `?.`, `?:`, `let`, `also`, `apply`, `run`.
- No `lateinit var` for properties that have a sensible default value;
  use `var x: T = default` or pull state into a constructor parameter.
- `Result<T>` (`kotlin.Result` or a domain-specific sealed type) at
  cross-module boundaries when failure is part of the contract.

## Async / Coroutines

- `suspend` functions for one-shot async work.
- `Flow` for cold streams; `StateFlow` for hot, observable state with
  a current value; `SharedFlow` for one-shot events with replay.
- Launch work inside `viewModelScope` (ViewModels) or `lifecycleScope`
  (composition holders) — never `GlobalScope`.
- Side effects in composables use `LaunchedEffect(key)` /
  `DisposableEffect(key)` — never start a coroutine from `body { }`
  directly.
- Cancellation: respect it. Use `withTimeout`, `ensureActive`. Don't
  `try { … } catch (e: Exception) { … }` over the whole body and
  swallow `CancellationException`.

## State Hoisting

- ViewModel owns the state. Composables read it via
  `collectAsStateWithLifecycle()`.
- `remember { mutableStateOf(...) }` is acceptable for **transient UI**
  that doesn't survive ViewModel rebuild (sheet visibility tied to a
  single screen, scroll positions, focus). Anything that should survive
  a config change belongs in the ViewModel.
- `rememberSaveable` for transient state that should survive config
  change but not destruction (text field input, expanded card state).

## Navigation

- Compose Navigation 2.9+ with typed routes via
  `@Serializable` route classes.
- One `NavHost` in `app/` and one in `tv/`. Feature graphs are
  exposed via `NavGraphBuilder.<feature>Graph(...)` extension functions
  declared in the feature module.
- Pass typed arguments through the serializable route, not through
  global state.

```kotlin
@Serializable
data class MovieSummaryRoute(val id: Long)

navController.navigate(MovieSummaryRoute(id = 42))

composable<MovieSummaryRoute> { backStackEntry ->
    val args = backStackEntry.toRoute<MovieSummaryRoute>()
    MovieSummaryScreen(id = args.id)
}
```

## Error Handling

- Predictable failures bubble as `Result<T>` / typed sealed errors.
- UI surfaces errors through the `UiState.Error(message)` variant with
  copy from a localised `error_*` string.
- Unrecoverable invariants: `error(...)`, `check(...)`, `require(...)`
  with a clear message. Never `throw RuntimeException("…")` for
  user-input validation.

## Search-First Reuse

Before authoring:

- A new mapper — search `mapTo*` and `*Mapper` for an existing one.
- A new colour or spacing token — check `TraktTheme.colors` and
  `TraktTheme.spacing` first.
- A new HTTP call — check whether the OpenAPI client already exposes
  the endpoint, or whether `KtorClientFactory` already wraps it.
- A new date / number formatter — check
  `common/.../helpers/formatting/`.
