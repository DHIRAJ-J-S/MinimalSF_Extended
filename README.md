<h1><img src="fastlane/metadata/android/en-US/images/icon.png" width="50" align="absmiddle" alt="icon">&nbsp;MinimalSF Extended</h1>

**Power-user actions and extra options for the [MinimalSF launcher](https://github.com/DHIRAJ-J-S/MinimalSF_Launcher).**

A tiny plugin that adds force stop, clear data, freeze and more to MinimalSF's long press menu, plus extras like coloured icons, so the launcher itself stays minimal. Install it only if you want these; without it, MinimalSF is unchanged.

<a href="https://github.com/DHIRAJ-J-S/MinimalSF_Extended/releases/latest"><img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" alt="Get it on GitHub" height="54"></a>
<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/DHIRAJ-J-S/MinimalSF_Extended"><img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="54"></a>

[![Latest release](https://img.shields.io/github/v/release/DHIRAJ-J-S/MinimalSF_Extended?label=release)](https://github.com/DHIRAJ-J-S/MinimalSF_Extended/releases/latest)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
[![VirusTotal: v1.1, 0 detections](https://img.shields.io/badge/VirusTotal-v1.1%20%C2%B7%200%20detections-brightgreen?logo=virustotal&logoColor=white)](https://www.virustotal.com/gui/file/7fbc6520d6b541183c1769bd011194c2b3203706dbf1c0229050f65527c94522/detection)

<p>
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/01_long_press.png" width="19%" alt="Long press menu with extended…">
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/02_actions.png" width="19%" alt="Extended actions">
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/04_extended_menu.png" width="19%" alt="Extended menu in MinimalSF settings">
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/06_coloured_icons.png" width="19%" alt="Coloured app icons">
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/07_plugin_app.png" width="19%" alt="The plugin's screen">
</p>

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

## 🧩 Extended Menu

In MinimalSF, **settings → extended menu** (plugin 1.1+):

| Option | What it does |
|---|---|
| Long press menu | Choose which entries the long press menu shows: app shortcuts, app info, uninstall, set keyword, hide from search, and each action above |
| Hide empty search bar | The search bar stays invisible until you start typing ([#7](https://github.com/DHIRAJ-J-S/MinimalSF_Launcher/issues/7)) |
| Coloured app icons | App icons in colour instead of black and white ([#10](https://github.com/DHIRAJ-J-S/MinimalSF_Launcher/issues/10)) |
| Auto-launch a single match | Turn it off to show a single match as a normal row, so you can long press it ([#11](https://github.com/DHIRAJ-J-S/MinimalSF_Launcher/issues/11)) |
| Open MinimalSF Extended | Opens this app (setup, root mode, frozen apps) |

These don't need Shizuku or root. Uninstall the plugin and MinimalSF goes back to its defaults.

---

## 📋 Requirements

- **MinimalSF 1.3.2 or newer.** That's all the [extended menu](#-extended-menu) needs.
- For the **actions**, one of these:
  - **[Shizuku](https://shizuku.rikka.app/)**, a fork of it, or **Sui**: works without root (wireless debugging)
  - **Root** (Magisk, KernelSU, APatch...), using the plugin's **root mode**

Clear cache needs root: root mode, or Shizuku started with root.

---

## 🚀 Setup

1. Install MinimalSF Extended and open it. The extended menu works right away; steps 2 and 3 are for the actions.
2. Either:
   - **Shizuku:** start Shizuku, then tap **allow** in MinimalSF Extended and allow it in Shizuku's prompt.
   - **Root:** tap **turn on** under root mode and grant root.
3. In MinimalSF, long press any app → **extended…**, or open **settings → extended menu**

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
- 🧪 **Scanned on [VirusTotal](https://www.virustotal.com/gui/file/236abe8aec431c50a777f2c874a6a54a4a67f934572be2496be781046eb52d35/detection):** 0 detections (v1.0).

---

## ❓ FAQ

**Why a separate app?**
Most people never need these, and they need Shizuku or root. Keeping them out of MinimalSF keeps the launcher small and simple ([#3](https://github.com/DHIRAJ-J-S/MinimalSF_Launcher/issues/3)).

**Does freeform work without Shizuku or root?**
No. Android doesn't let a normal app open another app in a window. Tested on Android 15 with every freeform developer option on.

**Do I need the "freeform windows" developer option?**
Not on the phones tested: One UI (Samsung) through Shizuku, and EvolutionX (Android 15) through Shizuku and root. Some other ROMs may need it.

**New actions and options?**
They arrive with plugin updates alone: MinimalSF reads both the actions and the extended menu from the plugin. Only options that change how the launcher itself behaves also need a MinimalSF update.

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
Have fun with it, break things, improve things 😁
```
