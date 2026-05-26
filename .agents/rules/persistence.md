---
trigger: glob
globs: 'common/src/main/**/*.kt'
description: 'DataStore (Preferences and Proto), entity caches, AuthStorage, no SharedPreferences. Migrations and disk-I/O hygiene.'
applyTo: 'common/src/main/**/*.kt'
---

# Persistence

## What lives where

| Need                                       | Tool                                            |
| ------------------------------------------ | ----------------------------------------------- |
| User preferences (theme, flags, token)     | DataStore — `Preferences` or typed Proto        |
| Entity caches (recent movies, upcoming)    | Existing entity stores in `common/.../<entity>/`|
| Ephemeral in-memory cache                  | `MutableStateFlow<…>` inside a `single { }` Koin scope |
| Auth tokens / sensitive material           | DataStore + Android Keystore-backed encryption  |
| Files (downloaded artwork, OkHttp cache)   | App cache directory (already wired)             |

**Forbidden in new code:**

- `SharedPreferences` direct usage (use DataStore).
- Realm.
- SQLDelight (the codebase has not adopted it; do not introduce
  without an architecture decision).
- Hand-rolled SQLite via `SupportSQLiteOpenHelper`.

## DataStore — Preferences

```kotlin
private val Context.userPrefs: DataStore<Preferences> by preferencesDataStore("user_prefs")

class UserPreferencesStore(private val dataStore: DataStore<Preferences>) {
    val seasonalThemeEnabled: Flow<Boolean> = dataStore.data
        .map { it[KEY_SEASONAL_THEME] ?: true }

    suspend fun setSeasonalThemeEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_SEASONAL_THEME] = enabled }
    }

    private companion object {
        val KEY_SEASONAL_THEME = booleanPreferencesKey("seasonal_theme")
    }
}
```

Rules:

- One `DataStore<Preferences>` instance per logical scope (user prefs,
  feature-flag overrides, etc.). Don't share a single global store
  across feature concerns.
- Keys live in a `companion object` next to the store. Use the typed
  helpers (`stringPreferencesKey`, `booleanPreferencesKey`, …).
- Reads return `Flow<T>`. Writes are `suspend`.
- Wire stores as `single { }` in Koin.

## Typed (Proto) DataStore

For more complex state (multi-field structures, schema evolution),
use Proto DataStore + kotlinx.serialization Proto:

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

- `@Serializable` data classes with default values for every field.
- Migrations handled through `DataStoreFactory` with a `produceMigrations`
  block when schema changes.
- One file per typed store under `common/.../persistence/`.

## In-memory caches

For state that doesn't need to outlive the process but should be shared
across ViewModels (current login session, scrobble queue):

- Define a class with a private `MutableStateFlow<T>`, expose
  `StateFlow<T>`.
- Wire as `single { }` in Koin so all consumers see the same flow.
- Document the lifecycle: in-memory only, cleared on logout, etc.

## Auth tokens

- Tokens stored in a dedicated DataStore (`auth_prefs`).
- The `AuthStorage` collaborator is the **only** type that reads or
  writes auth tokens.
- Repositories and ViewModels go through `AuthStorage`; they do not
  reach DataStore directly.
- Token refresh handled in the Ktor authorised client's `Auth` plugin
  (see `networking.md`).

## Migrations

- For Preferences DataStore: use `SharedPreferencesMigration` once
  during the legacy-shedding pass; document in a one-line comment when
  retiring an old key.
- For Proto DataStore: bump the schema version, write a migration
  closure that maps old → new defaults.

## Disk I/O hygiene

- All DataStore reads/writes are `suspend` — they already dispatch off
  the main thread.
- File caches live under `context.cacheDir` (OkHttp / Ktor already use
  this); never under `filesDir` unless data must survive cache eviction.
- Do not block on `runBlocking { dataStore.data.first() }` in
  production code. Read through Flow.

## Quick checklist

- [ ] DataStore (not SharedPreferences) for new preferences
- [ ] Keys typed via `*PreferencesKey` helpers
- [ ] Reads return `Flow<T>`, writes are `suspend`
- [ ] Store wired as `single { }` in Koin
- [ ] Auth tokens go through `AuthStorage`, never DataStore directly
- [ ] No `runBlocking { … .first() }` in production code paths
