# Getting Started

1. Download and install the latest stable Android Studio:

   https://developer.android.com/studio
   
2. Clone this repo and open it.
3. Open `local.properties` and make sure they contain all required values like below:
```
TRAKT_API_KEY = "PUT_YOUR_VALUE_HERE"
TRAKT_API_SECRET = "PUT_YOUR_VALUE_HERE"

KEYSTORE_ALIAS = PUT_YOUR_VALUE_HERE
KEYSTORE_PASSWORD = PUT_YOUR_VALUE_HERE
KEYSTORE_KEY_PASSWORD = PUT_YOUR_VALUE_HERE
```
4. Download `google-services.json` Firebase config for the app from:
   
   https://console.firebase.google.com/u/8/project/trakt-f4bbe/settings/general/android:tv.trakt.trakt
   
   Put it into `/app/` folder -> `/app/google-services.json`.

6. Make sure `keystore.jks` is located in the root folder (same level as `/local.properties`)

You are all set!
