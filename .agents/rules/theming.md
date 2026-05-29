---
trigger: glob
globs: '{**/ui/**/*.kt,**/theme/**/*.kt,**/*Theme.kt}'
description: 'TraktTheme tokens, MaterialTheme overlay, TraktSpacing / TraktTypography / TraktSize / TraktRadius. Coil 3 specifics. TV adaptations.'
applyTo: '{**/ui/**/*.kt,**/theme/**/*.kt,**/*Theme.kt}'
---

# Theming & Design Tokens

## TraktTheme

Design system exposed through `TraktTheme` (in `common/.../ui/theme/Theme.kt`). Four token
namespaces:

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

Use via `TraktTheme.<namespace>.<token>` inside composables. Each namespace = data class of `Color`,
`TextStyle`, or `Dp` values.

## Colours

All colours via `TraktTheme.colors.*` in general.
For one time shots a direct color is allowed if it makes sense for bot Light and Dark themes, e.g.
`Color.White` or `Color.Transparent`. For example:

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
- `MaterialTheme.colorScheme.primary` direct reads (use Trakt token). Material 3 colour scheme wired
  underneath; consumers go through `TraktTheme.colors`.
- `colorResource(R.color.…)` for design-system colours. Colour resources OK for legacy values also
  referenced from XML (notifications, app icon); new tokens live in theme.

Need colour not in palette → **add token** to `TraktColors` with light/dark/seasonal variants. Never
use literal at call site.

## Seasonal themes

- Halloween (orange), Christmas (red), other overrides flow through Firebase Remote Config +
  `CustomThemeUseCase`.
- Theme switching wires at app root (`TraktTheme(colors = customColors ?: DefaultColors) { … }`).
- Feature code doesn't branch on season — reads `TraktTheme.colors.*`, active palette swaps
  automatically.

## Typography

`TraktTheme.typography.*` exposes semantic styles:

```kotlin
Text(title, style = TraktTheme.typography.title)
Text(body, style = TraktTheme.typography.body)
Text(tag, style = TraktTheme.typography.tag)
```

- New text uses semantic styles; no inline `TextStyle(fontSize = 14.sp)` literals.
- Honour Dynamic Type — Material 3 picks up system font scale by default. Don't fight with absolute
  pixel sizes.

## Spacing

`TraktTheme.spacing.*` adaptive — values vary by window size class (phone / tablet / TV). Use named
scale:

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(TraktTheme.spacing.md)) { … }
Modifier.padding(horizontal = TraktTheme.spacing.lg)
```

- **No magic numbers** (`Modifier.padding(16.dp)`). Use token, or — if value genuinely unique to one
  composable — declare as private `val` with one-line reason.
- **Explicit arrangement spacing.** `Column`/`Row` declare
  `verticalArrangement = Arrangement.spacedBy(...)` / `horizontalArrangement = ...` rather than
  spacing items with `Spacer(Modifier.height(...))`.

## Sizes

`TraktTheme.size.*` exposes adaptive sizes for icons, avatars, cards, hero artwork. Same rules as
spacing — use tokens, don't hard-code `64.dp`.

## Material 3 vs TraktTheme

- Codebase wraps Material 3 at root via `MaterialTheme(...)`. Trakt tokens layer on top.
- Material 3 components (`Button`, `Card`, `TextField`) welcome — pass colours and shapes from
  `TraktTheme.*`, not raw values.

## Images & icons

- Bitmaps and remote images via **Coil 3** (`io.coil-kt.coil3`) + `AsyncImage` /
  `SubcomposeAsyncImage`.
- Local drawables: declare under `resources/.../drawable/`, reference via `R.drawable.<name>`.
  `Painter` via `painterResource(R.drawable...)`.
- Material 3 `Icons.Filled.*` / `Icons.Rounded.*` OK for generic glyphs (back arrow, more, search).
  Reserve custom Trakt icons for branded assets.
- **Forbidden in new code:** Glide, Fresco, Picasso. Stick with Coil.

### Coil 3 specifics

- Prefer **`SubcomposeAsyncImage`** when composable needs `Loading` / `Error` / `Success` state
  branches; use `AsyncImage` for common no-branch case.
- **Image request keys must be stable strings.** Never `UUID.randomUUID()` or
  `System.currentTimeMillis()` inside request — busts cache on every recomposition.
- Shared `ImageLoader` wired once in Koin as `single { }`. Custom Coil interceptors (Trakt CDN URL
  normalisation, fallback-host swaps, accept-header pinning) live in `common/.../networking/coil/`
  and attach there. Never construct fresh `ImageLoader` per call site.
- Coil 3 uses **Ktor 3 engine adapter** (`coil-network-ktor3`); reuse existing `HttpClient`, don't
  spin up parallel OkHttp instance.

## TV-specific theming

- TV module has own `TraktTheme` wrapper using `androidx.tv.material3.MaterialTheme`.
- Focus styling uses Compose TV built-in focus indicators; don't hand-roll focus rings.
- Spacing tokens in TV `TraktSpacing` scaled up from phone values — use same semantic name (`md`,
  `lg`), let token carry platform difference.

## Accessibility

- Touch targets ≥ 48dp on phone, ≥ focus-safe size on TV.
- `Modifier.semantics { contentDescription = "…" }` for non-text composables.
- Animations respect reduced motion when system preferences indicate it.
- Contrast: WCAG AA. Token palette enforces this; don't fight with one-off colours.

## Quick checklist

- [ ] Colours via `TraktTheme.colors.*` — no raw `Color(...)` literals
- [ ] Spacing via `TraktTheme.spacing.*` — no `padding(16.dp)`
- [ ] Sizes via `TraktTheme.size.*` — no `Modifier.size(64.dp)` magic
- [ ] Typography via `TraktTheme.typography.*` — no inline `TextStyle`
- [ ] Images via Coil 3 + `R.drawable.*`, never Glide/Fresco/Picasso
- [ ] Light / dark / seasonal variants exist for every new token
- [ ] Animations honour reduced-motion
