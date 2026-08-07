---
trigger: glob
globs: '{**/*ViewModel.kt,**/*State.kt}'
description: 'ViewModel rules: single combined state declared last with no explicit type, indexed-cast combine + stateIn, one MutableStateFlow per state field, no mutableStateOf, one-shot events via SharedFlow, viewModelScope, constructor injection, size limits.'
applyTo: '{**/*ViewModel.kt,**/*State.kt}'
---

# ViewModels

> **TL;DR**: One `state` per ViewModel — declared **last**, with **no explicit
> type**, built by `combine(…) { state -> FeatureState(state[0] as T, …) }`
> `.stateIn(viewModelScope, WhileSubscribed(5_000), initialState)`. One private
> `MutableStateFlow` per state field. No `mutableStateOf`, no `Context`, no
> Compose types. One-shot events through `SharedFlow(replay = 0)`. Constructor
> injection via Koin `viewModel { }`. Canonical home for ViewModel rules —
> `architecture.md` and `state-management.md` point here.

## Shape

Canonical layout, top to bottom: constructor collaborators, derived
`private val`s (`destination`, `initialState`), one private `MutableStateFlow`
per state field, plain `private var` bookkeeping, `init { }`, private loaders,
public intent functions, and **`state` last**.

```kotlin
@Suppress("UNCHECKED_CAST")
internal class AllDiscoverViewModel(
    savedStateHandle: SavedStateHandle,
    private val filterManager: GlobalFilterManager,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<DiscoverDestination>()
    private val initialState = AllDiscoverState()

    private val userState = MutableStateFlow(initialState.user)
    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val typeState = MutableStateFlow(destination.source)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages = 1
    private var hasMoreData = false

    init {
        loadInitialData()
        observeFilters()
    }

    // private loaders / observers …

    fun setFilter(filter: GlobalFilter) {
        filterState.update { filter }
        loadData()
    }

    val state = combine(
        typeState,
        filterState,
        collectionStateProvider.stateFlow,
        itemsState,
        loadingState,
        loadingMoreState,
        userState,
        errorState,
    ) { state ->
        AllDiscoverState(
            type = state[0] as DiscoverSection,
            filter = state[1] as GlobalFilter,
            collection = state[2] as UserCollectionState,
            items = state[3] as ImmutableList<DiscoverItem>?,
            loading = state[4] as LoadingState,
            loadingMore = state[5] as LoadingState,
            user = state[6] as? User,
            error = state[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
```

See `core/discover/sections/all/AllDiscoverViewModel.kt` for the live reference.

## The `state` declaration

Non-negotiable shape:

- **`state` is the last declaration in the class.** Below `init`, below every
  loader, below every public intent function. Reading a ViewModel top-down ends
  at the state it produces.
- **No explicit type on `state`.** Write `val state = combine(…)`, never
  `val state: StateFlow<AllDiscoverState> = …`. Type is inferred from the
  builder; an explicit annotation is noise that drifts.
- **Always the indexed-cast `combine` form**, even for two sources and even for
  one — the vararg `combine(…) { state -> … }` overload with
  `state[n] as Type`. Consistency across every ViewModel beats the typed
  `combine(a, b) { a, b -> … }` overloads, and it survives adding a source
  without a rewrite.
- **Cast per field, positionally.** `state[n] as Type` for non-null,
  `state[n] as Type?` for nullable fields, `state[n] as? Type` where the source
  flow's element type is looser than the state field.
- **Argument order matches nothing but itself** — keep the `combine(…)` source
  order and the `state[n]` indices in lockstep. Adding a source appends to both
  lists; never renumber by hand halfway.
- **`@Suppress("UNCHECKED_CAST")` on the class.** The array casts are unchecked
  by construction; the suppression is expected, not a smell.
- **`stateIn` with named arguments**: `scope = viewModelScope`,
  `started = SharingStarted.WhileSubscribed(5_000)`,
  `initialValue = initialState`.
- **`private val initialState = FeatureState()`** near the top. Every
  `MutableStateFlow` seeds from it (`MutableStateFlow(initialState.items)`), and
  `stateIn` uses it as `initialValue` — one place defines "empty screen".

## State rules

- **One `state` per ViewModel.** Never expose several flows for the view to
  combine. Combine here, emit one object.
- **One private `MutableStateFlow` per state field**, named `<field>State`
  (`itemsState`, `loadingState`, `errorState`). Loaders and intent functions
  mutate those; the combine turns them into the public state.
- **Don't expose `MutableStateFlow` publicly.** The only public state member is
  `state`.
- Use `update { … }` on `MutableStateFlow`, not `value = value.copy(…)`.
- Read a current value inside loaders via `filterState.value` — don't collect
  your own state flow from within the ViewModel.
- **State type is a flat `@Immutable data class` with a default for every
  field** — `<Feature>State()` must construct. Loading and failure ride as
  fields (`loading: LoadingState`, `error: Exception?`), not as sealed variants.
  A sealed state type is fine only when the screen's modes are genuinely
  exclusive; then `when` over it exhaustively, no `else ->` branch.
- Collections in state are `ImmutableList<T>` / `PersistentList<T>` from
  `kotlinx.collections.immutable`. Plain `List<T>` is unstable to Compose. See
  `state-management.md` for Compose stability detail.

## Boundaries

- ViewModels **don't** call composables, hold an Android `Context`, or depend on
  `View` / `Composer` types.
- ViewModels don't enqueue WorkManager work directly — call a `SyncTrigger`
  collaborator (see `architecture.md`).
- ViewModels don't touch DataStore or auth tokens directly — go through a
  repository / `AuthStorage` (see `persistence.md`).
