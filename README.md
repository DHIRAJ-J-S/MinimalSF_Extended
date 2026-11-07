# MinimalSF Extended

Power-user actions for the [MinimalSF launcher](https://github.com/DHIRAJ-J-S/MinimalSF_Launcher), as a separate plugin, so the launcher itself stays minimal.

With it installed, long press an app in MinimalSF and tap **extended…**:

| Action | What it does |
|---|---|
| Open in freeform | Opens the app in a floating window |
| Force stop | Closes the app completely |
| Clear cache | Deletes the app's temporary files (needs Shizuku started with root) |
| Clear data | Resets the app like a fresh install (asks first) |
| Freeze | Disables the app without uninstalling it; unfreeze it from the plugin's screen |

## Setup

1. Install [Shizuku](https://shizuku.rikka.app/) and start it (wireless debugging, or root).
2. Install MinimalSF Extended and open it once.
3. Tap **allow**, then allow it in Shizuku's prompt.
4. In MinimalSF (1.3.2 or newer), long press any app → **extended…**

The plugin's screen shows whether Shizuku is ready and lists the apps you froze, since frozen apps disappear from the launcher.

## Safety

- **Only MinimalSF can use it.** The action screen is protected by a signature permission, so only apps signed with the same key can call it. Anything else, including `adb`, is refused.
- **No shell in between.** Each action is one fixed `am` or `pm` command, with the package name checked before it's used, so an app name can't turn into an extra command.
- **Off limits:** the system, Settings, Shizuku and MinimalSF itself can't be stopped, cleared or frozen from here.
- **No internet permission**, no ads, no tracking.

## Build

Open in Android Studio, or `./gradlew assembleRelease`. The release APK is about 50 KB.

Both apps must be signed with the same key for MinimalSF to be allowed to use the plugin. Releases are built and signed by GitHub Actions (see [.github/RELEASING.md](.github/RELEASING.md)).

## License

MIT
