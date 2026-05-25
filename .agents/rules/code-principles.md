---
trigger: glob
globs: '**'
description: 'Functional programming, immutability, early exits, single responsibility, init injection via Koin, typed IDs. Apply to all Kotlin source.'
applyTo: '**'
---

# Code Principles

## Functional Programming

- **Prefer pure functions.** Same input → same output.
- **Side effects at the edges.** I/O (network, disk, Firebase, analytics)
  lives in repositories, use-cases, and ViewModel collectors — never
  inside mappers, formatters, or composables.
- Composables are pure functions of their parameters. State they read
  must come from a hoisted source (parameter, `CompositionLocal`,
  `collectAsStateWithLifecycle`).

## Immutability

- **Prefer `val` over `var`.** Reach for `var` only when local mutation
  measurably simplifies the code.
- Default to value types: `data class`, `sealed class`, `enum`. Reach
  for plain `class` only when behaviour requires it.
- Use `kotlin.collections.immutable` (`ImmutableList`,
  `PersistentMap`) for state held by `data class`es and exposed to
  Compose. The dep is already in the version catalogue.
- Use `map` / `filter` / `fold` over mutating loops.

**Avoid:**
```kotlin
var result = mutableListOf<Foo>()
for (item in items) result += transform(item)
```

**Prefer:**
```kotlin
val result = items.map(::transform)
```

## Early Exits

Use guard clauses. Avoid nested `let { … }` and nested null checks.

**Avoid:**
```kotlin
fun process(data: Data?): Foo? {
    if (data != null) {
        if (data.isValid) {
            if (data.hasPermission) {
                return transform(data)
            }
        }
    }
    return null
}
```

**Prefer:**
```kotlin
fun process(data: Data?): Foo? {
    if (data == null) return null
    if (!data.isValid) return null
    if (!data.hasPermission) return null

    return transform(data)
}
```

## Sealed Hierarchies for State

Model UI state as a sealed hierarchy. Forces exhaustive `when` and
prevents impossible states.

```kotlin
sealed interface MovieUiState {
    data object Loading : MovieUiState
    data class Loaded(val movie: Movie) : MovieUiState
    data class Error(val throwable: Throwable) : MovieUiState
}
```

Compose `when` handlers must be exhaustive — do not add `else ->`
branches that swallow new cases. Non-exhaustive `when` on a sealed type
silently drops new variants added to the hierarchy.

## Code Smells to Avoid

- **Nested conditionals** — refactor into guard clauses or separate
  functions.
- **God ViewModels** — stores over ~300 lines are smells; split by
  concern.
- **Abstraction leaks** — callers shouldn't need to know how a type is
  built.
- **Singleton-style lookups inside types** — accept dependencies via
  Koin's `module { … }` declarations and constructor injection. Don't
  `KoinJavaComponent.getKoin().get<T>()` from inside a class body.
- **`!!` non-null assertions** in production code. Use `requireNotNull`
  with a message, `checkNotNull` for internal invariants, or a `?:`
  fallback. `!!` is acceptable inside tests and previews.
- **`runBlocking`** in production code. It exists for tests and CLI
  tools only.

## Function Design

- **Single Responsibility** — one job per function.
- Function names describe what the function does, not how.
- Functions with **3 or more parameters** take a single
  `data class` parameter (or named arguments at every call site).

**Avoid:**
```kotlin
fun fetch(url: String, token: String, retry: Int, timeout: Duration): Response
```

**Prefer:**
```kotlin
data class FetchOptions(
    val url: String,
    val token: String,
    val retry: Int,
    val timeout: Duration,
)

fun fetch(options: FetchOptions): Response
```

## Dependency Injection

- **Constructor injection** for ViewModels, use-cases, repositories,
  clients.
- Wire dependencies in Koin modules under
  `common/.../di/` or feature-local `<feature>/di/<Feature>Module.kt`.
- Scope rule of thumb:
  - **`single { }`** — clients, repositories, caches, anything
    expensive or that owns state.
  - **`factory { }`** — use-cases, mappers, anything stateless and
    cheap.
  - **`viewModel { }`** — every ViewModel.
- Do not mix Koin with Hilt / Dagger. The codebase has standardised on
  Koin.
- Compose retrieves ViewModels via `koinViewModel()`. Composables
  receiving collaborators take them as parameters (not via service
  locator lookup inside the body).

**Avoid:**
```kotlin
@Composable
fun ProfileScreen() {
    val client = remember { GlobalContext.get().get<TraktClient>() }
    // …
}
```

**Prefer:**
```kotlin
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // …
}
```

## Simplicity

- Simple code is maintainable code.
- Favour readability over cleverness.
- If a solution feels complex, step back and reconsider before adding
  more abstraction.

## Typed IDs

Wrap primitive identifiers in `@JvmInline value class` at the domain
boundary:

```kotlin
@JvmInline value class MovieId(val raw: Long)
@JvmInline value class EpisodeId(val raw: Long)
@JvmInline value class Slug(val raw: String)
```

- Mappers in `common/.../networking/` are the only place that produces
  them from generated DTOs (`MovieDto.ids.trakt.let(::MovieId)`).
- Repository / use-case / ViewModel signatures take the typed form so
  a `MovieId` cannot be passed where an `EpisodeId` is expected.
- Compose Navigation typed routes accept the wrapped form too via a
  custom `NavType` for `@Serializable` data classes.

This is a **recommendation, not a hard mandate**. Apply when the cost
of swap-confusion is real (cross-entity APIs like `Credits`, `Reviews`,
`Lists`). Skip for purely internal helpers.

## Iteration

- Functional collection operators (`map`, `filter`, `flatMap`, `fold`,
  `groupBy`) over imperative loops.
- For flows: `map`, `filter`, `combine`, `flatMapLatest`,
  `distinctUntilChanged`. Use `stateIn` to convert a cold flow to a
  hot `StateFlow` once at the ViewModel layer.
- `forEach` only when the body is a side effect; otherwise use the
  expression form.
