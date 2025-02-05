/*
 * HexedReloaded, a reimplementation of the hexed gamemode from Anuke,
 * with more features and better performances.
 *
 * Copyright (C) 2025  Xpdustry
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
package com.xpdustry.hexed.generation;

import com.xpdustry.distributor.api.collection.MindustryCollections;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import mindustry.game.Schematic;
import mindustry.world.Block;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ImmutableSchematic {

    private final List<Tile> tiles;
    private final int width;
    private final int height;
    private final SortedSet<String> labels;
    private final Map<String, String> tags;

    public ImmutableSchematic(final Schematic schematic) {
        this.tiles = MindustryCollections.immutableList(schematic.tiles).stream()
                .map(stile -> new ImmutableSchematic.Tile(
                        stile.x, stile.y, stile.block, stile.config, Tile.Rotation.from(stile.rotation)))
                .toList();
        this.width = schematic.width;
        this.height = schematic.height;
        this.labels =
                Collections.unmodifiableSortedSet(new TreeSet<>(MindustryCollections.immutableList(schematic.labels)));
        this.tags = Map.copyOf(MindustryCollections.immutableMap(schematic.tags));
    }

    public ImmutableSchematic(
            final List<Tile> tiles,
            final int width,
            final int height,
            final SortedSet<String> labels,
            final Map<String, String> tags) {
        this.tiles = List.copyOf(tiles);
        this.width = width;
        this.height = height;
        this.labels = Collections.unmodifiableSortedSet(new TreeSet<>(labels));
        this.tags = Map.copyOf(tags);
    }

    public List<Tile> getTiles() {
        return this.tiles;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public SortedSet<String> getLabels() {
        return this.labels;
    }

    public Map<String, String> getTags() {
        return this.tags;
    }

    public String getName() {
        return this.tags.getOrDefault("name", "unknown");
    }

    public String getDescription() {
        return this.tags.getOrDefault("description", "");
    }

    public record Tile(int x, int y, Block block, @Nullable Object configuration, Rotation rotation) {

        public enum Rotation {
            RIGHT,
            TOP,
            LEFT,
            BOTTOM;

            static Rotation from(final byte rotation) {
                return values()[rotation % 4];
            }
        }
    }
}
