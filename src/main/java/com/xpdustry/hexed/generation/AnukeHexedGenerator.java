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

import arc.math.Mathf;
import arc.math.geom.Bresenham2;
import arc.math.geom.Geometry;
import arc.math.geom.Intersector;
import arc.math.geom.Vec2;
import arc.util.Structs;
import arc.util.noise.Simplex;
import com.xpdustry.hexed.model.Hex;
import com.xpdustry.hexed.model.Hexagon;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.world.Block;

/**
 * Original code from Anuke.
 * I will be honest, I have no idea what some sections do, but if it works, it works...
 */
public final class AnukeHexedGenerator implements MapGenerator<HexedMapContext> {

    private static final AnukeHexedGenerator INSTANCE = new AnukeHexedGenerator();

    private static final int DIAMETER = 74;
    private static final int SPACING = 78;
    private static final int WIDTH = 516;
    private static final int HEIGHT = 516;

    // elevation --->
    // temperature
    // |
    // v
    private static final Block[][] FLOORS = {
        {Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.grass},
        {Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.grass, Blocks.grass},
        {Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.grass, Blocks.shale},
        {
            Blocks.darksandTaintedWater,
            Blocks.darksandTaintedWater,
            Blocks.moss,
            Blocks.moss,
            Blocks.sporeMoss,
            Blocks.stone
        },
        {Blocks.ice, Blocks.iceSnow, Blocks.snow, Blocks.dacite, Blocks.hotrock, Blocks.salt}
    };

    private static final Block[][] BLOCKS = {
        {Blocks.stoneWall, Blocks.stoneWall, Blocks.sandWall, Blocks.sandWall, Blocks.pine, Blocks.pine},
        {Blocks.stoneWall, Blocks.stoneWall, Blocks.duneWall, Blocks.duneWall, Blocks.pine, Blocks.pine},
        {Blocks.stoneWall, Blocks.stoneWall, Blocks.duneWall, Blocks.duneWall, Blocks.pine, Blocks.pine},
        {Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.stoneWall},
        {Blocks.iceWall, Blocks.snowWall, Blocks.snowWall, Blocks.snowWall, Blocks.stoneWall, Blocks.saltWall}
    };

    public static AnukeHexedGenerator getInstance() {
        return INSTANCE;
    }

    private AnukeHexedGenerator() {}

    @SuppressWarnings("ExtractMethodRecommender")
    @Override
    public HexedMapContext generate() {
        final var context = new SimpleHexedMapContext();
        context.resize(WIDTH, HEIGHT);

        final int seed1 = Mathf.random(0, 10000);
        final int seed2 = Mathf.random(0, 10000);

        // Generate ores

        final var ores = OreGeneratorFunction.getDefaultOreFunctions();
        for (final var function : ores) {
            function.setThreshold(function.getThreshold() - 0.05F);
        }

        {
            final var function = new OreGeneratorFunction();
            function.setOre(Blocks.oreScrap);
            function.setScale(function.getScale() + 2 / 2.1F);
            ores.add(0, function);
        }

        final var random = new Random();
        ores.forEach(function -> function.setSeed(random.nextInt()));

        context.forEachTile((x, y, tile) -> {
            final int temp = Mathf.clamp(
                    (int) ((Simplex.noise2d(seed1, 12, 0.6, 1.0 / 400, x, y) - 0.5) * 10 * BLOCKS.length),
                    0,
                    BLOCKS.length - 1);
            final int elev = Mathf.clamp(
                    (int) (((Simplex.noise2d(seed2, 12, 0.6, 1.0 / 700, x, y) - 0.5) * 10 + 0.15f) * BLOCKS[0].length),
                    0,
                    BLOCKS[0].length - 1);

            tile.setFloor(FLOORS[temp][elev].asFloor());
            tile.setBlock(BLOCKS[temp][elev]);
        });

        for (final var function : ores) {
            context.forEachTile(function);
        }

        // Generate hexes

        final List<Hex> hexes = new ArrayList<>();
        final double h = Math.sqrt(3) * SPACING / 2;
        // base horizontal spacing=1.5w
        // offset = 3/4w
        for (int x = 0; x < WIDTH / SPACING - 2; x++) {
            for (int y = 0; y < HEIGHT / (h / 2) - 2; y++) {
                final int cx = (int) (x * SPACING * 1.5 + (y % 2) * SPACING * 3.0 / 4) + SPACING / 2;
                final int cy = (int) (y * h / 2) + SPACING / 2;
                hexes.add(new Hexagon(y + (int) (x * (HEIGHT / (h / 2) - 2)), cx, cy, DIAMETER));
            }
        }
        context.setHexes(hexes);

        // Create hex boundaries

        for (final var hex : context.getHexes()) {
            final int x = hex.getTileX();
            final int y = hex.getTileY();

            Geometry.circle(x, y, WIDTH, HEIGHT, DIAMETER, (cx, cy) -> {
                if (Intersector.isInsideHexagon(x, y, DIAMETER, cx, cy)) {
                    context.getTile(cx, cy).setBlock(Blocks.air);
                }
            });

            final float angle = 360f / 3 / 2f - 90;
            for (int a = 0; a < 3; a++) {
                final float f = a * 120f + angle;

                final var vector = new Vec2().trnsExact(f, SPACING + 12);
                if (Structs.inBounds(x + (int) vector.x, y + (int) vector.y, WIDTH, HEIGHT)) {
                    vector.trnsExact(f, SPACING / 2f + 7);
                    Bresenham2.line(
                            x,
                            y,
                            x + (int) vector.x,
                            y + (int) vector.y,
                            (cx, cy) ->
                                    Geometry.circle(cx, cy, WIDTH, HEIGHT, 3, (c2x, c2y) -> context.getTile(c2x, c2y)
                                            .setBlock(Blocks.air)));
                }
            }
        }

        // Add some boulders :)

        context.forEachTile((x, y, tile) -> {
            if (!Mathf.chance(0.03) || tile.getBlock() != Blocks.air) return;
            if (tile.getFloor() == Blocks.sand) {
                tile.setBlock(Blocks.sandBoulder);
            } else if (tile.getFloor() == Blocks.stone) {
                tile.setBlock(Blocks.boulder);
            } else if (tile.getFloor() == Blocks.shale) {
                tile.setBlock(Blocks.shaleBoulder);
            } else if (tile.getFloor() == Blocks.darksand) {
                tile.setBlock(Blocks.boulder);
            } else if (tile.getFloor() == Blocks.moss) {
                tile.setBlock(Blocks.sporeCluster);
            } else if (tile.getFloor() == Blocks.ice) {
                tile.setBlock(Blocks.ice);
            } else if (tile.getFloor() == Blocks.snow) {
                tile.setBlock(Blocks.snowBoulder);
            }
        });

        // Apply core radius rule

        final var rules = context.getRules();
        rules.polygonCoreProtection = false;
        rules.enemyCoreBuildRadius = DIAMETER / 2F * Vars.tilesize;
        context.setRules(rules);
        return context;
    }
}