- ViewModels don't see raw `IOException` / `HttpException`. Repositories map
  failures to typed `ApiError` / `Result<…>`; the ViewModel parks them in
  `errorState` and the view renders localised copy from the state's `error`
  field (see `networking.md`).
- **TV reuses the same ViewModel** where underlying state is identical; only the
  Compose tree differs. Shared state types and ViewModels live in `:common`.

## One-shot events

Navigation effects, snackbars, toasts — not state, so not folded into the
combine. Declared above `state`, which stays the last member:

```kotlin
private val eventsFlow = MutableSharedFlow<MovieEvent>(replay = 0)
val events = eventsFlow.asSharedFlow()

@Composable
fun MovieSummaryScreen(viewModel: MovieSummaryViewModel = koinViewModel()) {
    val snackbar = LocalSnackbarState.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MovieEvent.WatchlistAdded -> snackbar.show(L10n.movieAddedToWatchlist)
                is MovieEvent.Error -> snackbar.show(event.message)
            }
        }
    }
    // …
}
```

- `SharedFlow` with `replay = 0` — never `StateFlow` for events.
- Collect in `LaunchedEffect`, never from inside composition `body { }`.

## State ownership vs the composable

- ViewModel owns screen state; composables read it via
  `collectAsStateWithLifecycle()` and pass user intents back as lambdas.
- `remember { mutableStateOf(...) }` is for **transient UI** that doesn't need
  to survive a ViewModel rebuild — sheet visibility, scroll position, dropdown
  expanded, focus. Config-change survivors belong in the ViewModel.
- `rememberSaveable` for transient state surviving config change but not
  back-stack removal.
- State outliving the screen (auth tokens, preferences, sync watermarks) goes to
  DataStore; the ViewModel reads it through a repository exposing `Flow<…>`.

```kotlin
@Composable
fun ExampleScreen(
    id: Long,
    viewModel: ExampleViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Content(
        state = state,
        onRetry = viewModel::reload,
        onBack = onBack,
    )
}
```

## Concurrency

- Launch work in `viewModelScope`. Never `GlobalScope`, never `lifecycleScope`
  from a ViewModel.
- Single coroutine context per ViewModel.
- `Dispatchers.Default` for CPU-bound work; `Dispatchers.IO` only when interop
  demands it (Ktor manages its own dispatchers).
- **No `runBlocking`** in a ViewModel. Tests only.
- Respect cancellation — `withTimeout`, `ensureActive`. Don't wrap the whole
  body in `try { … } catch (e: Exception) { … }` and swallow
  `CancellationException`.

## Dependency injection

- **Constructor injection only.** No `KoinJavaComponent.getKoin().get<T>()` or
  `GlobalContext.get()` inside the class body.
- Wire every ViewModel as `viewModel { }` in the feature's
  `core/<feature>/di/<Feature>Module.kt`.
- Prefer `viewModelOf(::FeatureViewModel)`; fall back to the lambda form only
  for parametrised definitions or qualified params.
- 3+ constructor collaborators of the same shape → group into a `data class`
  param, or use named arguments at every call site.

```kotlin
internal val movieModule = module {
    viewModelOf(::MovieListViewModel)
    // parametrised ViewModel: no Of variant, keep the lambda form
    viewModel { (id: Long) -> MovieSummaryViewModel(id = id, repository = get()) }
}
```

- Composables obtain ViewModels via `koinViewModel()` /
  `koinViewModel { parametersOf(…) }`.

## Collaborators

- ViewModel talks to repositories directly for single-source reads.
- Add a `UseCase` **only when** it combines 2+ repositories, encapsulates
  non-trivial reused business logic, or is shared across ViewModels. No
  one-line `UseCase` wrapper around a single repository call.
- Non-trivial mapping DTO/domain → UI belongs in a pure mapper or the state
  type, not inline in the flow chain.
- Typed IDs (`MovieId`, `EpisodeId`, `Slug`) in ViewModel signatures where
  cross-entity swap confusion is a real risk.

## Size & structure

- **God ViewModels are a smell — >~300 lines, split by concern.** Extract
  use-cases, delegate collaborators, or split the screen.
- File naming: `<Feature>ViewModel.kt`, state in `<Feature>State.kt`, Koin
  module in `di/<Feature>Module.kt`.
- Side effects (network, disk, Firebase, analytics) live in repositories,
  use-cases, or ViewModel collectors — never in mappers, formatters, or
  composables.

## Quick checklist

- [ ] `state` is the **last** declaration in the class
- [ ] `state` has **no explicit type** — `val state = combine(…)`
- [ ] Built with the indexed-cast `combine(…) { state -> FeatureState(state[0] as T, …) }` form
- [ ] `@Suppress("UNCHECKED_CAST")` on the class
- [ ] `combine` source order and `state[n]` indices in lockstep
- [ ] 
  `stateIn(scope = viewModelScope, started = WhileSubscribed(5_000), initialValue = initialState)`
- [ ] `private val initialState = FeatureState()` seeds every `MutableStateFlow`
- [ ] One private `MutableStateFlow` per state field, named `<field>State`
- [ ] No `mutableStateOf`, no `Context`, no Compose types in the ViewModel
- [ ] `MutableStateFlow` / `MutableSharedFlow` private; public types read-only
- [ ] One-shot events on `SharedFlow(replay = 0)`, collected in `LaunchedEffect`
- [ ] Errors arrive pre-mapped as `ApiError` / `Result<…>`, land in `errorState`
- [ ] Constructor injection, wired as `viewModel { }` / `viewModelOf(::…)`
- [ ] Work launched in `viewModelScope`, no `runBlocking` / `GlobalScope`
- [ ] Under ~300 lines
