# common — Claude instructions

Part of the `trakt-android` monorepo. Root rules apply — see
`../AGENTS.md`.

## Overlay for this module

- **Role**: bottom of the dependency graph. Domain models, Ktor /
  OpenAPI networking, repositories, Koin modules, design-system
  primitives (`TraktTheme`), Firebase wrappers, helpers.
- **Plugin**: `com.android.library` + `kotlin.compose`
  + `kotlin.serialization`.
- **Hard rule**: never imports from `:app` or `:tv`. If a type belongs
  here, it must compile in isolation against external deps and
  `:resources`.
- **Active areas (new code preferred here)**:
  - `common/.../networking/` — `KtorClientFactory`, interceptors,
    mappers (`<entity>Mapper.kt`).
  - `common/.../model/` — domain types. `@Immutable` data classes,
    `Sendable`-friendly (listed in `compose-stability.conf`).
  - `common/.../<entity>/` — repositories, use-cases, entity caches.
  - `common/.../ui/` — cross-platform Compose primitives.
  - `common/.../di/` — Koin modules wired in `TraktApplication`.
  - `common/.../firebase/` — Crashlytics, Analytics, Remote Config
    wrappers.
- **Generated sources** at `build/generate-resources/...` come from
  the OpenAPI generator — never hand-edited. ktlint already excludes
  them via `.editorconfig`.
- **State**: domain types are immutable. State containers expose
  `Flow<T>` reads and `suspend fun` writes. Local-write-first
  repository contract per `networking.md`.
- **Testing**: `common/src/test/.../fakes/` and `.../fixtures/` for
  fakes and `.fixture(...)` factories. No mocking libraries.

@../AGENTS.md
