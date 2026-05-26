---
trigger: glob
globs: 'common/src/main/**/*.kt'
description: 'OpenAPI client, Ktor 3 setup, mappers, ApiError, repository contract with local-write-first, TTL helpers, pagination.'
applyTo: 'common/src/main/**/*.kt'
---

# Networking

## Layers

| Layer                         | Lives in                                                   |
| ----------------------------- | ---------------------------------------------------------- |
| OpenAPI spec                  | `openapi/openapi.json`                                     |
| Generated API client (DTOs)   | `build/generate-resources/.../main/src/main/kotlin/`       |
| Ktor client + interceptors    | `common/.../networking/`                                   |
| Hand-written domain mappers   | `common/.../<entity>/` or `common/.../networking/mappers/` |
| Repositories                  | feature-local `core/<feature>/data/` or `common/.../<entity>/` |

## OpenAPI client (generated)

- Source of truth: `openapi/openapi.json`.
- Regenerate with `./gradlew openApiGenerate`.
- Generated sources are **not edited by hand**. The root
  `build.gradle.kts` runs post-generate tasks to normalise the
  output:
  - `dedupeSerializable` — removes duplicate `@Serializable`
    annotations.
  - `publicApiClient` — makes the generated `HttpClient` public.
  - `removeGenders` — strips the unused gender parameter from people
    endpoints.
- When the OpenAPI spec changes, regenerate and commit the diff —
  generated sources are committed so consumers don't need the
  generator at build time.
- Generated files are excluded from ktlint via `.editorconfig`.

## Ktor client setup

`common/.../networking/KtorClientFactory` builds two `HttpClient`
instances over a single shared `OkHttp` engine:

- **Authorised** — attaches the bearer token from auth storage,
  refreshes on 401, sends Trakt API headers.
- **Public** — no token, used for unauthenticated endpoints.

Rules:

- New endpoints reuse the existing `HttpClient` instances injected via
  Koin. Don't construct a new `HttpClient` per call.
- Logging via `Logger.SIMPLE` (Timber bridge) at `LogLevel.HEADERS` in
  debug builds, `LogLevel.NONE` in release.
- File-based response cache lives under the app's cache dir.
- Use `ContentNegotiation` + `json()` for kotlinx.serialization, or
  `moshi()` where existing endpoints rely on it.

## Domain mappers

Hand-written, pure, deterministic. One mapper per endpoint or per
entity.

```kotlin
// common/.../movie/MovieMapper.kt

internal fun MovieDto.toDomain(): Movie = Movie(
    id = ids.trakt,
    slug = ids.slug.orEmpty(),
    title = title.orEmpty(),
    overview = overview,
    releasedAt = released?.toLocalDate(),
)
```

- Mappers are `internal` and named `…ToDomain` or `mapTo<Domain>`.
- No I/O, no logging, no `runBlocking`.
- Mappers handle nullability and provide sensible domain defaults
  (empty strings, empty lists) — domain types are non-nullable where
  possible.
- Status `204` (no content) is **success**, not failure. Mappers and
  repositories handle `204` by returning empty/default values. Recent
  bug across stacks: credits endpoints returned `204` and were treated
  as errors.

## Domain models

Live in `common/.../<entity>/`:

```kotlin
@Immutable
data class Movie(
    val id: Long,
    val slug: String,
    val title: String,
    val overview: String?,
    val releasedAt: LocalDate?,
)
```

- `@Immutable` annotation on models held in Compose state.
- Use `kotlin.time.Duration`, `kotlinx.datetime.LocalDate /
  Instant` for time values. `kotlinx-datetime` is in the version
  catalogue.
- Use `ImmutableList<T>` / `PersistentList<T>` for collections held
  in state.

## Repositories

Repositories are the public boundary between data and the rest of the
app.

```kotlin
interface MovieRepository {
    fun movie(id: Long): Flow<Movie>
    suspend fun refresh(id: Long): Result<Unit>
}

internal class MovieRepositoryImpl(
    private val remote: MoviesApi,
    private val local: MovieCache,
) : MovieRepository {

    override fun movie(id: Long): Flow<Movie> = local
        .observe(id)
        .onStart { runCatching { refresh(id) } }

    override suspend fun refresh(id: Long): Result<Unit> = runCatching {
        val dto = remote.getMovie(id = id, extended = "full")
        local.put(dto.toDomain())
    }
}
```

Rules:

- **Public reads return `Flow<T>`.** Hot or cold, the caller doesn't
  care.
- **Public writes are `suspend fun`** returning `Result<T>` or a
  domain-specific sealed result type.
- **Offline-first**: prefer reading from local first, refresh from
  remote in the background, emit the local stream.
- **Local storage is the single source of truth.** Remote responses
  are persisted to local before the repository emits them. Never
  forward an HTTP response straight to the caller — that breaks the
  offline-first invariant the moment the network fails on a retry.
- **Implementations are `internal`.** Expose only the interface
  through Koin.
- **No `Activity` / `Context` / `Composable` references** in
  repositories.

## Errors

Define a typed error hierarchy at the boundary:

```kotlin
sealed interface ApiError {
    data class Http(val status: Int) : ApiError
    data class Decoding(val cause: Throwable) : ApiError
    data class RateLimited(val retryAfter: Duration?) : ApiError
    data object Offline : ApiError
    data object Canceled : ApiError
    data class Unknown(val cause: Throwable) : ApiError
}
```

- Catch Ktor exceptions in one place (interceptor or repository) and
  map to `ApiError`.
- Repository return types carry the error via `Result<…>` or a
  domain sealed type — don't let raw `IOException` / `HttpException`
  escape into ViewModels.
- ViewModel surfaces `ApiError` through `UiState.Error(...)` with
  localised copy.

## Authentication

- Bearer token storage: DataStore (`Preferences`) wrapped by an
  `AuthStorage` collaborator.
- Token refresh: handled by the authorised `HttpClient`'s `Auth` plugin
  configuration. Repositories don't reach into auth state.
- For OAuth flows (sign-in, device code), keep the flow in a dedicated
  `auth/` feature folder; do not couple to feature ViewModels.

## TTL & caching

When caching domain entities, define TTLs through a centralised helper:

```kotlin
object TraktDurations {
    val stable = 12.hours      // summaries, translations
    val normal = 3.hours       // lists, ratings
    val frequent = 30.minutes  // user-affecting lists
    val live = 5.minutes       // watchers, now-playing
}
```

Never cache for `Duration.INFINITE`.

## Quick checklist

- [ ] DTO comes from the generated OpenAPI client (or a documented
      exception)
- [ ] Mapper is pure, named `…ToDomain` / `mapTo<Domain>`
- [ ] Repository exposes `Flow<…>` reads and `suspend` writes
- [ ] Errors mapped to a typed `ApiError` before reaching the ViewModel
- [ ] Status `204` handled explicitly where applicable
- [ ] Koin module wires the repository via `single { }`
- [ ] No `Activity` / `Context` references in data classes
