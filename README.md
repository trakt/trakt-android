<div align="center">
  <img width="150" height="150" alt="Logo" src="https://github.com/user-attachments/assets/31a702ff-bbaa-485d-8f58-18aa9b80d984" />
  <h1>Trakt for Android</h1>
  <p><em>Track everything you watch, wherever you stream it.</em></p>
</div>

<p align="center">
  <a href="https://github.com/trakt/trakt-android/releases"><img src="https://img.shields.io/github/v/tag/trakt/trakt-android?label=version&style=flat" alt="Version"></a>
  <a href="https://github.com/trakt/trakt-android/commits"><img src="https://img.shields.io/github/commit-activity/m/trakt/trakt-android?style=flat" alt="Commits"></a>
  <a href="https://github.com/trakt/trakt-android/stargazers"><img src="https://img.shields.io/github/stars/trakt/trakt-android?style=flat" alt="Stars"></a>
  <a href="https://github.com/trakt/trakt-android/actions/workflows/master.yml"><img src="https://img.shields.io/github/actions/workflow/status/trakt/trakt-android/master.yml?branch=main&label=lint" alt="Lint"></a>
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=tv.trakt.trakt">Get it on Google Play</a>
</p>

<br>

<p align="center">
<img width="180" alt="image" src="https://github.com/user-attachments/assets/62829b44-c64c-40c7-9c50-3f2ae405df86" />
&nbsp;
<img width="180" alt="image" src="https://github.com/user-attachments/assets/7ab4cdcb-431c-43a3-8e02-5c5f50ce8d71" />
&nbsp;
<img width="180" alt="image" src="https://github.com/user-attachments/assets/9342e362-f6c3-4381-847d-fef490334179" />
&nbsp;
<img width="180" alt="image" src="https://github.com/user-attachments/assets/d38dfd52-0bee-4c4f-b559-d99693179df7" />
</p>

## Supported Form Factors

- Mobile
- Tablets
- Android TV

Requires Android 9 (API 28) or newer.

## Project Structure

| Module      | Purpose                                              |
| ----------- | ---------------------------------------------------- |
| `app`       | Phone and tablet app, entry point                    |
| `tv`        | Android TV app                                       |
| `common`    | Shared domain, networking (OpenAPI client), DI       |
| `resources` | Strings, drawables, translations                     |

Built with Kotlin, Jetpack Compose, Koin, Ktor and Coil.

## Getting Started

1. Install the latest stable [Android Studio](https://developer.android.com/studio) and JDK 17.
2. Clone this repo and open it.
3. Create `local.properties` in the root folder with the values below:
   ```properties
   TRAKT_API_KEY = "PUT_YOUR_VALUE_HERE"

   # Optional
   YOUNIFY_API_KEY = "PUT_YOUR_VALUE_HERE"
   KLIPY_API_KEY = "PUT_YOUR_VALUE_HERE"

   # Release signing. Required by Gradle even for debug builds,
   # so use any placeholder values if you only build debug.
   KEYSTORE_ALIAS = PUT_YOUR_VALUE_HERE
   KEYSTORE_PASSWORD = PUT_YOUR_VALUE_HERE
   KEYSTORE_KEY_PASSWORD = PUT_YOUR_VALUE_HERE
   ```
   API key values are injected into `BuildConfig` as-is, so keep the quotes.
4. Download `google-services.json` from your Firebase console project settings and put it in `/app/google-services.json`.
   The Firebase project needs apps for both `tv.trakt.trakt` and `tv.trakt.trakt.v3` (internal flavor).
5. Put `debug.keystore` and `keystore.jks` in the root folder (same level as `local.properties`).
   Both are gitignored. For debug builds you can copy your local Android debug keystore from `~/.android/debug.keystore`.

You are all set!

## Building

The app has two flavors: `playstore` and `internal` (installs side by side with the `.v3` package suffix).

```bash
./gradlew :app:assembleInternalDebug    # internal debug build
./gradlew :app:assemblePlaystoreDebug   # Play Store debug build
./gradlew ktlintCheck                   # lint, run before opening a PR
./gradlew ktlintFormat                  # auto-fix formatting
```

## Localisation 🌐

Want to help translating Trakt into your native language?
Have you spotted a mistake or an improvement?

Join the CrowdIn project: [Translate Trakt](https://crwd.in/trakt-poc/3857d5ea667dd425fbd0cb2e4e80dc192749600)

## Contributions 👏

All contributions are welcome BUT, as a general rule we try to keep our Trakt apps in parity with what's happening on the [Website](https://github.com/trakt/trakt-web).

If you wish to introduce something new into the app that's NOT already available on the website, please open a PR for the web first - or even better, start with a website GitHub issue or [Featurebase](https://roadmap.trakt.tv/) request. This way your tokens and time will not be wasted ;)

If you see something that's already available on web but not yet in the Android app, PRs are welcome (it is still better to start with an issue first so we can assess and not duplicate any work by accident).

Let's keep the PRs small and focused on a single feature/issue. This way it is easier for everyone to understand it and review.

- Commits and PR titles follow [Conventional Commits](https://www.conventionalcommits.org/), e.g. `feat(app): add notes drawer`.
- Make sure `./gradlew ktlintCheck` passes.
- Using an AI coding agent? It should follow [`AGENTS.md`](AGENTS.md) and the rules in [`.agents/rules/`](.agents/rules/).

## License

Licensed under the [GNU General Public License v3.0](LICENSE).
