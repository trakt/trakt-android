---
trigger: glob
globs: '**'
description: 'Functional programming, immutability, early exits, single responsibility, init injection via Koin, typed IDs. Apply to all Kotlin source.'
applyTo: '**'
---

# Code Principles

## Functional Programming

- **Prefer pure functions.** Same input → same output.
- **Side effects at edges.** I/O (network, disk, Firebase, analytics) lives in repositories, use-cases, ViewModel collectors — never inside mappers, formatters, composables.
- Composables: pure functions of params. State must come from hoisted source (parameter, `CompositionLocal`, `collectAsStateWithLifecycle`).

## Immutability

- **Prefer `val` over `var`.** Use `var` only when local mutation measurably simplifies code.
- Default to value types: `data class`, `sealed class`, `enum`. Plain `class` only when behaviour requires.
- Use `kotlin.collections.immutable` (`ImmutableList`, `PersistentMap`) for state in `data class`es exposed to Compose. Dep already in version catalogue.
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

Compose `when` handlers must be exhaustive — no `else ->` branches that swallow new cases. Non-exhaustive `when` on sealed type silently drops new variants.

## Code Smells to Avoid

- **Nested conditionals** — refactor into guard clauses or separate functions.
- **God ViewModels** — >~300 lines = smell; split by concern.
- **Abstraction leaks** — callers shouldn't know how type is built.
- **Singleton-style lookups inside types** — accept deps via Koin `module { … }` + constructor injection. No `KoinJavaComponent.getKoin().get<T>()` inside class body.
- **`!!` non-null assertions** in production code. Use `requireNotNull` with message, `checkNotNull` for internal invariants, or `?:` fallback. `!!` OK in tests and previews.
- **`runBlocking`** in production code. Tests and CLI tools only.

## Function Design

- **Single Responsibility** — one job per function.
- Names describe what, not how.
- **3+ parameters** → single `data class` param (or named arguments at every call site).

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

- **Constructor injection** for ViewModels, use-cases, repositories, clients.
- Wire deps in Koin modules under `common/.../di/` or `<feature>/di/<Feature>Module.kt`.
- Scope:
  - **`single { }`** — clients, repositories, caches, expensive/stateful things.
  - **`factory { }`** — use-cases, mappers, stateless cheap things.
  - **`viewModel { }`** — every ViewModel.
- No mixing Koin with Hilt/Dagger. Codebase standardised on Koin.
- Compose: ViewModels via `koinViewModel()`. Composables receiving collaborators take them as params (not service locator inside body).

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

- Simple code = maintainable code.
- Readability over cleverness.
- If solution feels complex, step back before adding abstraction.

## Typed IDs

Wrap primitive identifiers in `@JvmInline value class` at domain boundary:

```kotlin
@JvmInline value class MovieId(val raw: Long)
@JvmInline value class EpisodeId(val raw: Long)
@JvmInline value class Slug(val raw: String)
```

- Mappers in `common/.../networking/` only place that produces them from DTOs (`MovieDto.ids.trakt.let(::MovieId)`).
- Repository / use-case / ViewModel signatures take typed form — `MovieId` cannot pass where `EpisodeId` expected.
- Compose Navigation typed routes accept wrapped form via custom `NavType` for `@Serializable` data classes.

**Recommendation, not hard mandate.** Apply when swap-confusion cost is real (cross-entity APIs: `Credits`, `Reviews`, `Lists`). Skip for purely internal helpers.

## Iteration

- Functional operators (`map`, `filter`, `flatMap`, `fold`, `groupBy`) over imperative loops.
- Flows: `map`, `filter`, `combine`, `flatMapLatest`, `distinctUntilChanged`. Use `stateIn` to convert cold flow to hot `StateFlow` at ViewModel layer.
- `forEach` only when body is side effect; otherwise use expression form.
