---
trigger: glob
globs: '**/*.kt'
description: 'MVVM + UDF, Compose-only UI, ViewModel + StateFlow, Compose Navigation typed routes, repositories. Background sync, adaptive layouts.'
applyTo: '**/*.kt'
---

# Architecture

> **TL;DR**: MVVM + Unidirectional Data Flow. Compose-only UI. ViewModels
> expose `StateFlow<UiState>`. Repositories expose `Flow<…>` for reads
> and `suspend` for writes. DI via Koin. Navigation via Compose
> Navigation 2.9+ with `@Serializable` typed routes. Inspired by Now in
> Android (Google) and Tivi (Chris Banes); selectively borrows.

## Invariants

Hold across every layer. Lifted from Now in Android's `ArchitectureLearningJourney.md`:

- **Higher layers react to lower layers.** UI reacts to domain reacts to data. Never reverse.
- **Events flow down, data flows up.** User intents propagate down as function calls / event sinks; state propagates up as `Flow`s and `StateFlow`s.
- **Local storage is single source of truth.** Repositories write remote responses to local storage **before** emitting to callers; callers always observe local stream. Remote-only emission paths break offline-first.
- **Repositories own domain models.** Mappers translate at network boundary; no DTO leaks past repository, no domain model leaks across feature boundaries — features depend on `:common` models, not each other.

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

- Add `UseCase` **only when** ViewModel needs to combine multiple repositories or apply non-trivial business logic that is reused. No `UseCase` that just forwards to single repository method — call repository directly.
- Repositories own **offline-first** behaviour: read local first, refresh remote in background, emit updates through same Flow.

## Composables

Composables are pure functions of parameters. State flows in through parameter list; events flow out through lambdas.

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

- **Split `<Feature>Screen` (stateful) from `<Feature>Content` (stateless).** `Content` takes state and event lambdas only — easy to preview and snapshot-test.
- **No `@Preview` on stateful screen.** Preview `Content` variant.
- **`when` over sealed `UiState` is exhaustive.** Add case to sealed type instead of `else ->` branch.

## ViewModels

ViewModels expose a single `state` — declared last, untyped, built by the indexed-cast
`combine(…)` + `stateIn(viewModelScope, WhileSubscribed(5_000), initialState)` pattern. One-shot
events flow through `SharedFlow(replay = 0)`.

**Full ViewModel rules live in `viewmodel.md`** — declaration shape, state ownership, boundaries,
events, concurrency, DI, size limits, testing. Read it before writing or editing a ViewModel.

## Domain Layer (Optional)

Use-cases live in `core/<feature>/domain/` only when they earn keep:

- Combine 2+ repositories.
- Encapsulate non-trivial business logic (rate-limit-aware refreshes, complex ranking).
- Shared by multiple ViewModels.

Avoid one-line `UseCase` wrappers around repository calls.

## Data Layer

- **Repository** is public boundary. Located in `core/<feature>/data/` or `common/.../<entity>/`.
- **Reads return `Flow<T>`.** Local + remote sources flow through repository unified by `data` layer; ViewModel sees only unified stream.
- **Writes are `suspend fun` returning `Result<…>`** (or domain-specific sealed result type).
- **Mappers** are pure functions named `mapTo<Domain>(...)` or `<Source>Mapper.kt`. Live next to repository. No I/O.

## Navigation

Single-activity, single `NavHost` in `app/`, mirror in `tv/`. Typed routes via `@Serializable` data classes (Compose Navigation 2.9+):

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
- **No global navigator singleton.** Pass `navController` callbacks down as lambdas (`onMovieClick: (Long) -> Unit`) so feature composables stay framework-agnostic.
- **Feature-local `NavGraphBuilder` extensions** expose feature destinations:

```kotlin
fun NavGraphBuilder.movieFeature(onBack: () -> Unit) {
    composable<MovieSummaryRoute> { … }
    composable<MovieCastRoute> { … }
}
```

## Dependency Injection (Koin)

- Constructor injection at every layer.
- One Koin module per feature, named `<Feature>Module.kt`, located in `core/<feature>/di/`.
- Wire all feature modules in `TraktApplication.setupKoin()`.

