---
trigger: glob
globs: 'common/src/main/java/tv/trakt/trakt/common/firebase/analytics/**'
description: 'Analytics interface, Firebase + HAL backends, screen views, user action events, HAL parity with trakt-web, event allowlist, privacy.'
applyTo: 'common/src/main/java/tv/trakt/trakt/common/firebase/analytics/**'
---

# Analytics

> **TL;DR**: Features depend only on the `Analytics` interface, injected through the constructor.
> Release builds send every call to Firebase Analytics and HAL in parallel. Debug builds only log
> through Timber. A new event is added to the interface and implemented in all three backends.
> HAL events must match trakt-web exactly: same event names, dims, metrics, values and routes.

## Web parity (HAL)

HAL is shared with trakt-web (local checkout: `~/Documents/Android/trakt/trakt-web`). Android and
web events must land in the same HAL rows, so **any HAL work starts by checking the web repo**:

- **Pull latest web `main` first.** Before any comparison or sync, update the checkout:
  `git -C ~/Documents/Android/trakt/trakt-web checkout main && git -C ~/Documents/Android/trakt/trakt-web pull --ff-only`.
  A stale checkout hides new or renamed events. If the checkout has local changes or is on another
  branch, stop and ask the user instead of discarding work.

- Source of truth on web: `projects/client/src/lib/features/analytics/` -
  `events/AnalyticsEvent.ts` (event names), `events/AnalyticsEventDataMap.ts` (dims, metrics and
  allowed values), `PageView.svelte` (page view routes).
- **Event names**: reuse the web event name verbatim. Never invent an Android-only name for an
  action web already tracks.
- **Dims and metrics**: same keys, same value strings (casing, enum values, `source` names), same
  types (dims are strings, metrics are `Long`).
- **Trigger point**: fire at the same moment web does (after success, on open, on checkout start).
- **Routes**: page views use the web SvelteKit route template via `HalRoutes.kt`.
- Web has no equivalent: confirm with the user before adding an Android-only event or dim, then
  add it to trakt-workers `EVENT_CONTRACTS.ts` (see step 3 of "Adding an event").
- Touching an existing HAL event: re-check it against web and fix any drift in the same change.

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
    ├── HalAnalytics.kt         # top-level Analytics calls to HAL events
    ├── HalAnalyticsGroups.kt   # grouped interfaces (ratings, lists, today, ...)
    ├── HalContract.kt          # HAL event names, dim/metric keys, value normalisers
    ├── HalRoutes.kt            # screenName to web route template map
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
- HAL tracks signed-in users only. Events are dropped until the loaded profile sets
  `setUserProperty("vip", "true" | "false")`. Logout flows must call
  `setUserProperty("vip", null)` to stop tracking.

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
- HAL sends a route, not the name. `HalRoutes.kt` maps screens shared with trakt-web to the web
  SvelteKit route template (`movie_details` to `/movies/[slug]`), so page views line up across
  platforms. Android-only screens fall back to `/<screenName>`.
- New screen with a web equivalent: add it to `HalRoutes.kt`. Never send a resolved path with real
  ids or slugs.
- Renaming a screen breaks its history in both backends.

### User actions

- Log **after the write succeeds**, not on tap. Workers log once the API call returns (see
  `PostRatingWorker`, `PostReactionWorker`). ViewModels log inside the success branch.
  - Exception: `logVipUpgrade` fires when the Play billing flow opens, matching web, which tracks
    checkout start.
- Use the grouped interfaces: `analytics.progress`, `analytics.ratings`, `analytics.reactions`,
  `analytics.comments`, `analytics.lists`, `analytics.today`, `analytics.trivia`,
  `analytics.playback`.
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
     be ≤ 40 characters. Add a `require` check in `init` for new long constants. Name new events
     `<area>_<action>` in snake_case (`lists_item_add`, `today_close`).
   - `HalAnalytics` / `HalAnalyticsGroups`: use the constants in `HalContract.kt`. Reuse the web
     event name and dim values from trakt-web `AnalyticsEvent.ts` / `AnalyticsEventDataMap.ts`
     so both platforms land in the same rows. Dims are strings, metrics are `Long`. Implement as
     `= Unit` if HAL should not track it.
   - `CompositeAnalytics`: forward to every target. The compiler flags a missing override.
3. **HAL allowlist first.** HAL refuses the whole batch on an unknown event name, and drops dims
   or metrics not allowlisted for that event. Any name, dim or metric not yet in trakt-workers
   `src/core/analytics/EVENT_CONTRACTS.ts` (metrics also need `METRIC_SLOTS.ts`) must be added and
   deployed there **before** the Android release that emits it.
4. Call it from the ViewModel, use-case or worker following the rules above.

## Quick checklist

- [ ] `Analytics` injected via constructor, no backend types in feature code
- [ ] Logged from ViewModel / use-case / worker, not a composable
- [ ] Action events logged after success, not on tap
- [ ] `screenName` lowercase `snake_case`, logged once in `init`
- [ ] No PII in parameters
- [ ] New event implemented in Debug, Firebase, HAL and Composite
- [ ] HAL event name, dims, metrics and values match trakt-web `AnalyticsEvent.ts` /
  `AnalyticsEventDataMap.ts`
- [ ] HAL event name and dim keys already in trakt-workers `EVENT_CONTRACTS.ts`
- [ ] Screen shared with web mapped in `HalRoutes.kt`
- [ ] Firebase event name ≤ 40 characters including `av3_`
