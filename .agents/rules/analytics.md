# Analytics

> **TL;DR**: Features depend only on the `Analytics` interface, injected through the constructor.
> Release builds send every call to Firebase Analytics and HAL in parallel. Debug builds only log
> through Timber. A new event is added to the interface and implemented in all three backends.

## Pipeline

All code lives in `common/.../firebase/analytics/`:

```
Analytics.kt                    # public interface, the only type features touch
di/AnalyticsModule.kt           # Koin wiring, picks Debug vs Composite
implementation/
├── CompositeAnalytics.kt       # forwards every call to each target
├── debug/DebugAnalytics.kt     # Timber only, nothing leaves the device
├── firebase/FirebaseAnalytics.kt
└── hal/
    ├── HalAnalytics.kt         # maps Analytics calls to HAL events + dims
    ├── HalEngine.kt            # stamps time + user_type, queues, schedules send
    └── data/
        ├── HalEvent.kt         # event model + batch JSON encoding
        ├── HalEventStore.kt    # in-memory FIFO queue, capped at 500 events
        ├── remote/             # KtorHalSender, User-Agent builder
        └── work/HalSendWorker.kt
```

- `single<Analytics>` is `DebugAnalytics` when `BuildConfig.DEBUG`, otherwise
  `CompositeAnalytics(listOf(FirebaseAnalytics, HalAnalytics))`.
- HAL delivery: `HalEngine.track` adds to `HalEventStore`, then enqueues the unique `hal_send`
  `HalSendWorker` (`ExistingWorkPolicy.KEEP`, 10s initial delay, network required). The worker
  posts batches of 50 to `https://hal.trakt.tv/e`.
  - Rejected batches (4xx except 429) are dropped.
  - 429, 5xx and network errors retry with exponential backoff, up to 3 attempts.
- HAL events live in memory only. Losing queued events on process death is accepted.
- HAL is first-party and sends no user id or device id. `setUserId` is a no-op for HAL. Only the
  `vip` user property reaches HAL, as the `user_type` dim.

## Calling analytics

- **Constructor injection** of `Analytics` into ViewModels, use-cases and workers. No
  `koinInject<Analytics>()` in composables, no `GlobalContext` lookups.
- **Never call a backend directly.** No `Firebase.analytics`, `HalEngine` or `HalSendWorker`
  usage outside `firebase/analytics/`.
- Calls are fire-and-forget and non-suspending. Don't wrap them in `launch` or `try`.
- Side effects at the edges: log from ViewModels, use-cases or workers, never from composables,
  mappers or state types.

### Screen views

Log once per screen from the ViewModel `init`:

```kotlin
init {
    loadData()

    analytics.logScreenView(
        screenName = "movie_details",
    )
}
```

- `screenName` is lowercase `snake_case`: `home`, `show_details`, `all_watchlist`.
- "All" list screens use the `all_<section>` prefix.
- HAL derives the route from the name (`/movie_details`), so renaming a screen breaks its history
  in both backends.

### User actions

- Log **after the write succeeds**, not on tap. Workers log once the API call returns (see
  `PostRatingWorker`, `PostReactionWorker`). ViewModels log inside the success branch.
- Use the grouped interfaces: `analytics.progress`, `analytics.ratings`, `analytics.reactions`,
  `analytics.comments`, `analytics.trivia`, `analytics.playback`.
- Pass raw domain values (`mediaType.value`, `source`). Backends lowercase and normalise.

### User identity

- Login: `setUserId(traktId)`, `setUserProperty("vip", …)`, `setUserProperty("director", …)`.
- Logout: the same calls with `null` (see `LogoutUserUseCase`).

## Privacy

- Event parameters carry categories only: media type, source, action, rating value.
- **Never** pass titles, comment or list text, usernames, emails or other PII.
- Only `setUserId` carries an id, and it reaches Firebase and Crashlytics only.

## Adding an event

1. Add the method to `Analytics` (or its nested group) with a short KDoc.
2. Implement it in **every** backend:
   - `DebugAnalytics`: `Timber.d` with the parameters.
   - `FirebaseAnalytics`: event name built with `eventName(…)` (`av3_` prefix). The full name must
     be ≤ 40 characters. Add a `require` check in `init` for new long constants.
   - `HalAnalytics`: reuse the `EVENT_*` / `DIM_*` constants at the top of the file. New names
     follow the existing `<area>.<action>` kebab style (`media-action.rate`). Dims are strings,
     metrics are ints. Implement as `= Unit` if HAL should not track it.
   - `CompositeAnalytics`: forward to every target. The compiler flags a missing override.
3. Call it from the ViewModel, use-case or worker following the rules above.

## Quick checklist

- [ ] `Analytics` injected via constructor, no backend types in feature code
- [ ] Logged from ViewModel / use-case / worker, not a composable
- [ ] Action events logged after success, not on tap
- [ ] `screenName` lowercase `snake_case`, logged once in `init`
- [ ] No PII in parameters
- [ ] New event implemented in Debug, Firebase, HAL and Composite
- [ ] Firebase event name ≤ 40 characters including `av3_`
