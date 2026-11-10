# Contributing to MinimalSF Extended

Thanks for helping out! A few things to know first.

## Before you start

- **Open an issue first** for anything bigger than a small fix, so we can agree on it before you spend time on it.
- **New actions are welcome**, but each one must stay safe: a fixed command, run for one checked app at a time. See [Safety](README.md#%EF%B8%8F-safety) in the README.
- **No internet, no trackers.** The plugin has no internet permission, ads, analytics or non-free libraries, and must stay that way.
- Issues with the launcher itself go to [MinimalSF](https://github.com/DHIRAJ-J-S/MinimalSF_Launcher).

## Reporting bugs

Use the [bug report form](https://github.com/DHIRAJ-J-S/MinimalSF_Extended/issues/new/choose). Include both app versions, whether you use Shizuku or root, your phone and Android version, and steps to reproduce.

## Building

Open the project in Android Studio, or run:

```bash
./gradlew assembleRelease
```

The plugin only works with a MinimalSF build signed with the **same key**, so a debug build of one app won't talk to a release build of the other. Release builds are signed by GitHub Actions; see [.github/RELEASING.md](.github/RELEASING.md).

## Pull requests

- Keep each pull request to one change.
- Follow the style of the surrounding code (Kotlin, no new dependencies unless really needed).
- Test on a real phone if you can, and say what you tested on and whether through Shizuku or root.
- Add a line to the next `fastlane/metadata/android/en-US/changelogs/` file if the change is visible to users.

By contributing, you agree that your work is released under the [MIT License](LICENSE).
