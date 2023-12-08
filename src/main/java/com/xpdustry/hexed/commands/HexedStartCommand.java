/*
 * HexedPluginReloaded, A reimplementation of the hexed gamemode, with more features and better performances.
 *
 * Copyright (C) 2023  Xpdustry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.xpdustry.hexed.commands;

import arc.util.CommandHandler;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.Flag;
import com.xpdustry.hexed.HexedPluginReloaded;
import com.xpdustry.hexed.generator.AnukeHexedGenerator;
import com.xpdustry.hexed.generator.HexedGeneratorContext;
import fr.xpdustry.distributor.api.command.sender.CommandSender;
import fr.xpdustry.distributor.api.plugin.PluginListener;
import fr.xpdustry.nucleus.mindustry.testing.map.MapGenerator;
import fr.xpdustry.nucleus.mindustry.testing.map.MapLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;

public final class HexedStartCommand implements PluginListener {

    private static final Map<String, MapGenerator<HexedGeneratorContext>> GENERATORS =
            Map.of("anuke", AnukeHexedGenerator.getInstance());

    private final HexedPluginReloaded hexed;

    public HexedStartCommand(final HexedPluginReloaded hexed) {
        this.hexed = hexed;
    }

    @CommandMethod("hexed [generator]")
    @CommandDescription("Begin hosting with the Hexed game mode.")
    @CommandPermission("fr.xpdustry.hexed.start")
    public void onHexedCommand(
            final CommandSender sender,
            final @Argument("generator") String gen,
            final @Flag("override-loadout") boolean override) {
        if (Vars.state.isGame()) {
            sender.sendWarning("Stop the server first.");
            return;
        }

        if (gen != null && !GENERATORS.containsKey(gen)) {
            sender.sendWarning("Unknown generator " + gen + ".");
            return;
        }

        Schematic loadout = null;
        if (override) {
            final var custom = getCustomLoadout();
            if (custom.isEmpty()) {
                sender.sendWarning("No custom loadout found.");
                return;
            }
            loadout = custom.get();
        }

        final MapGenerator<HexedGeneratorContext> generator =
                gen == null ? AnukeHexedGenerator.getInstance() : GENERATORS.get(gen);

        try (final var loader = MapLoader.create()) {
            final var context = loader.load(generator);
            sender.sendMessage("Map generated.");
            this.hexed.getHexedState().setHexes(context.getHexes());
            this.hexed.getHexedState().setLoadout(loadout == null ? context.getLoadout() : loadout);
            Vars.state.rules = context.getRules();
            sender.sendMessage("Server started.");
        } catch (final Exception e) {
            sender.sendWarning("Failed to hexed with generator " + generator + ".");
        }
    }

    @Override
    public void onPluginClientCommandsRegistration(final CommandHandler handler) {
        this.hexed.getClientCommandManager().getAnnotationParser().parse(this);
    }

    @Override
    public void onPluginServerCommandsRegistration(final CommandHandler handler) {
        this.hexed.getServerCommandManager().getAnnotationParser().parse(this);
    }

    private Optional<Schematic> getCustomLoadout() {
        var file = this.hexed.getDirectory().resolve("loadout.msch");
        if (Files.exists(file)) {
            try (final var stream = Files.newInputStream(file)) {
                return Optional.of(Schematics.read(stream));
            } catch (final IOException e) {
                throw new RuntimeException("Failed to load the loadout in the plugin directory.", e);
            }
        }
        return Optional.empty();
    }
}
