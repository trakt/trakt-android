---
trigger: glob
globs: '{app/src/main/java/tv/trakt/trakt/core/featureflags/**,app/src/main/java/tv/trakt/trakt/core/settings/features/preview/**}'
description: 'Preview features: FeatureFlag enum, Remote Config audience (off / director / vip / public), opt-in toggles in Settings, gating a feature through FeatureFlagsUseCase, rollout and removal.'
applyTo: '{app/src/main/java/tv/trakt/trakt/core/featureflags/**,app/src/main/java/tv/trakt/trakt/core/settings/features/preview/**}'
paths:
  - 'app/src/main/java/tv/trakt/trakt/core/featureflags/**/*'
  - 'app/src/main/java/tv/trakt/trakt/core/settings/features/preview/**/*'
---

# Feature Flags (Preview Features)

> **TL;DR**: A feature in preview is one `FeatureFlag` enum entry. The Remote Config string
> `preview_feature_<id>` sets its audience: `off`, `director`, `vip` or `public`. Users in the
> audience opt in under Settings, Preview Features (default off). `public` turns it on for everyone
> without an app update. Feature code asks `FeatureFlagsUseCase.observeEnabled(flag)` and nothing
> else. Phone app only.

## Where it lives

```
app/.../core/featureflags/
├── model/FeatureFlag.kt            # the flag list: id, title, description
├── model/FeatureFlagAudience.kt    # off / director / vip / public, parsed from Remote Config
├── model/FeatureFlagPreview.kt     # one row of the Preview Features screen: flag, audience, toggle
├── usecases/FeatureFlagsUseCase.kt # the only place that resolves a flag
└── di/FeatureFlagsModule.kt
app/.../core/settings/features/preview/   # Preview Features screen, state, ViewModel
app/.../ui/components/AttentionIcon.kt    # animated icon for unseen preview features
```

## How a flag resolves

| Remote Config value        | Feature is on for                 | Shown in Preview Features |
| -------------------------- | --------------------------------- | ------------------------- |
| `off`, missing or unknown  | nobody                            | no                        |
| `director`                 | directors who switched it on      | directors                 |
| `vip`                      | VIPs and directors who switched it on | every signed-in user; locked for free users |
| `public`                   | everyone, signed out included     | no                        |

- Preview toggles default to **off**. The user opts in.
- `public` ignores the stored toggle. A user who never opted in, or opted out, still gets it.
- Signed-out users only ever get `public` features.
- VIP means `user.isAnyVip`. Director means `user.isDirector`.

## Remote Config

- Key: `preview_feature_` + the flag `id` (`character_portraits` to
  `preview_feature_character_portraits`). Built by `FeatureFlag.remoteKey`.
- Type: **string**. Never a boolean.
- Do not add these keys to `common/src/main/res/xml/remote_config_defaults.xml`. A missing key must
  resolve to `off`.
- Values are read when a flag flow emits (profile or settings change, new collector). There is no
  live Remote Config listener: an open screen keeps its value until it is re-collected.
- Agents never create or change Remote Config parameters. Tell the user which key and value to set
  in the Firebase console.
- Plain Remote Config switches in `FirebaseConfig.RemoteKey` (`PLEX_PLAY_ENABLED`,
  `MOBILE_WELCOME_BANNER_ENABLED`) are not preview features. Use a `FeatureFlag` when a
  user-facing feature needs a staged audience; keep a plain key for images, URLs and kill switches.

## Adding a preview feature

1. Add an entry to `FeatureFlag`:

   ```kotlin
   CharacterPortraits(
       id = "character_portraits",
       title = R.string.preview_feature_title_character_portraits,
       description = R.string.preview_feature_description_character_portraits,
   ),
   ```

   - `id` is snake_case. When the feature exists on trakt-web, take the id from
     `projects/client/src/lib/features/feature-flag/models/FeatureFlag.ts` with dashes as
     underscores (`character-portraits` to `character_portraits`).
   - Strings are named `preview_feature_title_<name>` and `preview_feature_description_<name>`.
     Features that exist on web usually have both in `strings.xml` already. Missing keys go to
     `strings-todo.xml` (see `localization.md`).
