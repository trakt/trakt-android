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

## ViewModel

One `state: StateFlow<UiState>` per ViewModel. Build from upstream Flows, expose via `stateIn(viewModelScope, WhileSubscribed(5_000), initial)`. Internal mutable signals private:

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

- **No `mutableStateOf` in ViewModel.** Use `StateFlow` / `MutableStateFlow`. `mutableStateOf` Compose-coupled, breaks unit-testing without Compose runtime.
- **Don't combine three separate `StateFlow`s in composable.** Combine once in ViewModel, expose single state.
- **Don't expose `MutableStateFlow` publicly.** Expose `StateFlow`; mutate inside ViewModel.
- Use `update { … }` on `MutableStateFlow` for atomic updates, not `value = value.copy(…)`.

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
- Collect events in `LaunchedEffect`.
- Don't fire events from inside composition (`body { }` directly).

## Composable-local state

`remember { mutableStateOf(...) }` for state that:

- Owned by single composable instance.
- Doesn't survive config change.
- Doesn't need testing without Compose runtime.

Qualifies: sheet visibility, scroll position, dropdown expanded state, text field input committed elsewhere on "Done", focus management.

Use `rememberSaveable` when state survives config change but not screen leaving back stack.

## CompositionLocal

App-wide ambient UI state already wired:

- `LocalBottomBarVisibility` — driven by feature flags.
- `LocalSnackbarState` — global snackbar host accessor.
- `LocalCheckInVisibility`, `LocalRatePromptVisibility` — overlay visibility.

Add new locals **only** when:

- Value genuinely app-level (not single feature).
- Threading through every composable param list impractical.
- Value provided once at app root.

Use `staticCompositionLocalOf` when value constant for provider lifetime; `compositionLocalOf` when it changes during recomposition.

## Side effects

- `LaunchedEffect(key) { … }` — coroutine tied to composition lifetime.
- `DisposableEffect(key) { … onDispose { … } }` — cleanup on dispose.
- `SideEffect { … }` — sync side effect after every successful composition. Rare; usually want `LaunchedEffect`.
- **Never** start coroutine from inside `body { }` directly.
- **Never** read `mutableStateOf` from outside composition without `snapshotFlow`.

## Compose stability

Rules:

- **`@Immutable`** for data classes used as state where every public property is `val` pointing at deeply immutable values.
- **`@Stable`** for types with observable properties but stable equality/hash — rare; only when `@Immutable` would be lie.
- **`ImmutableList<T>` / `PersistentList<T>`** from `kotlinx.collections.immutable` for collections in state. Plain `List<T>` unstable to Compose.
- **Triage recomposition regressions with compose-compiler metrics.** Run `./gradlew :app:assembleDebug -Pcompose.metrics=true -Pcompose.reports=true`, commit report under `docs/compose-metrics/<bom-version>/` when investigating hot screen.

## Persistence beyond a ViewModel

State outliving screen (auth tokens, preferences, sync watermarks): write to DataStore. ViewModel reads through repository exposing `Flow<Preferences>`.

See `persistence.md` for data layer rules.
