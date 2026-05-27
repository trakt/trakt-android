---
trigger: glob
globs: '**/*.kt'
description: 'Naming, file layout, type safety, async / coroutines, navigation, error handling, search-first reuse for new Kotlin code.'
applyTo: '**/*.kt'
---

# Implementation Guidelines

## Before Writing Code

- **Search existing patterns first.** Codebase has feature folders, Koin modules, `TraktTheme` tokens — find working example before inventing.
- Check if new helper already exists in `common/.../helpers/`, `common/.../ui/`, or another feature.
- Prefer real files as examples over abstract descriptions.

## When Establishing New Patterns

- Pattern diverges from conventions → note it so rules can update.
- Helper used in 2+ places → home in `common/.../helpers/` (logic) or `common/.../ui/` (Compose).
- Refactor shared logic → leave one-line note in PR body pointing at new home.

## Naming Conventions

- **PascalCase** — types, top-level Composables, file names matching primary type.
- **camelCase** — properties, functions, local variables, lambda parameters.
- **SCREAMING_SNAKE_CASE** — top-level / companion constants only (`const val MAX_RETRIES = 3`).
- **Acronyms** — follow Kotlin style guide: treat as words. `Url` / `Id` / `Json` for type names; `url`, `id`, `json` for properties. ktlint's `function-naming` flags all-caps variant.

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

- Lower-case, no underscores: `tv.trakt.trakt.core.search`, `tv.trakt.trakt.common.networking`. Match file path.

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

## Type Safety

- No `Any` types in new code unless interop demands. Use generics or sealed hierarchies.
- Prefer non-nullable. Use `T?` only when absence is semantically meaningful.
- Optional chaining: `?.`, `?:`, `let`, `also`, `apply`, `run`.
- No `lateinit var` when sensible default exists; use `var x: T = default` or pull state into constructor param.
- `Result<T>` (`kotlin.Result` or domain-specific sealed type) at cross-module boundaries when failure is part of contract.

## Async / Coroutines

- `suspend` for one-shot async work.
- `Flow` for cold streams; `StateFlow` for hot observable state with current value; `SharedFlow` for one-shot events with replay.
- Launch work inside `viewModelScope` (ViewModels) or `lifecycleScope` (composition holders) — never `GlobalScope`.
- Side effects in composables: `LaunchedEffect(key)` / `DisposableEffect(key)` — never start coroutine from `body { }` directly.
- Cancellation: respect it. Use `withTimeout`, `ensureActive`. Don't `try { … } catch (e: Exception) { … }` over whole body and swallow `CancellationException`.

## State Hoisting

- ViewModel owns state. Composables read via `collectAsStateWithLifecycle()`.
- `remember { mutableStateOf(...) }` OK for **transient UI** not surviving ViewModel rebuild (sheet visibility, scroll positions, focus). Config-change survivors belong in ViewModel.
- `rememberSaveable` for transient state surviving config change but not destruction (text field input, expanded card state).

## Navigation

- Compose Navigation 2.9+ with typed routes via `@Serializable` route classes.
- One `NavHost` in `app/` and one in `tv/`. Feature graphs exposed via `NavGraphBuilder.<feature>Graph(...)` extension functions in feature module.
- Pass typed args through serializable route, not global state.

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
- UI surfaces errors via `UiState.Error(message)` with copy from localised `error_*` string.
- Unrecoverable invariants: `error(...)`, `check(...)`, `require(...)` with clear message. Never `throw RuntimeException("…")` for user-input validation.

## Search-First Reuse

Before authoring:

- New mapper — search `mapTo*` and `*Mapper` for existing one.
- New colour or spacing token — check `TraktTheme.colors` and `TraktTheme.spacing` first.
- New HTTP call — check if OpenAPI client exposes endpoint, or if `KtorClientFactory` wraps it.
- New date / number formatter — check `common/.../helpers/formatting/`.