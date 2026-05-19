---
trigger: glob
globs: '{**/ui/**/*.kt,**/theme/**/*.kt,**/*Theme.kt}'
description: 'TraktTheme tokens, MaterialTheme overlay, TraktSpacing / TraktTypography / TraktSize / TraktRadius. Coil 3 specifics. TV adaptations.'
applyTo: '{**/ui/**/*.kt,**/theme/**/*.kt,**/*Theme.kt}'
---

# Theming & Design Tokens

## TraktTheme

The design system is exposed through `TraktTheme` (in
`common/.../ui/theme/Theme.kt`). It owns four token namespaces:

```kotlin
@Composable
fun TraktTheme(
    colors: TraktColors = TraktTheme.colors,
    typography: TraktTypography = TraktTheme.typography,
    spacing: TraktSpacing = TraktTheme.spacing,
    size: TraktSize = TraktTheme.size,
    content: @Composable () -> Unit,
)

object TraktTheme {
    val colors: TraktColors @Composable @ReadOnlyComposable get() = LocalTraktColors.current
    val typography: TraktTypography @Composable @ReadOnlyComposable get() = LocalTraktTypography.current
    val spacing: TraktSpacing @Composable @ReadOnlyComposable get() = LocalTraktSpacing.current
    val size: TraktSize @Composable @ReadOnlyComposable get() = LocalTraktSize.current
}
```

Use through `TraktTheme.<namespace>.<token>` inside composables. Each
namespace is a data class of `Color`, `TextStyle`, or `Dp` values.

## Colours

All colours referenced via `TraktTheme.colors.*`:

```kotlin
Text(
    text = title,
    color = TraktTheme.colors.textPrimary,
)

Surface(
    color = TraktTheme.colors.background,
) { … }
```

**Forbidden in new code:**

- Raw `Color(0xFF7B68EE)` / `Color(red = …, green = …, blue = …)`.
- `MaterialTheme.colorScheme.primary` direct reads (use Trakt token).
  Material 3 colour scheme is wired underneath; consumers go through
  `TraktTheme.colors`.
- `colorResource(R.color.…)` for design-system colours. Colour
  resources are fine for legacy values that are also referenced from
  XML (notifications, app icon) but new tokens live in the theme.

If a colour you need isn't in the palette, **add a token** to
`TraktColors` with light/dark/seasonal variants. Never paper over with
a literal at the call site.

## Seasonal themes

- Halloween (orange), Christmas (red), and other overrides flow
  through Firebase Remote Config + `CustomThemeUseCase`.
- Theme switching wires up at the app root
  (`TraktTheme(colors = customColors ?: DefaultColors) { … }`).
- Feature code doesn't branch on season — it reads
  `TraktTheme.colors.*` and the active palette swaps out automatically.

## Typography

`TraktTheme.typography.*` exposes semantic styles:

```kotlin
Text(title, style = TraktTheme.typography.title)
Text(body,  style = TraktTheme.typography.body)
Text(tag,   style = TraktTheme.typography.tag)
```

- New text uses semantic styles; no inline `TextStyle(fontSize = 14.sp)`
  literals.
- Honour Dynamic Type — Material 3 picks up the system font scale by
  default. Don't fight it with absolute pixel sizes.

## Spacing

`TraktTheme.spacing.*` is adaptive — values vary by window size class
(phone / tablet / TV). Use the named scale:

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(TraktTheme.spacing.md)) { … }
Modifier.padding(horizontal = TraktTheme.spacing.lg)
```

- **No magic numbers** (`Modifier.padding(16.dp)`). Either use a
  token, or — if the value is genuinely unique to one composable —
  declare it as a private `val` with a one-line reason.
- **Explicit arrangement spacing.** `Column`/`Row` declare
  `verticalArrangement = Arrangement.spacedBy(...)` /
  `horizontalArrangement = ...` rather than spacing items with
  `Spacer(Modifier.height(...))`.

## Sizes

`TraktTheme.size.*` exposes adaptive sizes for icons, avatars, cards,
hero artwork. Same rules as spacing — use tokens, don't hard-code
`64.dp`.

## Material 3 vs TraktTheme

- The codebase wraps Material 3 at the root via `MaterialTheme(...)`.
  Trakt tokens layer on top.
- Material 3 components (`Button`, `Card`, `TextField`) are welcome —
  pass colours and shapes from `TraktTheme.*` rather than overriding
  with raw values.

## Images & icons

- Bitmaps and remote images via **Coil 3** (`io.coil-kt.coil3`) +
  `AsyncImage` / `SubcomposeAsyncImage`.
- Local drawables: declare under `resources/.../drawable/` and reference
  via `R.drawable.<name>`. `Painter` via `painterResource(R.drawable...)`.
- Material 3 `Icons.Filled.*` / `Icons.Rounded.*` are acceptable for
  generic glyphs (back arrow, more, search). Reserve custom Trakt icons
  for branded assets.
- **Forbidden in new code:** Glide, Fresco, Picasso. Stick with Coil.

### Coil 3 specifics

- Prefer **`SubcomposeAsyncImage`** when the same composable needs
  `Loading` / `Error` / `Success` state branches; reach for
  `AsyncImage` for the common no-branch case.
- **Image request keys must be stable strings.** Never
  `UUID.randomUUID()` or `System.currentTimeMillis()` inside a
  request — they bust the cache on every recomposition.
- The shared `ImageLoader` is wired once in Koin as a `single { }`.
  Custom Coil interceptors (Trakt CDN URL normalisation, fallback-host
  swaps, accept-header pinning) live in
  `common/.../networking/coil/` and attach there. Never construct a
  fresh `ImageLoader` per call site.
- Coil 3 uses the **Ktor 3 engine adapter** (`coil-network-ktor3`);
  reuse the existing `HttpClient` rather than spinning up a parallel
  OkHttp instance.

## TV-specific theming

- TV module has its own `TraktTheme` wrapper using
  `androidx.tv.material3.MaterialTheme`.
- Focus styling uses Compose TV's built-in focus indicators; avoid
  hand-rolling focus rings.
- Spacing tokens in the TV `TraktSpacing` are scaled up from phone
  values — use the same semantic name (`md`, `lg`) and let the token
  carry the platform difference.

## Accessibility

- Touch targets ≥ 48dp on phone, ≥ focus-safe size on TV.
- `Modifier.semantics { contentDescription = "…" }` for non-text
  composables.
- Animations respect reduced motion when system preferences indicate
  it.
- Contrast: WCAG AA. The token palette enforces this; don't fight it
  with one-off colours.

## Quick checklist

- [ ] Colours via `TraktTheme.colors.*` — no raw `Color(...)` literals
- [ ] Spacing via `TraktTheme.spacing.*` — no `padding(16.dp)`
- [ ] Sizes via `TraktTheme.size.*` — no `Modifier.size(64.dp)` magic
- [ ] Typography via `TraktTheme.typography.*` — no inline `TextStyle`
- [ ] Images via Coil 3 + `R.drawable.*`, never Glide/Fresco/Picasso
- [ ] Light / dark / seasonal variants exist for every new token
- [ ] Animations honour reduced-motion
