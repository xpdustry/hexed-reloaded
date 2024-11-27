/*
 * HexedReloaded, A reimplementation of the hexed gamemode from Anuke, with more features and better performances.
 *
 * Copyright (C) 2024  Xpdustry
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
package com.xpdustry.hexed.api.generation;

import arc.files.Fi;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import mindustry.Vars;
import mindustry.core.GameState.State;
import mindustry.gen.Groups;
import mindustry.io.SaveIO;
import mindustry.maps.Map;
import mindustry.net.Administration.Config;
import mindustry.net.Packets.KickReason;
import mindustry.world.Tiles;

public final class MapLoader implements Closeable {

    private final boolean paused;

    public static MapLoader create() {
        return new MapLoader();
    }

    private MapLoader() {
        this.paused = Vars.state.isPaused();
        if (Vars.state.isGame()) {
            Groups.player.each(player -> player.kick(KickReason.serverRestarting));
            Vars.state.set(State.menu);
            Vars.net.closeServer();
        }
    }

    public void load(final Map map) {
        Vars.world.loadMap(map);
    }

    public void load(final File file) {
        SaveIO.load(new Fi(file));
        Vars.state.rules.sector = null;
    }

    public void load(final int width, final int height, final Consumer<Tiles> generator) {
        Vars.logic.reset();
        Vars.world.loadGenerator(width, height, generator::accept);
    }

    public <C extends MapContext> C load(final MapGenerator<C> generator) {
        Vars.logic.reset();
        Vars.world.beginMapLoad();

        // Clear tile entities
        for (final var tile : Vars.world.tiles) {
            if (tile != null && tile.build != null) {
                tile.build.remove();
            }
        }

        final var context = generator.generate();
        Vars.world.tiles = new Tiles(context.getWidth(), context.getHeight());
        Vars.world.tiles.fill();
        context.forEachTile((x, y, tile) -> {
            final var original = Vars.world.tiles.get(x, y);
            original.setFloor(tile.getFloor());
            original.setOverlay(tile.getOverlay());
            original.setBlock(tile.getBlock());
            if (tile.getBlock().hasBuilding()) {
                original.build.health = tile.getBuilding().getHealth();
                original.build.team = tile.getBuilding().getTeam();
                original.build.configure(tile.getBuilding().getConfiguration());
            }
        });

        Vars.world.endMapLoad();
        Vars.state.rules = context.getRules();
        Vars.state.map.tags.put("name", context.getMapName());
        return context;
    }

    @Override
    public void close() throws IOException {
        Vars.state.set(this.paused ? State.paused : State.playing);
        try {
            Vars.net.host(Config.port.num());
        } catch (final IOException exception) {
            Vars.state.set(State.menu);
            throw exception;
        }
    }
}
