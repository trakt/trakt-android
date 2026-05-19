---
trigger: glob
globs: '**/*.kt'
description: 'MVVM + UDF, Compose-only UI, ViewModel + StateFlow, sealed UiState, Compose Navigation typed routes, repositories. Background sync, adaptive layouts.'
applyTo: '**/*.kt'
---

# Architecture

> **TL;DR**: MVVM + Unidirectional Data Flow. Compose-only UI. ViewModels
> expose `StateFlow<UiState>`. Repositories expose `Flow<…>` for reads
> and `suspend` for writes. DI via Koin. Navigation via Compose
> Navigation 2.9+ with `@Serializable` typed routes. Inspired by Now in
> Android (Google) and Tivi (Chris Banes); selectively borrows.

## Invariants

These hold across every layer; rule files below elaborate. Lifted from
Now in Android's `ArchitectureLearningJourney.md`:

- **Higher layers react to lower layers.** UI reacts to domain reacts
  to data. Never the other way around.
- **Events flow down, data flows up.** User intents propagate down as
  function calls / event sinks; state propagates up as `Flow`s and
  `StateFlow`s.
- **Local storage is the single source of truth.** Repositories write
  remote responses to local storage **before** emitting to callers;
  callers always observe the local stream. Remote-only emission paths
  break offline-first.
- **Repositories own their domain models.** Mappers translate at the
  network boundary; no DTO leaks past a repository, and no domain model
  leaks across feature boundaries — features depend on `:common`
  models, not on each other.

## Layers

```
┌──────────────────────────────────────────┐
│ UI                                       │  Compose Screens + ViewModels
│   - @Composable Screens                  │  StateFlow<UiState>
│   - ViewModels                           │
├──────────────────────────────────────────┤
│ Domain (optional, thin)                  │  UseCases that combine Flows
│   - UseCase classes                      │
├──────────────────────────────────────────┤
│ Data                                     │  Repositories, local stores
│   - Repository                           │  Flow<T> reads, suspend writes
│   - Local (DataStore, entity caches)     │
│   - Remote (Ktor + OpenAPI client)       │
└──────────────────────────────────────────┘
```

- Add a `UseCase` **only when** a ViewModel needs to combine multiple
  repositories or apply non-trivial business logic that is reused. Do
  not write a `UseCase` that just forwards to a single repository
  method — call the repository directly.
- Repositories own the **offline-first** behaviour: read from local
  first, refresh from remote in the background, emit updates through
  the same Flow.

## Composables

Composables are pure functions of their parameters. State flows in
through the parameter list; events flow out through lambdas.

```kotlin
@Composable
fun MovieSummaryScreen(
    id: Long,
    viewModel: MovieSummaryViewModel = koinViewModel { parametersOf(id) },
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MovieSummaryContent(
        state = state,
        onRetry = viewModel::reload,
        onBack = onBack,
    )
}

@Composable
private fun MovieSummaryContent(
    state: MovieSummaryUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    when (state) {
        MovieSummaryUiState.Loading       -> LoadingPlaceholder()
        is MovieSummaryUiState.Loaded     -> MovieSummaryBody(state.movie, onBack)
        is MovieSummaryUiState.Error      -> ErrorState(error = state.error, onRetry = onRetry)
    }
}
```

- **Split `<Feature>Screen` (stateful) from `<Feature>Content` (stateless).**
  `Content` takes the state and event lambdas only — easy to preview
  and snapshot-test.
- **No `@Preview` on the stateful screen.** Preview the `Content`
  variant.
- **`when` over sealed `UiState` is exhaustive.** Add a case to the
  sealed type instead of an `else ->` branch.

## ViewModels

ViewModels expose a single `state: StateFlow<UiState>` constructed via
`stateIn(viewModelScope, WhileSubscribed(5_000), Loading)`. Internal
mutable flows feed the public one.

```kotlin
class MovieSummaryViewModel(
    private val id: Long,
    private val repository: MovieRepository,
) : ViewModel() {

    val state: StateFlow<MovieSummaryUiState> =
        repository
            .movie(id)
            .map { MovieSummaryUiState.Loaded(it) as MovieSummaryUiState }
            .catch { emit(MovieSummaryUiState.Error(it)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MovieSummaryUiState.Loading,
            )

    fun reload() { /* mutate an internal flow that the upstream observes */ }
}
```

Rules:

- **One `StateFlow<UiState>` per ViewModel.** Not three separate
  `StateFlow`s combined inside the view.
- ViewModels do not call composables, do not hold Android `Context`,
  do not depend on `View`/`Composer` types.
- One-shot events (snackbars, navigation effects) flow through a
  `SharedFlow<UiEvent>` with `replay = 0`, collected from
  `LaunchedEffect`.

## Domain Layer (Optional)

Use-cases live in `core/<feature>/domain/` only when they earn their
keep:

- They combine 2+ repositories.
- They encapsulate non-trivial business logic (rate-limit-aware
  refreshes, complex ranking).
- They are shared by multiple ViewModels.

Avoid one-line `UseCase` wrappers around repository calls.

## Data Layer

- **Repository** is the public boundary. Located in `core/<feature>/data/`
  or in `common/.../<entity>/`.
- **Reads return `Flow<T>`.** Local + remote sources flow through
  the repository unified by the `data` layer; the ViewModel sees only
  the unified stream.
- **Writes are `suspend fun` returning `Result<…>`** (or a domain-
  specific sealed result type).
- **Mappers** are pure functions named `mapTo<Domain>(...)` or
  `<Source>Mapper.kt`. Live next to the repository. No I/O.

## Navigation

Single-activity, single `NavHost` in `app/`, mirror in `tv/`. Typed
routes via `@Serializable` data classes (Compose Navigation 2.9+):

