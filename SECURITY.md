# Security Policy

## Supported versions

Only the latest release gets fixes. Please update before reporting.

## Reporting a problem

Please **don't open a public issue** for security problems. Report them privately instead:
[Security → Report a vulnerability](https://github.com/DHIRAJ-J-S/MinimalSF_Extended/security/advisories/new).

Include what's affected, how to reproduce it, and which versions of the plugin and MinimalSF you tested. You should hear back within a week.

## Scope

The plugin runs commands through Shizuku or root, so these matter most:

- Any app other than MinimalSF using the plugin's actions or settings (getting around the signature permission or the run-time signature check)
- Running a command outside the fixed list, or against more than one app, or against a protected app (the system, Settings, Shizuku, MinimalSF)
- Shell injection through app names or other values
- Clear cache following links outside the app's cache folder

Problems in the launcher itself should be reported on the [MinimalSF](https://github.com/DHIRAJ-J-S/MinimalSF_Launcher) repository.
