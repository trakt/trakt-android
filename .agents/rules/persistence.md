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
- SQLDelight (codebase not adopted; no introduce without architecture decision).
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
- One file per typed store under `common/.../persistence/`.

## In-memory caches

State not needing process survival but shared across ViewModels (login session, scrobble queue):

- Class with private `MutableStateFlow<T>`, expose `StateFlow<T>`.
- Wire as `single { }` in Koin — all consumers same flow.
- Document lifecycle: in-memory only, cleared on logout, etc.

## Auth tokens

- Tokens in dedicated DataStore (`auth_prefs`).
- `AuthStorage` only type that reads/writes auth tokens.
- Repositories and ViewModels go through `AuthStorage`; no direct DataStore access.
- Token refresh in Ktor authorised client's `Auth` plugin (see `networking.md`).

## Migrations

- Preferences DataStore: use `SharedPreferencesMigration` once during legacy-shedding pass; one-line comment when retiring old key.
- Proto DataStore: bump schema version, write migration closure mapping old → new defaults.

## Disk I/O hygiene

- All DataStore reads/writes `suspend` — already off main thread.
- File caches under `context.cacheDir` (OkHttp / Ktor already use this); never `filesDir` unless data must survive cache eviction.
- No `runBlocking { dataStore.data.first() }` in production. Read through Flow.

## Quick checklist

- [ ] DataStore (not SharedPreferences) for new preferences
- [ ] Keys typed via `*PreferencesKey` helpers
- [ ] Reads return `Flow<T>`, writes are `suspend`
- [ ] Store wired as `single { }` in Koin
- [ ] Auth tokens go through `AuthStorage`, never DataStore directly
- [ ] No `runBlocking { … .first() }` in production code paths