<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# .NET Watch Run Configuration Changelog

## [Unreleased]

## [2023.1.1]

- Minor compatibility fixes

## [2023.1.0]

- Compatibility with Rider 2023.1

## [2022.3.0]

- Add checkbox for DOTNET_WATCH_SUPPRESS_BROWSER_REFRESH, DOTNET_USE_POLLING_FILE_WATCHER
- Add checkbox to disable automatic browser launch [#5](https://github.com/maartenba/DotNetWatch/issues/5)
- Do not stop dotnet watch process when a build is started [#4](https://github.com/maartenba/DotNetWatch/issues/4)
- Compatibility with Rider 2022.3

## [2022.2.3]

- Fix issue where working directory was not updated when changing selected project

## [2022.2.2]

- Compatibility with Rider 2022.2

## [2022.2.1]

- Compatibility with Rider 2022.2 EAP9

## [2022.2.0]

- Compatibility with Rider 2022.2 EAP1

## [2022.1.0]

- Compatibility with Rider 2022.1

## [0.1.6]

### Added

- Add checkbox for DOTNET_WATCH_RESTART_ON_RUDE_EDIT [#3](https://github.com/maartenba/DotNetWatch/issues/3)

## [0.1.5]

### Added

- Compatibility with Rider 2022.1 EAP

## [0.1.4]

### Fixes

- Unable to use hotreload with --framework flag in command line [#2](https://github.com/maartenba/DotNetWatch/issues/2)

## [0.1.3]

### Added

- Compatibility with Rider 2021.3 RTM

## [0.1.2]

### Fixes

- Working directory not updating in projects with multiple targets
- Duplicate projects in project selector are no longer shown
- Loading configuration editor no longer shows blank fields

## [0.1.1]

### Added

- Add initial support for `dotnet-watch` run configurations