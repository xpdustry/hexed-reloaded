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
import com.xpdustry.hexed.api.HexedAPI;
import com.xpdustry.hexed.api.HexedAPIProvider;
import com.xpdustry.hexed.api.HexedState;
import com.xpdustry.hexed.api.generation.AnukeHexedGenerator;
import com.xpdustry.hexed.api.generation.HexedMapContext;
import com.xpdustry.hexed.api.generation.ImmutableSchematic;
import com.xpdustry.hexed.api.generation.MapGenerator;
import com.xpdustry.hexed.api.generation.MapLoader;
import com.xpdustry.hexed.api.generation.SimpleHexedMapContext;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import mindustry.Vars;
import mindustry.game.Schematics;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

@SuppressWarnings("unused")
public final class HexedPluginReloaded extends AbstractMindustryPlugin implements HexedAPI {

    private final Map<String, MapGenerator<HexedMapContext>> generators = new HashMap<>();
    private @MonotonicNonNull SimpleHexedState state = null;
    private @MonotonicNonNull ImmutableSchematic defaultBase = null;

    @Override
    public HexedState getHexedState() {
        return this.state;
    }

    SimpleHexedState getHexedState0() {
        return this.state;
    }

    @Override
    public void registerGenerator(final String name, final MapGenerator<HexedMapContext> generator) {
        if (this.generators.containsKey(name)) {
            throw new IllegalStateException("A generator with the name " + name + " has already been registered.");
        }
        this.generators.put(name, generator);
    }

    @Override
    public Map<String, MapGenerator<HexedMapContext>> getGenerators() {
        return Collections.unmodifiableMap(this.generators);
    }

    @Override
    public ImmutableSchematic getDefaultBaseSchematic() {
        return this.defaultBase;
    }

    @Override
    public boolean isEnabled() {
        return Vars.state.rules.tags.getBool(HEXED_PRESENCE_FLAG);
    }

    @Override
    public boolean start(final MapGenerator<HexedMapContext> generator) {
        try (final var loader = MapLoader.create()) {
            this.getLogger().info("Generating hexed map.");
            final var start = System.currentTimeMillis();
            final var context = loader.load(generator);
            this.getLogger().info("Generated hexed map, took {} milliseconds.", System.currentTimeMillis() - start);
            final var base = context.getBaseSchematic() == null ? this.defaultBase : context.getBaseSchematic();
            this.state = new SimpleHexedState(base, context.getHexes(), context.getDuration());
            return true;
        } catch (final Exception e) {
            this.getLogger().error("Failed to host a hexed game", e);
            return false;
        }
    }

    @Override
    public void onInit() {
        HexedAPIProvider.set(this);

        try (final var stream = SimpleHexedMapContext.class.getResourceAsStream("/default.msch")) {
            this.defaultBase = new ImmutableSchematic(Schematics.read(Objects.requireNonNull(stream)));
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load the default base schematic.", e);
        }

        this.generators.put("anuke", new AnukeHexedGenerator());
        this.state = new SimpleHexedState(this.defaultBase, Collections.emptyList(), Duration.ZERO);

        this.addListener(new HexedLogic(this));
        this.addListener(new HexedRenderer(this));
        this.addListener(new HexedCommands(this));
    }
}
