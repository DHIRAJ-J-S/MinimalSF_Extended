<h1><img src="fastlane/metadata/android/en-US/images/icon.png" width="50" align="absmiddle" alt="icon">&nbsp;MinimalSF Extended</h1>

**Power-user actions for the [MinimalSF launcher](https://github.com/DHIRAJ-J-S/MinimalSF_Launcher).**

A tiny plugin that adds force stop, clear data, freeze and more to MinimalSF's long press menu, so the launcher itself stays minimal. Install it only if you want these; without it, MinimalSF is unchanged.

<a href="https://github.com/DHIRAJ-J-S/MinimalSF_Extended/releases/latest"><img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" alt="Get it on GitHub" height="54"></a>
<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/DHIRAJ-J-S/MinimalSF_Extended"><img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="54"></a>

[![Latest release](https://img.shields.io/github/v/release/DHIRAJ-J-S/MinimalSF_Extended?label=release)](https://github.com/DHIRAJ-J-S/MinimalSF_Extended/releases/latest)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)

---

## ⚡ Actions

Long press any app in MinimalSF → **extended…**

| Action | What it does |
|---|---|
| 🪟 Open in freeform | Opens the app in a floating window |
| ⏹️ Force stop | Closes the app completely |
| 🧹 Clear cache | Deletes the app's temporary files (root only) |
| 🗑️ Clear data | Resets the app like a fresh install (asks first) |
| ❄️ Freeze | Disables the app without uninstalling it; its files and data are kept |

Frozen apps disappear from the launcher, so the plugin's own screen lists them, with an **unfreeze** button for each.

---

## 📋 Requirements

- **MinimalSF 1.3.2 or newer**
- One of these:
  - **[Shizuku](https://shizuku.rikka.app/)**, a fork of it, or **Sui**: works without root (wireless debugging)
  - **Root** (Magisk, KernelSU, APatch...), using the plugin's **root mode**

Clear cache needs root: root mode, or Shizuku started with root.

---

## 🚀 Setup

1. Install MinimalSF Extended and open it.
2. Either:
   - **Shizuku:** start Shizuku, then tap **allow** in MinimalSF Extended and allow it in Shizuku's prompt.
   - **Root:** tap **turn on** under root mode and grant root.
3. In MinimalSF, long press any app → **extended…**

Shizuku is used whenever it's running; root mode is the fallback. The menu only shows what works right now. With neither, it offers **set up Shizuku or root…**, which opens the plugin.

---

## 🛡️ Safety

- **Only MinimalSF can use it.** Its action screen and menu are locked to apps signed with the same key. Anything else, including `adb`, is refused, and each app checks the other's signature at run time.
- **A fixed list of commands.** It can only run `am force-stop`, `pm clear`, `pm disable-user`, `pm enable` and `am start --windowingMode 5`, for one checked app at a time. Nothing else gets through, even if something got past the lock above.
- **No shell tricks.** App names are checked before use, and commands run without a shell (Shizuku) or with every value quoted (root).
- **Clear cache never follows links,** so an app can't point its cache at system files.
- **Off limits:** the system, Settings, Shizuku and MinimalSF can't be stopped, cleared or frozen.
- **No tapjacking:** buttons ignore taps while another app draws over the screen.
- 🚫 **No internet permission**, no ads, no tracking, backups off.

---

## ❓ FAQ

**Why a separate app?**
Most people never need these, and they need Shizuku or root. Keeping them out of MinimalSF keeps the launcher small and simple ([#3](https://github.com/DHIRAJ-J-S/MinimalSF_Launcher/issues/3)).

**Does freeform work without Shizuku or root?**
No. Android doesn't let a normal app open another app in a window. Tested on Android 15 with every freeform developer option on.

**Do I need the "freeform windows" developer option?**
Not on the phones tested: One UI (Samsung) through Shizuku, and EvolutionX (Android 15) through Shizuku and root. Some other ROMs may need it.

**New actions?**
They arrive with plugin updates alone: MinimalSF reads the menu from the plugin, so you don't need a launcher update.

---

## 📦 Install

- **Obtainium** (recommended): tap the Obtainium badge at the top on your phone, or add `https://github.com/DHIRAJ-J-S/MinimalSF_Extended` in [Obtainium](https://github.com/ImranR98/Obtainium). You get updates automatically from each new release.
- **Direct download:** get the latest APK from [Releases](https://github.com/DHIRAJ-J-S/MinimalSF_Extended/releases/latest).

---

## 🔨 Build

Open in Android Studio, or run `./gradlew assembleRelease`.

MinimalSF and the plugin must be signed with the **same key** for them to work together. Releases are built and signed by GitHub Actions; see [.github/RELEASING.md](.github/RELEASING.md).

---

## 📄 License

MIT License
```
You’re free to use, tweak, and build on this project.
Have fun with it, break things, improve things 🙂
```
