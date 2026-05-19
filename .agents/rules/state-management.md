---
trigger: glob
globs: '{app,tv,common}/src/main/**/*.kt'
description: 'StateFlow / @Observable usage, ViewModel patterns, remember / rememberSaveable, Compose stability, no-mutableStateOf-in-VM.'
applyTo: '{app,tv,common}/src/main/**/*.kt'
---

# State Management

## What lives where

| Lifetime / scope                                     | Tool                                                   |
| ---------------------------------------------------- | ------------------------------------------------------ |
| Survives config change, drives screen UI             | `StateFlow<UiState>` on a `ViewModel`                  |
| Composable-local UI state, dies on dispose           | `var x by remember { mutableStateOf(value) }`          |
| Composable-local UI state, survives config change    | `var x by rememberSaveable { mutableStateOf(value) }`  |
| App-wide ambient UI state                            | `staticCompositionLocalOf` + `CompositionLocalProvider`|
| Derived value reactive to other state                | `val x by remember(...) { derivedStateOf { … } }`      |
| Side effects keyed to recomposition                  | `LaunchedEffect(key)`, `DisposableEffect(key)`         |

## Sealed UiState

Every screen has a sealed UI state hierarchy:

```kotlin
sealed interface MovieSummaryUiState {
    data object Loading : MovieSummaryUiState
    data class Loaded(val movie: Movie) : MovieSummaryUiState
    data class Error(val throwable: Throwable) : MovieSummaryUiState
}
```

- **No `data class … (val movie: Movie?, val isLoading: Boolean, val error: Throwable?)`**
  flat shapes. Use sealed types so impossible combinations are
  impossible.
- `@Immutable` annotation on `Loaded`-style data classes that hold
  collections, so Compose can skip recomposition when references are
  stable.
- For collections held in state, prefer `ImmutableList`
  (`kotlinx.collections.immutable`) over `List<T>` — Compose treats
  immutable types as stable.

## ViewModel

One `state: StateFlow<UiState>` per ViewModel. Build it from upstream
Flows and expose via `stateIn(viewModelScope, WhileSubscribed(5_000),
initial)`. Internal mutable signals are private:

```kotlin
class CalendarViewModel(
    private val repo: CalendarRepository,
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val state: StateFlow<CalendarUiState> =
        refreshTrigger
            .flatMapLatest { repo.upcoming() }
            .map { CalendarUiState.Loaded(it) as CalendarUiState }
            .catch { emit(CalendarUiState.Error(it)) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                CalendarUiState.Loading,
            )

    fun refresh() { refreshTrigger.tryEmit(Unit) }
}
```

Rules:

- **Don't put `mutableStateOf` in a ViewModel.** Use `StateFlow` /
  `MutableStateFlow`. `mutableStateOf` is Compose-coupled and breaks
  the ability to unit-test the ViewModel without Compose runtime.
- **Don't combine three separate `StateFlow`s in the composable.**
  Combine them once in the ViewModel and expose a single state.
- **Don't expose `MutableStateFlow` publicly.** Expose `StateFlow`;
  mutate from inside the ViewModel.
- Use `update { … }` on `MutableStateFlow` for atomic updates rather
  than `value = value.copy(…)`.

## One-shot events

For navigation effects, snackbars, toasts:

```kotlin
private val _events = MutableSharedFlow<MovieEvent>(replay = 0)
val events: SharedFlow<MovieEvent> = _events.asSharedFlow()

@Composable
fun MovieSummaryScreen(viewModel: MovieSummaryViewModel = koinViewModel()) {
    val snackbar = LocalSnackbarState.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MovieEvent.WatchlistAdded -> snackbar.show(L10n.movieAddedToWatchlist)
                is MovieEvent.Error          -> snackbar.show(event.message)
            }
        }
    }
    // …
}
```

- Use `SharedFlow(replay = 0)` for one-shot events.
- Collect events in a `LaunchedEffect`.
- Don't fire events from inside composition (`body { }` directly).

## Composable-local state

`remember { mutableStateOf(...) }` is for state that:

- Is owned by a single composable instance.
- Doesn't need to survive config change.
- Doesn't need to be tested without Compose runtime.

Examples that qualify: sheet visibility, scroll position, dropdown
expanded state, text field input that is committed elsewhere on
"Done", focus management.

Use `rememberSaveable` when the state should survive a config change
but not the screen leaving the back stack.

## CompositionLocal

App-wide ambient UI state already wired:

- `LocalBottomBarVisibility` — driven by feature flags.
- `LocalSnackbarState` — global snackbar host accessor.
- `LocalCheckInVisibility`, `LocalRatePromptVisibility` — overlay
  visibility.

Add new locals **only** when:

- The value is genuinely app-level (not a single feature).
- Threading it through every composable's parameter list is impractical.
- The value is provided once at the app root.

Define using `staticCompositionLocalOf` when the value is constant for
the lifetime of the provider; `compositionLocalOf` when it changes
during recomposition.

## Side effects

- `LaunchedEffect(key) { … }` — coroutine tied to composition lifetime.
- `DisposableEffect(key) { … onDispose { … } }` — cleanup on dispose.
- `SideEffect { … }` — synchronous side effect after every successful
  composition. Rare; usually you want `LaunchedEffect`.
- **Never** start a coroutine from inside `body { }` directly.
- **Never** read `mutableStateOf` from outside composition without
  `snapshotFlow`.

## Compose stability

All Trakt domain types in `common/.../model/` are listed in
`compose-stability.conf` at the repo root so the Compose compiler
treats them as stable. The same file declares third-party types that
are stable in practice but not annotated (`kotlinx.datetime.*`,
`kotlin.time.Duration`, `coil3.compose.AsyncImagePainter.State`,
`androidx.paging.compose.LazyPagingItems`). Compose convention plugins
wire the file via `freeCompilerArgs`:

```
-Xstability-configuration-path=$projectDir/compose-stability.conf
```

Rules:

- **`@Immutable`** for data classes used as state where every public
  property is `val` and points at deeply immutable values.
- **`@Stable`** for types whose properties are observable but whose
  equality and hash are stable — rare; reach for it only when
  `@Immutable` would be a lie.
- **`ImmutableList<T>` / `PersistentList<T>`** from
  `kotlinx.collections.immutable` for collections held in state. Plain
  `List<T>` is unstable to Compose.
- **Triage recomposition regressions with compose-compiler metrics.**
  Run `./gradlew :app:assembleDebug -Pcompose.metrics=true
  -Pcompose.reports=true` and commit the report under
  `docs/compose-metrics/<bom-version>/` when investigating a hot
  screen.

## Persistence beyond a ViewModel

For state that must outlive the screen (auth tokens, preferences,
sync watermarks): write to DataStore. The ViewModel reads it through a
repository that exposes a `Flow<Preferences>`.

See `persistence.md` for the data layer rules.
