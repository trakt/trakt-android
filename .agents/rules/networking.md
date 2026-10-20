---
trigger: glob
globs: 'common/src/main/**/*.kt'
description: 'OpenAPI client, Ktor 3 setup, mappers, ApiError, repository contract with local-write-first, TTL helpers, pagination.'
applyTo: 'common/src/main/**/*.kt'
paths:
  - 'common/src/main/**/*.kt'
  - '{app,tv}/src/main/**/data/**/*.kt'
  - 'openapi/**/*'
---

# Networking

## Layers

| Layer                         | Lives in                                                   |
| ----------------------------- | ---------------------------------------------------------- |
| OpenAPI spec                  | `openapi/openapi.json`                                     |
| Generated API client (DTOs)   | `build/generate-resources/.../main/src/main/kotlin/`       |
| Ktor client + interceptors    | `common/.../networking/`                                   |
| Hand-written extra APIs       | `common/.../networking/api/<feature>/`                     |
| Remote sources                | `common/.../data/remote/<Feature>RemoteSource.kt`          |
| Hand-written response DTOs    | `common/.../data/dto/`                                     |
| Hand-written request bodies   | `common/.../data/request/`                                 |
| DTO → domain mapping          | `fromDto` companion on the domain model in `common/.../model/` |
| Repositories                  | feature-local `core/<feature>/data/` or `common/.../core/<feature>/` |

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
- Use `ContentNegotiation` + `json()` for kotlinx.serialization where existing endpoints need it.

## Remote sources

A remote source wraps the generated OpenAPI APIs for one feature. Phone and TV share the same
classes; all of them live in one flat package, `common/.../data/remote/`. Remote sources are the
one exception to feature folders.

```kotlin
// common/.../data/remote/PeopleRemoteSource.kt
class PeopleRemoteSource(
    private val peopleExtrasApi: PeopleExtrasApi,
    private val peopleApi: PeopleApi,
) {
    suspend fun getPersonDetails(
        personId: TraktId,
        extended: String = "full,images,markdown",
    ): PersonDto { … }
}

// common/.../data/remote/di/RemoteSourcesModule.kt
val remoteSourcesModule = module {
    singleOf(::PeopleRemoteSource)
    // …
}
```

Rules:

- **One public `class <Feature>RemoteSource` per feature. No interface.** A single-implementation
  interface + `*ApiClient` pair adds a file and a Koin `bind` for nothing. Introduce an interface
  only when a second implementation actually exists.
- **Never in `:app`, `:tv` or a feature folder.** New endpoints go into the shared class in
  `data/remote/`, even when only one platform calls them today.
- **Hand-written DTOs** (endpoints the OpenAPI spec lacks) go to `data/dto/` for responses and
  `data/request/` for request bodies. Never next to the remote source.
- **Platform differences are parameters with the phone default** (`extended: String = "full,images,markdown"`).
  TV passes its own value (`"full,images"`) at the call site; extract repeated values into a
  `private const val` in the calling use-case.
- **Images use `extended=images`.** `cloud9` is legacy; do not add it back.
- **API instances come from `networkingApiModule`** (`common/.../networking/di/NetworkingModule.kt`).
  Never construct `PeopleApi(...)` inside a feature module.
- **All remote sources bind in one `remoteSourcesModule`**, registered in both
  `TraktApplication.setupKoin()` and `TvActivity`. Koin singletons are created lazily, so binding a
  phone-only source in TV costs nothing.
- **Bind with `singleOf(::XRemoteSource)`.** Use `single { XRemoteSource(get(named(…))) }` only when
  a param needs a qualifier (`authorizedClientConfig`, `authorizedCommentsApi`, …). See
  `data/remote/di/RemoteSourcesModule.kt`.
- Remote sources return DTOs. Mapping to domain happens above them, in repositories or use-cases.
  When a remote source catches errors, it must not swallow `CancellationException`
  (`rethrowCancellation`).
- Not every HTTP client is a remote source: non-Trakt clients with platform-specific headers (e.g.
  TV `PlexTimelineClient`) stay in their platform module.

## DTO → domain mapping

The domain model owns its mapping through a `companion object { fun fromDto(...) }`. No separate
mapper files (`*Mapper.kt`, `mapTo*.kt`) and no `Dto.toDomain()` extension functions. See
`Comment.fromDto`, `Images.fromDto`, `Ids.fromDto`.

```kotlin
// common/.../model/Movie.kt

@Immutable
data class Movie(
    val id: TraktId,
    val slug: String,
    val title: String,
) {
    companion object {
        fun fromDto(dto: MovieDto): Movie {
            return Movie(
                id = TraktId(dto.ids.trakt),
                slug = dto.ids.slug.orEmpty(),
                title = dto.title.orEmpty(),
            )
        }
    }
}

// call sites
val movie = Movie.fromDto(dto)
val movies = dtos.map(Movie::fromDto)
```

- One `fromDto` overload per source DTO (see `Ids.fromDto` for show / movie / episode ids).
- Return `T?` when an unknown value must be dropped (unknown enum from a newer API), and call with
  `mapNotNull(T::fromDto)`.
- Pure: no I/O, no logging, no `runBlocking`.
- Handle nullability, provide sensible domain defaults (empty strings, empty lists) — domain types non-nullable where possible.
- Status `204` (no content) = **success**, not failure. Repositories return empty/default values. Recent bug: credits endpoints returned `204`, treated as errors.

## Domain models

Live in `common/.../model/`:

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
        local.put(Movie.fromDto(dto))
    }
}
```

Rules:

- **Public reads return `Flow<T>`.** Hot or cold, caller doesn't care.
- **Public writes are `suspend fun`** returning `Result<T>` or domain-specific sealed result type.
- **Offline-first**: read local first, refresh remote in background, emit local stream.
- **Local storage = single source of truth.** Remote responses persisted to local before emit. Never forward HTTP response straight to caller — breaks offline-first when network fails on retry.
- **Repository implementations `internal`.** Expose only interface through Koin. Does not apply to
  remote sources (see "Remote sources"), which are public classes without an interface.
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

- Bearer token storage: DataStore (`Preferences`) wrapped by `TokenProvider` (`common/.../auth/`).
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
- [ ] Endpoint added to shared `<Feature>RemoteSource` in `data/remote/`, no interface, no `:app`/`:tv` copy
- [ ] Platform differences expressed as parameters with phone defaults
- [ ] DTO mapped via `companion object { fun fromDto }` on the domain model, pure, no mapper files
- [ ] Repository exposes `Flow<…>` reads and `suspend` writes
- [ ] Errors mapped to typed `ApiError` before ViewModel
- [ ] Status `204` handled explicitly where applicable
- [ ] Koin module wires repository via `single { }`
- [ ] No `Activity` / `Context` references in data classes