```kotlin
@Serializable
data object HomeRoute

@Serializable
data class MovieSummaryRoute(val id: Long)

NavHost(navController, startDestination = HomeRoute) {
    composable<HomeRoute> { HomeScreen(...) }
    composable<MovieSummaryRoute> { backStackEntry ->
        val route: MovieSummaryRoute = backStackEntry.toRoute()
        MovieSummaryScreen(id = route.id)
    }
}
```

- **No string routes.** Typed routes only for new screens.
- **No global navigator singleton.** Pass `navController` callbacks
  down as lambdas (`onMovieClick: (Long) -> Unit`) so feature
  composables stay framework-agnostic.
- **Feature-local `NavGraphBuilder` extensions** expose a feature's
  destinations:

```kotlin
fun NavGraphBuilder.movieFeature(onBack: () -> Unit) {
    composable<MovieSummaryRoute> { … }
    composable<MovieCastRoute> { … }
}
```

## Dependency Injection (Koin)

- Constructor injection at every layer.
- One Koin module per feature, named `<Feature>Module.kt`, located in
  `core/<feature>/di/`.
- Wire all feature modules in `TraktApplication.setupKoin()`.

```kotlin
internal val movieModule = module {
    single<MovieRepository> { MovieRepositoryImpl(client = get(), local = get()) }
    factory { GetMovieSummaryUseCase(repository = get()) }
    viewModel { (id: Long) -> MovieSummaryViewModel(id = id, repository = get()) }
}
```

Scopes:

- **`single { }`** — repositories, clients, caches, stateful collaborators.
- **`factory { }`** — use-cases, mappers, anything stateless and cheap.
- **`viewModel { }`** — every ViewModel.

## Background sync

Periodic refreshes (calendar, up-next, watchlist deltas, scrobble
flush) run as `CoroutineWorker` instances inside a `sync/` package
today, or a dedicated `:sync` module once the pattern grows.

```kotlin
class CalendarSyncWorker(
    appContext: Context,
    params: WorkerParameters,
    private val calendar: CalendarRepository,
    private val watchlist: WatchlistRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val refreshes = listOf(
            async { calendar.refresh() },
            async { watchlist.refresh() },
        )
        runCatching { refreshes.awaitAll() }
            .fold(onSuccess = { Result.success() }, onFailure = { Result.retry() })
    }
}
```

Rules:

- Workers receive repositories via Koin (`workerOf<>(...)`).
- Refresh tasks fan out via `async` + `awaitAll`. Single failures don't
  cancel siblings unless intended.
- Return `Result.retry()` on failure so WorkManager applies its
  exponential backoff. Don't write a custom retry loop inside the
  worker.
- Use `OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST` when
  expediting user-triggered syncs.
- Provide `ForegroundInfo` only when the user is actively waiting
  (manual pull-to-refresh chained through WorkManager).
- Sync entry points (which Workers to enqueue when) live in
  `common/.../sync/` or `app/.../sync/`. Composables / ViewModels do
  not enqueue work directly — they call a `SyncTrigger` collaborator.

## Adaptive layouts

Phone + tablet + Android TV — three form factors share most code.
Lean on Material 3 Adaptive (`androidx.compose.material3.adaptive`)
and `WindowSizeClass`:

```kotlin
@Composable
fun MovieSummaryContent(
    state: MovieSummaryUiState,
    windowSizeClass: WindowSizeClass,
    /* ... */
) {
    val useTwoPane = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
    if (useTwoPane) TwoPaneLayout(state) else SinglePaneLayout(state)
}
```

Rules:

- **Pass `WindowSizeClass` as a parameter** to feature `Content`
  composables that need it. Compute it once at the app root via
  `calculateWindowSizeClass(activity)`.
- **Never** branch on `Resources.configuration.smallestScreenWidthDp`
  inside a composable or `if (isTablet)` from helpers.
- TV-specific composables live in the `:tv` module; phone variants in
  `:app`. They share state types and ViewModels via `:common`.
- Use `TraktTheme.spacing` / `TraktTheme.size` tokens — they vary by
  window class internally.

## Single-Activity, Phone vs TV

- `MainActivity` (`app/`) hosts the phone NavHost.
- `TvActivity` (`tv/`) hosts the TV NavHost.
- Routing between them at startup: `MainActivity` detects
  `isTelevision()` and forwards to `TvSplashActivity`.
- **TV reuses the same ViewModels** where the underlying state is
  identical; only the Compose tree differs.

## Compose-as-Function

- Composables take **parameters in, render output, expose events as
  lambdas.** No object identity, no mutable side state outside
  `remember`.
- `Modifier` is always the second parameter and has a default
  (`modifier: Modifier = Modifier`).
- Slot APIs use `@Composable` lambda parameters
  (`content: @Composable () -> Unit`).

## Composition Locals

Existing pattern: `LocalBottomBarVisibility`, `LocalSnackbarState`,
`LocalCheckInVisibility`, `LocalRatePromptVisibility`. Use sparingly
for **ambient app-level UI state** that doesn't fit a feature
ViewModel.

Guidelines:

- Define `staticCompositionLocalOf` for values that don't change
  during a recomposition.
- Provide via `CompositionLocalProvider` at the app root.
- Don't reach for `CompositionLocal` to avoid prop drilling within
  a feature — pass parameters down instead.

## Concurrency

- Single coroutine context per ViewModel (`viewModelScope`).
- Use `Dispatchers.Default` for CPU-bound work, `Dispatchers.IO`
  only when interop demands it (Ktor handles its own dispatchers).
- **No `runBlocking`** outside tests.
- **No `GlobalScope`.** Anywhere.
