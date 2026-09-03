<div align="center">
  <img width="150" height="150" alt="logo" src="https://github.com/user-attachments/assets/3ae10e8f-11fa-461d-8046-4502ae005d3a" />
  <h1>Trakt for Android</h1>
  <p><em>Track everything you watch, wherever you stream it.</em></p>
</div>

<p align="center">
  <a href="https://github.com/trakt/trakt-android/releases"><img src="https://img.shields.io/github/v/tag/trakt/trakt-android?label=version&style=flat" alt="Version"></a>
  <a href="https://github.com/trakt/trakt-android/commits"><img src="https://img.shields.io/github/commit-activity/m/trakt/trakt-android?style=flat" alt="Commits"></a>
  <a href="https://github.com/trakt/trakt-android/stargazers"><img src="https://img.shields.io/github/stars/trakt/trakt-android?style=flat" alt="Stars"></a>
  <a href="https://github.com/trakt/trakt-android/stargazers"><img src="https://img.shields.io/github/actions/workflow/status/trakt/trakt-android/master.yml?branch=main" alt="Build"></a>
</p>

<br>

<p align="center">
<img width="200" alt="image" src="https://github.com/user-attachments/assets/81aaaa6e-7d4d-4a29-a264-362f2ea20c26" />
&nbsp;
<img width="200" alt="image" src="https://github.com/user-attachments/assets/5f66449e-9046-431b-8024-39ee9a234337" />
&nbsp;
<img width="200" alt="image" src="https://github.com/user-attachments/assets/4e756eec-b21a-4e8c-978a-ecfd2abb87d6" />
&nbsp;
<img width="200" alt="image" src="https://github.com/user-attachments/assets/e521a011-1e36-46c0-85eb-092dd6a36cb8" />
</p>

## Supported Form Factors

- Mobile
- Android TV
- Tablets

## Getting Started

1. Download and install the latest stable Android Studio:

   https://developer.android.com/studio
   
2. Clone this repo and open it.
3. Open `local.properties` and make sure they contain all required values like below:
```
TRAKT_API_KEY = "PUT_YOUR_VALUE_HERE"
TRAKT_API_SECRET = "PUT_YOUR_VALUE_HERE"
YOUNIFY_API_KEY "PUT_YOUR_VALUE_HERE (Optional)"

KEYSTORE_ALIAS = PUT_YOUR_VALUE_HERE
KEYSTORE_PASSWORD = PUT_YOUR_VALUE_HERE
KEYSTORE_KEY_PASSWORD = PUT_YOUR_VALUE_HERE
```
4. Download `google-services.json` Firebase config from your Firebase console project settings:
      
   Put it into `/app/` folder -> `/app/google-services.json`.

6. Make sure `keystore.jks` is located in the root folder (same level as `/local.properties`)

You are all set!

## Localisation 🌐

Want to help translating Trakt into your native language?
Have you spotted a mistake or an improvement?

Join the CrowdIn project: [Translate Trakt](https://crwd.in/trakt-poc/3857d5ea667dd425fbd0cb2e4e80dc192749600)

## Contributions 👏

All contributions are welcome BUT, as a general rule we try to keep our Trakt apps in parity with what's happening on the [Website](github.com/trakt/trakt-web)

If you wish to introduce something new into the app that's NOT already available on the website please open PR for the web first - or even better start with a website Github issue or [Featurebase](https://roadmap.trakt.tv/) request. This way your tokens and time will not be wasted ;)

If you see something that's already available on web but not yet in the Android app then PR is welcome surely (it is still better to start with an Issue first so we can assess and not duplicate any work by accident).

Let's keep the PRs small and focused on a single feature/issue. This way it is easier for everyone to understand it and review.
