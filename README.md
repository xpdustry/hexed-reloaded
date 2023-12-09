# hexed-reloaded

[![Xpdustry latest](https://maven.xpdustry.com/api/badge/latest/releases/com/xpdustry/hexed-reloaded?color=00FFFF&name=hexed-reloaded&prefix=v)](https://github.com/Xpdustry/HexedPluginReloaded/releases)
[![Build status](https://github.com/xpdustry/hexed-reloaded/actions/workflows/build.yml/badge.svg?branch=master&event=push)](https://github.com/Xpdustry/HexedPluginReloaded/actions/workflows/build.yml)
[![Mindustry 7.0 ](https://img.shields.io/badge/Mindustry-7.0-ffd37f)](https://github.com/Anuken/Mindustry/releases)

## Description

This plugin is a re-implementation of the [original gamemode](https://github.com/Anuken/HexedPlugin) from Anuken.

With the addition of a proper API for interacting with the hex game and an experimental map loading and generation API.

## Installation

This plugin requires :

- Java 17 or above.

- Mindustry v146 or above.

- [Distributor](https://github.com/xpdustry/distributor) v3.2.1.

## Usage

Once the plugin is installed on the server, use the `/hexed start` command 
to start a game with the vanilla generator from Anuke.
If you have custom generator registered, specify its name when running the command such as `hexed start my-generator`.

To access the API of this plugin, add the following in your project build script

```kts
repositories {
    maven("https://maven.xpdustry.com/releases")
}

dependencies {
    compileOnly("com.xpdustry:hexed-reloaded:VERSION")
}
```

Then in your plugin, you can use the `HexedAPIProvider` to get the API instance or
listen to the events produced by the plugin.

```java
import arc.Events;
import arc.util.Log;
import com.xpdustry.hexed.api.HexedAPIProvider;
import com.xpdustry.hexed.api.event.HexCaptureEvent;
import com.xpdustry.hexed.api.generation.HexedMapContext;
import com.xpdustry.hexed.api.generation.MapGenerator;
import com.xpdustry.hexed.api.generation.SimpleHexedMapContext;
import mindustry.mod.Plugin;

public final class MyPlugin extends Plugin {

  @Override
  public void init() {
    HexedAPIProvider.get().registerGenerator("my-generator", new MyGenerator());

    Events.on(HexCaptureEvent.class, event -> 
      Log.info("@ captured the hex @", event.player().plainName(), event.hex().getIdentifier()));
  }

  private static final class MyGenerator implements MapGenerator<HexedMapContext> {

    @Override
    public HexedMapContext generate() {
      final var context = new SimpleHexedMapContext();
      context.resize(300, 300);
      /*
       * Do your magic here
       */
      return context;
    }
  }
}
```

## Building

- `./gradlew shadowJar` to compile the plugin into a usable jar (will be located
  at `builds/libs/hexed-reloaded.jar`).

- `./gradlew jar` for a plain jar that contains only the plugin code.

- `./gradlew runMindustryServer` to run the plugin in a local Mindustry server.

- `./gradlew test` to run the unit tests of the plugin.
