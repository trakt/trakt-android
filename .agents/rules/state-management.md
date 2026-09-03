---
trigger: glob
globs: '{app,tv,common}/src/main/**/*.kt'
description: 'Composable-side state: remember / rememberSaveable, bottom sheets, composition locals, side effects, Compose stability. ViewModel state lives in viewmodel.md.'
applyTo: '{app,tv,common}/src/main/**/*.kt'
---

# State Management

## What lives where

| Lifetime / scope                                     | Tool                                                   |
| ---------------------------------------------------- | ------------------------------------------------------ |
| Survives config change, drives screen UI             | `StateFlow<UiState>` on a `ViewModel`                  |
| Composable-local UI state, dies on dispose           | `var x by remember { mutableStateOf(value) }`          |
| Composable-local UI state, survives config change    | `var x by rememberSaveable { mutableStateOf(value) }`  |
| App-wide ambient UI state                            | `staticCompositionLocalOf` + `CompositionLocalProvider`|
| Derived value reactive to other state                | `val x by remember(...) { derivedStateOf { … } }`      |
| Side effects keyed to recomposition                  | `LaunchedEffect(key)`, `DisposableEffect(key)`         |

## ViewModel state and one-shot events

**See `viewmodel.md`** — it is the canonical home for ViewModel state shape
(single untyped `state` declared last, indexed-cast `combine` + `stateIn` with
`WhileSubscribed(5_000)`), the no-`mutableStateOf-in-ViewModel` rule, one private
`MutableStateFlow` per state field, and `SharedFlow(replay = 0)` one-shot events.

This file covers everything on the composable side of the boundary: local
state, sheets, composition locals, side effects, stability.

## Composable-local state

`remember { mutableStateOf(...) }` for state that:

- Owned by single composable instance.
- Doesn't survive config change.
- Doesn't need testing without Compose runtime.

Qualifies: sheet visibility, scroll position, dropdown expanded state, text field input committed elsewhere on "Done", focus management.

Use `rememberSaveable` when state survives config change but not screen leaving back stack.

## Bottom sheets

Sheets wrap `TraktBottomSheet` (`app/.../ui/components/TraktBottomSheet.kt`) and follow the
`StreaksSheet` / `AllRatingsSheet` pattern: caller owns a `visible` boolean
(`remember { mutableStateOf(false) }`), sheet composable takes `visible` + `onDismiss` and renders
only when visible.

Sheet state comes from `rememberBottomSheetState` with explicit values:

```kotlin
@Composable
internal fun FeatureSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (visible) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) { /* content */ }
    }
}
```

Rules:

- **`rememberBottomSheetState(initialValue = Hidden, enabledValues = setOf(Hidden, Expanded))`**
  is the standard for new sheets. Do **not** use
  `rememberModalBottomSheetState(skipPartiallyExpanded = true)` in new code — legacy call sites
  migrate as they're touched.
- Sheet content lives in a separate stateless `<Feature>View` composable (previewable); the sheet
  file only hosts `TraktBottomSheet` wiring.
- Sheet visibility is transient UI state — `remember { mutableStateOf(false) }` at the caller, not
  in the ViewModel.

### Sheet-scoped ViewModel keys

Sheets that want a fresh ViewModel per open pass a random `key` to `koinViewModel(...)`. That key
**must be remembered**, keyed on the sheet's subject item(s):

```kotlin
val viewModelKey = remember(movie) { nextInt().toString() }

MovieDetailsListsView(
    viewModel = koinViewModel(
        key = viewModelKey,
        parameters = { parametersOf(movie) },
    ),
)
```

- **Never inline the key**: `koinViewModel(key = nextInt().toString(), …)` generates a new key on
  every recomposition — any parent state change silently spawns a brand-new ViewModel mid-flight,
  re-running `init` loads and wiping in-progress UI state (e.g. an optimistic toggle reverting when
  another ViewModel reacts to the change and recomposes the screen).
- `remember(item) { nextInt().toString() }` keeps the key stable while the sheet is open, and
  regenerates it per open: the item goes `null` on dismiss, so the `remember` key changes on the
  next open and a fresh ViewModel is created.
- Multiple subjects → all of them are remember keys: `remember(show, episode) { … }`.

## CompositionLocal

App-wide ambient UI state already wired:

- `LocalBottomBarVisibility` — driven by feature flags.
- `LocalSnackbarState` — global snackbar host accessor.
- `LocalCheckInVisibility`, `LocalRatePromptVisibility` — overlay visibility.

Add new locals **only** when:

- Value genuinely app-level (not single feature).
- Threading through every composable param list impractical.
- Value provided once at app root.

Use `staticCompositionLocalOf` when value constant for provider lifetime; `compositionLocalOf` when it changes during recomposition.

## Side effects

- `LaunchedEffect(key) { … }` — coroutine tied to composition lifetime.
- `DisposableEffect(key) { … onDispose { … } }` — cleanup on dispose.
- `SideEffect { … }` — sync side effect after every successful composition. Rare; usually want `LaunchedEffect`.
- **Never** start coroutine from inside `body { }` directly.
- **Never** read `mutableStateOf` from outside composition without `snapshotFlow`.

## Compose stability

Rules:

- **`@Immutable`** for data classes used as state where every public property is `val` pointing at deeply immutable values.
- **`@Stable`** for types with observable properties but stable equality/hash — rare; only when `@Immutable` would be lie.
- **`ImmutableList<T>` / `PersistentList<T>`** from `kotlinx.collections.immutable` for collections in state. Plain `List<T>` unstable to Compose.
- **Triage recomposition regressions with compose-compiler metrics.** Run `./gradlew :app:assembleDebug -Pcompose.metrics=true -Pcompose.reports=true`, commit report under `docs/compose-metrics/<bom-version>/` when investigating hot screen.

## Persistence beyond a ViewModel

State outliving screen (auth tokens, preferences, sync watermarks): write to DataStore. ViewModel reads through repository exposing `Flow<Preferences>`.

See `persistence.md` for data layer rules.
