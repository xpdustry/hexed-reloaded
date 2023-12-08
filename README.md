# HexedPluginReloaded

[![Xpdustry latest](https://maven.xpdustry.com/api/badge/latest/releases/com/xpdustry/hexed-reloaded?color=00FFFF&name=hexed-reloaded&prefix=v)](https://github.com/Xpdustry/HexedPluginReloaded/releases)
[![Build status](https://github.com/xpdustry/hexed-reloaded/actions/workflows/build.yml/badge.svg?branch=master&event=push)](https://github.com/Xpdustry/HexedPluginReloaded/actions/workflows/build.yml)
[![Mindustry 7.0 ](https://img.shields.io/badge/Mindustry-7.0-ffd37f)](https://github.com/Anuken/Mindustry/releases)

## Description

This plugin is a re-implementation of the original plugin from Anuken.

## Installation

This plugin requires :

- Java 17 or above.

- Mindustry v146 or above.

- [Distributor](https://github.com/xpdustry/distributor) v3.2.1.

## Building

- `./gradlew shadowJar` to compile the plugin into a usable jar (will be located
  at `builds/libs/hexed-reloaded.jar`).

- `./gradlew jar` for a plain jar that contains only the plugin code.

- `./gradlew runMindustryServer` to run the plugin in a local Mindustry server.

- `./gradlew test` to run the unit tests of the plugin.
