# MinimalSF Extended

Power-user actions for the [MinimalSF launcher](https://github.com/DHIRAJ-J-S/MinimalSF_Launcher), as a separate plugin, so the launcher itself stays minimal.

With it installed, long press an app in MinimalSF and tap **extended…**:

| Action | What it does |
|---|---|
| Open in freeform | Opens the app in a floating window (works without Shizuku or root) |
| Force stop | Closes the app completely |
| Clear cache | Deletes the app's temporary files (needs root: root mode, or Shizuku started with root) |
| Clear data | Resets the app like a fresh install (asks first) |
| Freeze | Disables the app without uninstalling it; unfreeze it from the plugin's screen |

## Setup

Install MinimalSF Extended. With nothing else, **open in freeform** already works. For the other actions, use one of:

- **Shizuku**, or a fork of it, or **Sui** (the Magisk / KernelSU module): start it, open MinimalSF Extended, tap **allow** and allow it in Shizuku's prompt.
- **Root mode**, on rooted phones (Magisk, KernelSU, APatch...): open MinimalSF Extended and tap **turn on** under root mode; your root app asks first. Shizuku is still used first whenever it's running.

Then in MinimalSF (1.3.2 or newer), long press any app → **extended…**. The menu only shows what works right now, and new actions arrive with plugin updates alone.

The plugin's screen shows whether Shizuku is ready and lists the apps you froze, since frozen apps disappear from the launcher.

## Safety

- **Only MinimalSF can use it.** The action screen and the menu are protected by a signature permission, so only apps signed with the same key can reach them. Anything else, including `adb`, is refused. The plugin also checks at run time that this permission is really defined by MinimalSF or itself, and that MinimalSF has the same key; MinimalSF checks the plugin's key too.
- **A fixed list of commands.** The privileged side (Shizuku's process, or `su`) only runs these exact shapes, with checked values: `am force-stop`, `pm clear`, `pm disable-user`, `pm enable` and `am start --windowingMode 5`, each for one app and one user. Anything else is refused, even if something got past the checks above. Shizuku runs commands without a shell; root mode quotes every value.
- **Clear cache never follows links**, so an app can't point its cache folder at system files.
- **Off limits:** the system, Settings, Shizuku and MinimalSF itself can't be stopped, cleared or frozen from here.
- **No tapjacking:** the plugin's buttons ignore taps while another app draws over the screen.
- **No internet permission**, no ads, no tracking, backups off.

## Build

Open in Android Studio, or `./gradlew assembleRelease`. The release APK is about 50 KB.

Both apps must be signed with the same key for MinimalSF to be allowed to use the plugin. Releases are built and signed by GitHub Actions (see [.github/RELEASING.md](.github/RELEASING.md)).

## License

MIT
