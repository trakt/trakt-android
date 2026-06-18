Before implementing anything, identify which domain you are working in and
actively apply the corresponding rule file for that domain:

- Compose screens, composables, theme (`app/`, `tv/`, `common/.../ui/`):
  apply architecture.md, state-management.md, theming.md
- Networking, Ktor client, OpenAPI mappers (`common/.../networking/`,
  generated OpenAPI sources): apply networking.md
- Persistence (DataStore, Room entities, file caches): apply persistence.md
- Localization (`resources/src/main/res/values*/strings.xml`,
  `ValidateStringPlaceholdersTask`): apply localization.md
- Gradle module boundaries, `build.gradle.kts` edits, version catalog
  (`gradle/libs.versions.toml`), new external deps: apply packages.md
- All other source code: apply project.md, code-principles.md, and
  implementation.md (always-on baseline).

All rule files are referenced below.

@.agents/rules/project.md

@.agents/rules/code-principles.md

@.agents/rules/implementation.md

@.agents/rules/architecture.md

@.agents/rules/state-management.md

@.agents/rules/networking.md

@.agents/rules/persistence.md

@.agents/rules/localization.md

@.agents/rules/theming.md

@.agents/rules/packages.md

@.agents/rules/commits.md