2. Gate the feature (see below).
3. Report the Remote Config key and the value to start with (`director`).

The Preview Features screen builds its rows from the enum. A new flag needs no UI work there.

## Gating a feature

```kotlin
internal class CastViewModel(
    private val featureFlagsUseCase: FeatureFlagsUseCase,
) : ViewModel() {
    private val portraitsState = MutableStateFlow(initialState.portraits)

    private fun observePortraits() {
        featureFlagsUseCase.observeEnabled(FeatureFlag.CharacterPortraits)
            .onEach { enabled -> portraitsState.update { enabled } }
            .launchIn(viewModelScope)
    }
}
```

- Inject `FeatureFlagsUseCase` into the ViewModel or use case. `observeEnabled(flag)` for state,
  `isEnabled(flag)` for a one-shot check.
- The result lands in screen state as a plain `Boolean` field (see `viewmodel.md`). Composables
  receive it as a parameter and never call the use case.
- Never read `Firebase.remoteConfig` for a preview key, and never gate on `user.isDirector` or
  `user.isAnyVip` in feature code. Audience logic lives in `FeatureFlagsUseCase` only.
- With the flag off, the app behaves exactly as before the feature existed: no extra requests, no
  empty placeholders, no changed layout.
- `FeatureFlag` lives in `:app`. Code in `:common` cannot see it: resolve the flag in `:app` and
  pass the `Boolean` down.
- A flag hides UI on the device. It is not access control. Data that must stay restricted needs
  enforcement in the API.

## Rollout and removal

- Rollout is a Remote Config change only: `director`, then `vip` (optional), then `public`. Use
  `off` as the kill switch.
- Only app versions that contain the flag and the feature react. Ship both before changing the
  value.
- Never rename a released `id`. It is the Remote Config key, the stored toggle key and the entry
  in the seen set.
- Once a feature is `public` and stable, remove the enum entry and every gate, and ask the user to
  delete the Remote Config parameter. Do not leave dead flags behind.

## Settings UI

- The Preview Features row on the Settings root is its own section above the rate banner.
- Visible to directors always, to everyone else only when at least one feature is listed for
  them (`observePreviewsVisible()`). A `vip` feature is listed for free users too.
- Free users see `vip` features as locked rows (`FeatureFlagPreview.locked`, switch disabled) with
  a `VipBanner` that opens billing with `VipUpsellSource.PreviewFeatures`. A locked feature is
  never enabled, whatever is stored.
- The screen groups features by audience: a "VIP" group and a "Director" group. VIP and free
  users see only the first. A director sees both, so it stays clear which previews paying users
  already have.
- A feature the user has not seen yet makes the row icon use `AttentionIcon`
  (`observePreviewsUnseen()`). Opening the Preview Features screen marks all available features as
  seen (`markPreviewsSeen()`).

## Storage

In the settings DataStore (`SETTINGS_PREFERENCES`), per device, not cleared on logout:

- `key_preview_feature_enabled_<id>`: the user's toggle.
- `key_preview_features_seen`: ids of features already shown to the user.

Only `FeatureFlagsUseCase` reads or writes these keys.

## Not covered

- Android TV has no flag system. Do not gate TV code on `FeatureFlag`.
- Toggling a preview feature is not tracked in analytics. Web tracks it as
  `user-action.preview-feature`; add it per `analytics.md` only when asked.

## Quick checklist

- [ ] One `FeatureFlag` entry, `id` in snake_case, matching trakt-web when the feature exists there
- [ ] Title and description from `preview_feature_title_*` / `preview_feature_description_*`
- [ ] Gate through `FeatureFlagsUseCase` in a ViewModel or use case, `Boolean` in state
- [ ] No direct `Firebase.remoteConfig`, `isDirector` or `isAnyVip` checks for gating
- [ ] Flag off leaves existing behaviour untouched
- [ ] No entry in `remote_config_defaults.xml`
- [ ] User told the key `preview_feature_<id>` and the starting value
- [ ] Released `id` never renamed; public and stable flags removed
