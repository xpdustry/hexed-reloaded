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
package fr.xpdustry.hexed;

import fr.xpdustry.distributor.api.plugin.AbstractMindustryPlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.world.blocks.storage.CoreBlock;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

@SuppressWarnings("unused")
public final class HexedPluginReloaded extends AbstractMindustryPlugin {

    public static final String HEXED_PRESENCE_FLAG = "hexed-reloaded";

    private final HexedState state = new HexedState();
    private @MonotonicNonNull Schematic loadout = null;

    @Override
    public void onInit() {
        this.addListener(new HexedLogic(this));
        this.addListener(new HexedRenderer(this));
        this.loadout = this.loadLoadout();
    }

    public boolean isActive() {
        return Vars.state.rules.tags.getBool(HEXED_PRESENCE_FLAG) && !Vars.state.isMenu();
    }

    public HexedState getHexedState() {
        return this.state;
    }

    Schematic getLoadout() {
        return this.loadout;
    }

    private Schematic loadLoadout() {
        var file = this.getDirectory().resolve("loadout.msch");
        final Schematic schematic;
        if (Files.exists(file)) {
            try (final var stream = Files.newInputStream(file)) {
                schematic = Schematics.read(stream);
            } catch (final IOException e) {
                throw new RuntimeException("Failed to load the loadout in the plugin directory.", e);
            }
        } else {
            try (final var stream = this.getClass().getResourceAsStream("/loadout.msch")) {
                schematic = Schematics.read(Objects.requireNonNull(stream));
            } catch (final IOException e) {
                throw new RuntimeException("Failed to load the default loadout.", e);
            }
        }
        final var core = schematic.tiles.find(s -> s.block instanceof CoreBlock);
        if (core == null) {
            throw new IllegalArgumentException("The loadout has no core tile.");
        }
        return schematic;
    }
}
