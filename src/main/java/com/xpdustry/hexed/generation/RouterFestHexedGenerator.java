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
package com.xpdustry.hexed.generation;

import com.xpdustry.hexed.model.Hex;
import com.xpdustry.hexed.model.SquareHex;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.world.blocks.environment.Floor;

public final class RouterFestHexedGenerator implements MapGenerator<HexedMapContext> {

    private static final RouterFestHexedGenerator INSTANCE = new RouterFestHexedGenerator();

    private static final int HEX_MAP_SIZE = 8;
    private static final int HEX_SIZE = 49;
    private static final int WALL_ENTRANCE_SIZE = 14;

    private static final int WALL_SIZE = 6;
    private static final int BORDER_SIZE = 3;

    private static final int HEX_TOTAL_SIZE = HEX_SIZE + (BORDER_SIZE * 2);
    private static final int HEX_MAP_TOTAL_SIZE = ((WALL_SIZE + HEX_TOTAL_SIZE) * HEX_MAP_SIZE) + WALL_SIZE;
    private static final int HEX_DIAMETER = HEX_TOTAL_SIZE + WALL_SIZE;

    private static final Floor HEX_BORDER_FLOOR = Blocks.darkPanel1.asFloor();
    private static final Floor HEX_FLOOR = Blocks.darksand.asFloor();

    public static RouterFestHexedGenerator getInstance() {
        return INSTANCE;
    }

    private RouterFestHexedGenerator() {}

    @Override
    public HexedMapContext generate() {
        final var context = new SimpleHexedMapContext();
        context.resize(HEX_MAP_TOTAL_SIZE, HEX_MAP_TOTAL_SIZE);
        context.forEachTile((x, y, tile) -> tile.setBlock(Blocks.darkMetal));

        // Lol
        // Blocks.router.health = 100000;
        // Blocks.router.solid = true;

        final List<Hex> hexes = new ArrayList<>();
        for (int i = 0; i < HEX_MAP_SIZE; i++) {
            for (int j = 0; j < HEX_MAP_SIZE; j++) {
                // coords = QUARTER + ROAD + PLOT
                final var x = (WALL_SIZE * (i + 1)) + (HEX_TOTAL_SIZE * i);
                final var y = (WALL_SIZE * (j + 1)) + (HEX_TOTAL_SIZE * j);
                final var cx = x + (HEX_TOTAL_SIZE / 2);
                final var cy = y + (HEX_TOTAL_SIZE / 2);

                hexes.add(new SquareHex(j + (i * HEX_MAP_SIZE), cx, cy, HEX_DIAMETER));

                context.forEachTile(x, y, HEX_TOTAL_SIZE, HEX_TOTAL_SIZE, (tx, ty, tile) -> {
                    tile.setBlock(Blocks.air);
                    tile.setFloor(HEX_BORDER_FLOOR); // Outline
                });

                context.forEachTile(x + BORDER_SIZE, y + BORDER_SIZE, HEX_SIZE, HEX_SIZE, (tx, ty, tile) -> {
                    tile.setFloor(HEX_FLOOR); // Internal
                });

                context.forEachTile(
                        cx - Math.floorDiv(Blocks.coreNucleus.size, 2),
                        cy - Math.floorDiv(Blocks.coreNucleus.size, 2),
                        Blocks.coreNucleus.size,
                        Blocks.coreNucleus.size,
                        (tx, ty, tile) -> {
                            tile.setFloor(Blocks.coreZone.asFloor()); // Core Zone
                        });

                // Make entrance
                if (i < HEX_MAP_SIZE - 1) {
                    context.forEachTile(
                            x + HEX_TOTAL_SIZE,
                            y + (HEX_TOTAL_SIZE / 2) - (WALL_ENTRANCE_SIZE / 2),
                            WALL_SIZE,
                            WALL_ENTRANCE_SIZE,
                            (tx, ty, tile) -> {
                                tile.setBlock(Blocks.air);
                                tile.setFloor(Blocks.darkPanel1.asFloor());
                            });
                }
                if (j < HEX_MAP_SIZE - 1) {
                    context.forEachTile(
                            x + (HEX_TOTAL_SIZE / 2) - (WALL_ENTRANCE_SIZE / 2),
                            y + HEX_TOTAL_SIZE,
                            WALL_ENTRANCE_SIZE,
                            WALL_SIZE,
                            (tx, ty, tile) -> {
                                tile.setBlock(Blocks.air);
                                tile.setFloor(Blocks.darkPanel1.asFloor());
                            });
                }
            }
        }
        context.setHexes(hexes);

        final var ores = OreGeneratorFunction.getDefaultHexedOreFunctions();
        ores.forEach(function -> {
            function.setTarget(Blocks.darksand);
            function.randomize();
        });
        context.forEachTile(TileConsumer.aggregate(ores));

        final var river = new RiverNoiseFunction();
        river.setScale(25F);
        river.setThreshold1(-0.05F);
        river.setThreshold2(0.1F);
        river.setOctaves(1);
        river.setFalloff(0.5F);
        river.setFloor1(Blocks.darksandWater.asFloor());
        river.setFloor2(Blocks.water.asFloor());
        river.setTarget(HEX_FLOOR);
        river.randomize();
        context.forEachTile(river);

        final var rules = context.getRules();
        rules.polygonCoreProtection = false;
        rules.enemyCoreBuildRadius = HEX_DIAMETER / 2F * Vars.tilesize;
        rules.buildSpeedMultiplier = 2F;
        rules.buildCostMultiplier = 0.75F;
        context.setRules(rules);
        context.setMapName("[orange]Routerfest");
        context.setBaseSchematic(this.getRouterBase());

        return context;
    }

    private Schematic getRouterBase() {
        try (final var stream = this.getClass().getResourceAsStream("/router.msch")) {
            return Schematics.read(Objects.requireNonNull(stream));
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load the router base schematic.", e);
        }
    }
}
