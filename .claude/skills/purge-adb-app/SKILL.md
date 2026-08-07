---
name: purge-adb-app
description: 'Fully removes the internal Trakt build (tv.trakt.trakt.v3) from the connected device or emulator - uninstalls the package and wipes any leftover /data/app install directories. Use when a reinstall fails with INSTALL_FAILED_UPDATE_INCOMPATIBLE / signature or version mismatches, or when the app must start from a clean slate. Trigger: /purge-adb-app, "purge app", "clean app install", "uninstall trakt internal".'
allowed-tools:
  - Bash
---

<objective>
Wipe the internal Trakt build off the attached device so the next install starts clean.
</objective>

<package>
Default package: `tv.trakt.trakt.v3` — the `internal` flavor (`applicationId` `tv.trakt.trakt`
plus the `.v3` `applicationIdSuffix` from `app/build.gradle.kts`).

If the user names a different package or flavor, substitute it everywhere below. The
`playstore` flavor is `tv.trakt.trakt`.
</package>

<steps>
1. Confirm exactly one device is attached:

   ```bash
   adb devices
   ```

No device — stop and say so. More than one — ask which serial to target and add
`-s <serial>` to every command that follows.

2. Uninstall and clear leftover install directories:

   ```bash
   adb shell "pm uninstall tv.trakt.trakt.v3"
   adb shell "rm -rf /data/app/tv.trakt.trakt.v3-*"
   ```

3. Verify nothing remains:

   ```bash
   adb shell "pm list packages | grep tv.trakt.trakt"
   ```

</steps>

<expected_output>

- `pm uninstall` prints `Success`. `Failure [DELETE_FAILED_INTERNAL_ERROR]` or
  `Unknown package` means the app was not installed — that is fine, carry on to step 2.
- The `rm -rf` needs root (emulator or userdebug build). On a production device it prints
  `rm: ... Permission denied` or fails silently; `pm uninstall` alone already removes the
  install directory there, so report it and move on rather than retrying with `adb root`
  unless the user asks.
- Step 3 printing nothing means the device is clean.
  </expected_output>

<notes>
- This deletes the app and all of its data (login session, DataStore preferences, caches).
  It is not reversible — say so before running if the user has not clearly asked for a wipe.
- Reinstall afterwards with `./gradlew :app:installInternalDebug`.
- To keep the install but drop only its data, use `adb shell pm clear tv.trakt.trakt.v3`
  instead and mention that option when the user's goal is just a clean state.
</notes>
