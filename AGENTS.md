Before implementing anything, identify which domain you are working in and
read the matching rule file from `.agents/rules/` with the Read tool. Only the
baseline below loads automatically; domain rules load on demand so every
session does not pay for all of them.

- Do not spawn subagents unless explicitly requested.
- Max subagents per task: 1.
- Do not recurse into deeper agents (depth = 0).

Domain rules (read every file whose area or paths the task touches). Claude
Code also loads them automatically through `.claude/rules/` when it reads a
file matching a rule's `paths` header, but that only covers file paths, not
areas such as `analytics.*` calls, so still read by area:

- Any Kotlin file (`**/*.kt`): layers, repositories, DI, navigation,
  background sync: `architecture.md`
- Kotlin under `app/`, `tv/` or `common/` `src/main/`: Compose-side state:
  `state-management.md`
- Compose UI in any module (any `@Composable`: screens, `views/`,
  `components/`, `**/ui/**/*.kt`, TV screens): `theming.md`,
  `localization.md`
- Theme code (`**/theme/**/*.kt`, `*Theme.kt`): `theming.md`
- ViewModels and screen state types (`*ViewModel.kt`, `*State.kt`):
  `viewmodel.md`
- `common/src/main/**/*.kt`: Ktor client, OpenAPI mappers, DataStore, Room,
  file caches: `networking.md`, `persistence.md`
- Repositories, DataStore, local sources and caches in any module (`app/`,
  `tv/`, feature `data/` folders): `networking.md`, `persistence.md`
- Koin modules that bind local sources or DataStores (feature `di/`
  folders, `TraktApplication`, `TvActivity`): `persistence.md`
- OpenAPI spec and generated client (`openapi/openapi.json`,
  `build/generate-resources/`, `./gradlew openApiGenerate`): `networking.md`
- Strings and resources (`resources/src/main/res/**`, `strings.xml`,
  `ValidateStringPlaceholdersTask`): `localization.md`
- Gradle (`build.gradle.kts`, `settings.gradle.kts`,
  `gradle/libs.versions.toml`, `buildSrc/`, `build-logic/`), new external
  deps: `packages.md`
- Analytics events, screen views, the `Analytics` interface and backends
  (`common/.../firebase/analytics/`, any `analytics.*` call): `analytics.md`

Re-read a domain file after a long gap if the context was compacted.

Always-on baseline (all source code, every commit and PR):

@.agents/rules/project.md

@.agents/rules/code-principles.md

@.agents/rules/implementation.md

@.agents/rules/commits.md
