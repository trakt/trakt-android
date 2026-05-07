<div align="center">
  <img width="200" height="200" alt="logo" src="https://github.com/user-attachments/assets/3ae10e8f-11fa-461d-8046-4502ae005d3a" />
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
<img width="2277" height="1200" alt="images" src="https://github.com/user-attachments/assets/565977dc-ee6e-4d9d-8d71-5a28f1217dba" />

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
