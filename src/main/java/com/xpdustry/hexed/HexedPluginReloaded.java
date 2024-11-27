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
package com.xpdustry.hexed;

import com.xpdustry.distributor.api.plugin.AbstractMindustryPlugin;
import com.xpdustry.hexed.generation.HexedMapContext;
import com.xpdustry.hexed.generation.MapGenerator;
import com.xpdustry.hexed.generation.MapLoader;
import mindustry.Vars;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

@SuppressWarnings("unused")
public final class HexedPluginReloaded extends AbstractMindustryPlugin implements HexedAPI {

    private @MonotonicNonNull HexedStateImpl state = null;

    @Override
    public HexedState getHexedState() {
        return this.state;
    }

    HexedStateImpl getHexedState0() {
        return this.state;
    }

    @Override
    public boolean isEnabled() {
        return Vars.state.rules.tags.getBool(HexedMapContext.HEXED_PRESENCE_FLAG);
    }

    @Override
    public boolean start(final MapGenerator<HexedMapContext> generator) {
        try (final var loader = MapLoader.create()) {
            this.getLogger().info("Generating hexed map.");
            final var start = System.currentTimeMillis();
            final var context = loader.load(generator);
            this.getLogger().info("Generated hexed map in {} milliseconds.", System.currentTimeMillis() - start);
            this.state = new HexedStateImpl(context.getBaseSchematic(), context.getHexes(), context.getDuration());
            return true;
        } catch (final Exception e) {
            this.getLogger().error("Failed to host a hexed game", e);
            return false;
        }
    }

    @Override
    public void onInit() {
        this.addListener(new HexedLogic(this));
        this.addListener(new HexedRenderer(this));
        this.addListener(new HexedCommands(this));
    }
}
