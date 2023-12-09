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
import com.xpdustry.hexed.HexedPluginReloaded;
import com.xpdustry.hexed.generation.AnukeHexedGenerator;
import com.xpdustry.hexed.generation.HexedMapContext;
import com.xpdustry.hexed.generation.MapGenerator;
import com.xpdustry.hexed.generation.MapLoader;
import fr.xpdustry.distributor.api.command.sender.CommandSender;
import fr.xpdustry.distributor.api.plugin.PluginListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class HexedStartCommand implements PluginListener {

    private static final Map<String, MapGenerator<HexedMapContext>> GENERATORS =
            Map.of("anuke", AnukeHexedGenerator.getInstance());

    private final HexedPluginReloaded hexed;
    private final Path customBasesDirectory;

    public HexedStartCommand(final HexedPluginReloaded hexed) {
        this.hexed = hexed;
        this.customBasesDirectory = this.hexed.getDirectory().resolve("bases");
    }

    @CommandMethod("hexed [generator] [base]")
    @CommandDescription("Begin hosting with the Hexed game mode.")
    @CommandPermission("com.xpdustry.hexed.start")
    public void onHexedCommand(
            final CommandSender sender,
            final @Argument("generator") @Nullable String name,
            final @Argument("base") @Nullable String base) {
        if (Vars.state.isGame()) {
            sender.sendWarning("Stop the server first.");
            return;
        }

        if (name != null && !GENERATORS.containsKey(name)) {
            sender.sendWarning("Unknown generator " + name + ".");
            return;
        }

        Schematic schematic = null;
        if (base != null) {
            final var file = this.customBasesDirectory.resolve(base + ".msch");
            if (Files.exists(file)) {
                try (final var stream = Files.newInputStream(file)) {
                    schematic = Schematics.read(stream);
                } catch (final IOException e) {
                    throw new RuntimeException("Failed to load the base schematic in the custom bases directory", e);
                }
            } else {
                sender.sendWarning("No custom base schematic named " + base + " found.");
                return;
            }
        }

        final MapGenerator<HexedMapContext> generator =
                name == null ? AnukeHexedGenerator.getInstance() : GENERATORS.get(name);
        if (generator == null) {
            sender.sendWarning("Generator named " + name + " not found.");
            return;
        }

        try (final var loader = MapLoader.create()) {
            final var context = loader.load(generator);
            sender.sendMessage("Map generated.");
            this.hexed.getHexedState().setHexes(context.getHexes());
            this.hexed.getHexedState().setBaseSchematic(schematic == null ? context.getBaseSchematic() : schematic);
            sender.sendMessage("Server started.");
        } catch (final Exception e) {
            sender.sendWarning("Failed to hexed with generator " + generator + ": " + e.getMessage());
            this.hexed.getLogger().error("Oh no", e);
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
}
