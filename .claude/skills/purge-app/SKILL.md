---
name: purge-app
description:
  Fully purge the Trakt app from a connected Android emulator/device — uninstall, remove leftover install dirs that block future installs, verify clean, report free storage. Trigger: "purge app", "clean uninstall", "app won't install", INSTALL_FAILED errors.
---

# Purge Trakt app from emulator/device

Removes the app and every leftover that can block future installs
(`INSTALL_FAILED_*`, stale `/data/app` or `/data/app-lib` dirs).

## Steps

1. **Pick device.** `adb devices` — if multiple, ask user or use the only emulator. Use
   `-s <serial>` on every following command.

2. **Find package.** Package id varies by flavor:
   ```bash
   adb -s <serial> shell pm list packages | grep -i trakt
   ```
   Expected: `tv.trakt.trakt.v3` (or debug variant). If nothing found, skip to step 4 (leftover
   check) — app may be half-uninstalled.

3. **Uninstall.**
   ```bash
   adb -s <serial> shell "pm uninstall <package>"
   ```
   `Failure [DELETE_FAILED_INTERNAL_ERROR]` when package already gone — not an error, continue.

4. **Remove leftovers that block reinstall.**
   ```bash
   adb -s <serial> shell "rm -rf /data/app/<package>-* /data/app/*/<package>-* /data/app-lib/<package>-* /data/data/<package>"
   ```
   On production builds `adb root` fails (`adbd cannot run as root in production builds`) — `rm` may
   be permission-denied. First check whether anything is even there:
   ```bash
   adb -s <serial> shell "ls -d /data/app/*trakt* /data/app/*/*trakt* /data/app-lib/*trakt* /data/data/*trakt*"
   ```
   All `No such file or directory` = clean, nothing to remove.

5. **Verify clean.**
   ```bash
   adb -s <serial> shell "pm list packages -u | grep -i trakt"
   ```
   Empty output = no remnants, including uninstalled-with-kept-data entries.

6. **Report storage.**
   ```bash
   adb -s <serial> shell df -h /data | tail -1
   ```
   Report free space to user. If `/data` still near full, note that other apps/system eat the disk;
   offer AVD disk resize or factory reset.

## Notes

- Cache trim rarely helps: `pm trim-caches 999999M` freed ~0 last run.
- Reinstall after purge: `./gradlew :app:installDebug`.