```kotlin
internal val movieModule = module {
    singleOf(::MovieRepositoryImpl) bind MovieRepository::class
    factoryOf(::GetMovieSummaryUseCase)
    // parametrised ViewModel: no Of variant, keep the lambda form
    viewModel { (id: Long) -> MovieSummaryViewModel(id = id, repository = get()) }
}
```

Scopes:

- **`single { }`** — repositories, clients, caches, stateful collaborators.
- **`factory { }`** — use-cases, mappers, stateless + cheap.
- **`viewModel { }`** — every ViewModel.

DSL form:

- **Default to the constructor-reference DSL (`singleOf`, `factoryOf`, `viewModelOf`, `workerOf`)** — `singleOf(::Foo)` replaces `single { Foo(get(), get()) }`; Koin resolves params by type.
- **Bind interfaces with `bind`**: `singleOf(::FooRepositoryImpl) bind FooRepository::class`.
- **Lambda form (`single { … }`) only when `Of` can't express it**: parametrised definitions (`viewModel { (id) -> … }`), qualified/named params, `get(named(…))`, or inline-built values. See `core/lists/di/ListsModule.kt`.

## Background sync

Periodic refreshes (calendar, up-next, watchlist deltas, scrobble flush) run as `CoroutineWorker` instances inside `sync/` package today, or dedicated `:sync` module once pattern grows.

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
- Refresh tasks fan out via `async` + `awaitAll`. Single failures don't cancel siblings unless intended.
- Return `Result.retry()` on failure so WorkManager applies exponential backoff. No custom retry loop inside worker.
- Use `OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST` when expediting user-triggered syncs.
- Provide `ForegroundInfo` only when user actively waiting (manual pull-to-refresh chained through WorkManager).
- Sync entry points (which Workers to enqueue when) live in `common/.../sync/` or `app/.../sync/`. Composables / ViewModels don't enqueue work directly — call `SyncTrigger` collaborator.

## Adaptive layouts

Phone + tablet + Android TV — three form factors share most code. Lean on Material 3 Adaptive (`androidx.compose.material3.adaptive`) and `WindowSizeClass`:

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

- **Pass `WindowSizeClass` as parameter** to feature `Content` composables that need it. Compute once at app root via `calculateWindowSizeClass(activity)`.
- **Never** branch on `Resources.configuration.smallestScreenWidthDp` inside composable or `if (isTablet)` from helpers.
- TV-specific composables live in `:tv` module; phone variants in `:app`. Share state types and ViewModels via `:common`.
- Use `TraktTheme.spacing` / `TraktTheme.size` tokens — vary by window class internally.

## Single-Activity, Phone vs TV

- `MainActivity` (`app/`) hosts phone NavHost.
- `TvActivity` (`tv/`) hosts TV NavHost.
- Routing at startup: `MainActivity` detects `isTelevision()` and forwards to `TvSplashActivity`.
- **TV reuses same ViewModels** where underlying state identical; only Compose tree differs.

## Compose-as-Function

- Composables take **parameters in, render output, expose events as lambdas.** No object identity, no mutable side state outside `remember`.
- `Modifier` always second parameter with default (`modifier: Modifier = Modifier`).
- Slot APIs use `@Composable` lambda parameters (`content: @Composable () -> Unit`).

## Composition Locals

Existing pattern: `LocalBottomBarVisibility`, `LocalSnackbarState`, `LocalCheckInVisibility`, `LocalRatePromptVisibility`. Use sparingly for **ambient app-level UI state** that doesn't fit feature ViewModel.

Guidelines:

- Define `staticCompositionLocalOf` for values that don't change during recomposition.
- Provide via `CompositionLocalProvider` at app root.
- Don't reach for `CompositionLocal` to avoid prop drilling within feature — pass parameters down instead.

## Concurrency

- Single coroutine context per ViewModel (`viewModelScope`).
- Use `Dispatchers.Default` for CPU-bound work, `Dispatchers.IO` only when interop demands it (Ktor handles own dispatchers).
- **No `runBlocking`** outside tests.
- **No `GlobalScope`.** Anywhere.
