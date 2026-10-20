---
trigger: glob
globs: 'common/src/main/**/*.kt'
description: 'DataStore (Preferences and Proto), local sources, auth token storage, no SharedPreferences. Migrations and disk-I/O hygiene.'
applyTo: 'common/src/main/**/*.kt'
paths:
  - 'common/src/main/**/*.kt'
  - '{app,tv}/src/main/**/data/**/*.kt'
  - '{app,tv}/src/main/**/di/**/*.kt'
  - 'app/src/main/java/tv/trakt/trakt/TraktApplication.kt'
  - 'tv/src/main/java/tv/trakt/trakt/app/TvActivity.kt'
---

# Persistence

## What lives where

| Need                                       | Tool                                            |
| ------------------------------------------ | ----------------------------------------------- |
| User preferences (theme, flags, token)     | DataStore — `Preferences` or typed Proto        |
| Entity caches (recent movies, upcoming)    | `<Feature>LocalSource` in `common/.../data/local/` |
| Ephemeral in-memory cache                  | `MutableStateFlow<…>` inside a `single { }` Koin scope |
| Auth tokens / sensitive material           | DataStore + Android Keystore-backed encryption  |
| Files (downloaded artwork, OkHttp cache)   | App cache directory (already wired)             |

**Forbidden in new code:**

- `SharedPreferences` direct usage (use DataStore).
- Realm.
- SQLDelight (codebase not adopted; no introduce without architecture decision).
- Hand-rolled SQLite via `SupportSQLiteOpenHelper`.

## DataStore — Preferences

```kotlin
private val Context.userPrefs: DataStore<Preferences> by preferencesDataStore("user_prefs")

class UserPreferencesStore(private val dataStore: DataStore<Preferences>) {
    val autoplayTrailers: Flow<Boolean> = dataStore.data
        .map { it[KEY_AUTOPLAY_TRAILERS] ?: true }

    suspend fun setAutoplayTrailers(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTOPLAY_TRAILERS] = enabled }
    }

    private companion object {
        val KEY_AUTOPLAY_TRAILERS = booleanPreferencesKey("autoplay_trailers")
    }
}
```

Rules:

- One `DataStore<Preferences>` per logical scope. No shared global store across feature concerns.
- Keys in `companion object` next to store. Use typed helpers (`stringPreferencesKey`, `booleanPreferencesKey`, …).
- Reads return `Flow<T>`. Writes are `suspend`.
- Wire stores as `single { }` in Koin.

## Typed (Proto) DataStore

Complex state (multi-field structures, schema evolution): use Proto DataStore + kotlinx.serialization Proto:

```kotlin
@Serializable
data class CalendarPrefs(
    val showHidden: Boolean = false,
    val daysAhead: Int = 14,
)

class CalendarPrefsSerializer : Serializer<CalendarPrefs> {
    override val defaultValue = CalendarPrefs()
    // …
}
```

Rules:

- `@Serializable` data classes, default value every field.
- Migrations via `DataStoreFactory` `produceMigrations` block on schema change.
- Declare the store in the Koin module that owns its consumer: `localSourcesModule` for a shared
  local source, the feature's `di/<Feature>Module.kt` otherwise.

## Local sources

A local source owns one feature's cached data: an in-memory map, a `MutableStateFlow` or a
DataStore. Phone and TV share the same classes in one flat package, `common/.../data/local/`.

```kotlin
// common/.../data/local/MovieLocalSource.kt
class MovieLocalSource {
    private val mutex = Mutex()
    private val storage = mutableMapOf<TraktId, Movie>()

    suspend fun getMovie(movieId: TraktId): Movie? { … }

    suspend fun upsertMovies(movies: List<Movie>) { … }
}

// common/.../data/local/di/LocalSourcesModule.kt
val localSourcesModule = module {
    singleOf(::MovieLocalSource)
    // …
}
```

Rules:

- **One public `class <Feature>LocalSource` per feature. No interface, no `*Storage` impl.** Add an
  interface only when a second implementation actually exists.
- **Lives in `common/.../data/local/`** whenever its types live in `:common`, even when only one
  platform reads it today. Move its model to `:common` first when the model is not genuinely
  platform-specific. A source that stores `:app` or `:tv` models stays in its feature's
  `data/local/` folder and is bound in that feature's Koin module, still as a plain class.
- **All shared local sources bind once in `localSourcesModule`**, registered in both
  `TraktApplication.setupKoin()` and `TvActivity`. Koin singletons are created lazily, so binding a
  phone-only source in TV costs nothing. Never bind a shared local source again in a feature module.
- **Bind with `singleOf(::XLocalSource)`.** Use `single { XLocalSource(dataStore = get(named(…))) }`
  only when a param needs a qualifier. A DataStore backing a shared source is declared next to it in
  `localSourcesModule`.
- **Never rename a DataStore file when moving a source.** The file name is the storage key; a new
  name drops what users already have cached.
- **Pin serial names when moving a stored sealed type.** A stored `List<Sealed>` writes each subtype's
  fully qualified name. Add `@SerialName("<old fqcn>")` to every subtype before changing its
  package, or the old cache fails to decode and is wiped.

## In-memory caches

State not needing process survival but shared across ViewModels (login session, scrobble queue):

- Class with private `MutableStateFlow<T>`, expose `StateFlow<T>`.
- Wire as `single { }` in Koin — all consumers same flow.
- Document lifecycle: in-memory only, cleared on logout, etc.

## Auth tokens

- Tokens in dedicated DataStore (`auth_preferences`), declared in `common/.../auth/di/CommonAuthModule.kt`.
- `TokenProvider` (`DefaultTokenProvider`) only type that reads/writes auth tokens.
- Repositories and ViewModels go through `TokenProvider` / `SessionManager`; no direct DataStore access.
- Token refresh in Ktor authorised client's `Auth` plugin (see `networking.md`).

## Migrations

- Preferences DataStore: use `SharedPreferencesMigration` once during legacy-shedding pass; one-line comment when retiring old key.
- Proto DataStore: bump schema version, write migration closure mapping old → new defaults.

## Disk I/O hygiene

- All DataStore reads/writes `suspend` — already off main thread.
- File caches under `context.cacheDir` (OkHttp / Ktor already use this); never `filesDir` unless data must survive cache eviction.
- No `runBlocking { dataStore.data.first() }` in production. Read through Flow.

## Quick checklist

- [ ] Local source is one class, no interface, in `common/.../data/local/` unless it stores platform models
- [ ] Shared local source bound only in `localSourcesModule`
- [ ] DataStore (not SharedPreferences) for new preferences
- [ ] Keys typed via `*PreferencesKey` helpers
- [ ] Reads return `Flow<T>`, writes are `suspend`
- [ ] Store wired as `single { }` in Koin
- [ ] Auth tokens go through `TokenProvider`, never DataStore directly
- [ ] No `runBlocking { … .first() }` in production code paths