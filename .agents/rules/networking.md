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
- Regenerate: `./gradlew openApiGenerate`.
- Generated sources **not edited by hand**. Root `build.gradle.kts` runs post-generate tasks:
  - `dedupeSerializable` — removes duplicate `@Serializable` annotations.
  - `publicApiClient` — makes generated `HttpClient` public.
  - `removeGenders` — strips unused gender param from people endpoints.
- Spec changes: regenerate, commit diff — generated sources committed so consumers skip generator at build time.
- Generated files excluded from ktlint via `.editorconfig`.

## Ktor client setup

`common/.../networking/KtorClientFactory` builds two `HttpClient` instances over single shared `OkHttp` engine:

- **Authorised** — attaches bearer token from auth storage, refreshes on 401, sends Trakt API headers.
- **Public** — no token, unauthenticated endpoints.

Rules:

- New endpoints reuse existing `HttpClient` instances injected via Koin. No new `HttpClient` per call.
- Logging via `Logger.SIMPLE` (Timber bridge) at `LogLevel.HEADERS` in debug, `LogLevel.NONE` in release.
- File-based response cache under app's cache dir.
- Use `ContentNegotiation` + `json()` for kotlinx.serialization, or `moshi()` where existing endpoints need it.

## Domain mappers

Hand-written, pure, deterministic. One mapper per endpoint or entity.

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

- Mappers `internal`, named `…ToDomain` or `mapTo<Domain>`.
- No I/O, no logging, no `runBlocking`.
- Mappers handle nullability, provide sensible domain defaults (empty strings, empty lists) — domain types non-nullable where possible.
- Status `204` (no content) = **success**, not failure. Mappers and repositories return empty/default values. Recent bug: credits endpoints returned `204`, treated as errors.

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

- `@Immutable` on models held in Compose state.
- Use `kotlin.time.Duration`, `kotlinx.datetime.LocalDate / Instant` for time. `kotlinx-datetime` in version catalogue.
- Use `ImmutableList<T>` / `PersistentList<T>` for collections in state.

## Repositories

Repositories = public boundary between data and rest of app.

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

- **Public reads return `Flow<T>`.** Hot or cold, caller doesn't care.
- **Public writes are `suspend fun`** returning `Result<T>` or domain-specific sealed result type.
- **Offline-first**: read local first, refresh remote in background, emit local stream.
- **Local storage = single source of truth.** Remote responses persisted to local before emit. Never forward HTTP response straight to caller — breaks offline-first when network fails on retry.
- **Implementations `internal`.** Expose only interface through Koin.
- **No `Activity` / `Context` / `Composable` references** in repositories.

## Errors

Define typed error hierarchy at boundary:

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

- Catch Ktor exceptions in one place (interceptor or repository), map to `ApiError`.
- Repository return types carry error via `Result<…>` or domain sealed type — no raw `IOException` / `HttpException` escaping into ViewModels.
- ViewModel surfaces `ApiError` through `UiState.Error(...)` with localised copy.

## Authentication

- Bearer token storage: DataStore (`Preferences`) wrapped by `AuthStorage` collaborator.
- Token refresh: handled by authorised `HttpClient`'s `Auth` plugin config. Repositories don't touch auth state.
- OAuth flows (sign-in, device code): keep in dedicated `auth/` feature folder, don't couple to feature ViewModels.

## TTL & caching

Define TTLs via centralised helper:

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

- [ ] DTO from generated OpenAPI client (or documented exception)
- [ ] Mapper pure, named `…ToDomain` / `mapTo<Domain>`
- [ ] Repository exposes `Flow<…>` reads and `suspend` writes
- [ ] Errors mapped to typed `ApiError` before ViewModel
- [ ] Status `204` handled explicitly where applicable
- [ ] Koin module wires repository via `single { }`
- [ ] No `Activity` / `Context` references in data classes