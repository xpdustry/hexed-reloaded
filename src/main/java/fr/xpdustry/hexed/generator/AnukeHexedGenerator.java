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
package fr.xpdustry.hexed.generator;

import arc.math.Mathf;
import arc.math.geom.Bresenham2;
import arc.math.geom.Geometry;
import arc.math.geom.Intersector;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Structs;
import arc.util.noise.Simplex;
import fr.xpdustry.hexed.model.Hex;
import fr.xpdustry.hexed.model.Hexagon;
import fr.xpdustry.nucleus.mindustry.testing.map.MapGenerator;
import java.util.ArrayList;
import java.util.List;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.maps.filters.GenerateFilter;
import mindustry.maps.filters.GenerateFilter.GenerateInput;
import mindustry.maps.filters.OreFilter;
import mindustry.world.Block;

// Original code from Anuke
public final class AnukeHexedGenerator implements MapGenerator<HexedGeneratorContext> {

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

    @Override
    public HexedGeneratorContext createContext() {
        return new SimpleHexedGeneratorContext();
    }

    @Override
    public void generate(final HexedGeneratorContext context) {
        context.reset(516, 516);

        int seed1 = Mathf.random(0, 10000);
        int seed2 = Mathf.random(0, 10000);

        // Generate ores

        Seq<GenerateFilter> ores = new Seq<>();
        Vars.maps.addDefaultOres(ores);
        ores.each(o -> ((OreFilter) o).threshold -= 0.05f);
        ores.insert(0, new OreFilter() {
            {
                ore = Blocks.oreScrap;
                scl += 2 / 2.1F;
            }
        });
        ores.each(GenerateFilter::randomize);
        GenerateInput in = new GenerateInput();

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                int temp = Mathf.clamp(
                        (int) ((Simplex.noise2d(seed1, 12, 0.6, 1.0 / 400, x, y) - 0.5) * 10 * BLOCKS.length),
                        0,
                        BLOCKS.length - 1);
                int elev = Mathf.clamp(
                        (int) (((Simplex.noise2d(seed2, 12, 0.6, 1.0 / 700, x, y) - 0.5) * 10 + 0.15f)
                                * BLOCKS[0].length),
                        0,
                        BLOCKS[0].length - 1);

                Block floor = FLOORS[temp][elev];
                Block wall = BLOCKS[temp][elev];
                Block ore = Blocks.air;

                for (GenerateFilter f : ores) {
                    in.floor = Blocks.stone;
                    in.block = wall;
                    in.overlay = ore;
                    in.x = x;
                    in.y = y;
                    in.width = WIDTH;
                    in.height = HEIGHT;
                    f.apply(in);
                    if (in.overlay != Blocks.air) {
                        ore = in.overlay;
                    }
                }

                context.setTile(x, y, floor, ore, wall);
            }
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

        for (final var hex : hexes) {
            int x = hex.getTileX();
            int y = hex.getTileY();
            Geometry.circle(x, y, WIDTH, HEIGHT, DIAMETER, (cx, cy) -> {
                if (Intersector.isInsideHexagon(x, y, DIAMETER, cx, cy)) {
                    context.setBlock(cx, cy, Blocks.air, Team.derelict);
                }
            });
            float angle = 360f / 3 / 2f - 90;
            for (int a = 0; a < 3; a++) {
                float f = a * 120f + angle;

                final var vector = new Vec2().trnsExact(f, SPACING + 12);
                if (Structs.inBounds(x + (int) vector.x, y + (int) vector.y, WIDTH, HEIGHT)) {
                    vector.trnsExact(f, SPACING / 2f + 7);
                    Bresenham2.line(
                            x,
                            y,
                            x + (int) vector.x,
                            y + (int) vector.y,
                            (cx, cy) -> Geometry.circle(
                                    cx,
                                    cy,
                                    WIDTH,
                                    HEIGHT,
                                    3,
                                    (c2x, c2y) -> context.setBlock(c2x, c2y, Blocks.air, Team.derelict)));
                }
            }
        }

        // Add some boulders :)

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (Mathf.chance(0.03)) {
                    context.setTileIf(
                            x,
                            y,
                            tile -> tile.block() == Blocks.air && tile.floor() == Blocks.sand,
                            Blocks.sandBoulder);
                    context.setTileIf(
                            x, y, tile -> tile.block() == Blocks.air && tile.floor() == Blocks.stone, Blocks.boulder);
                    context.setTileIf(
                            x,
                            y,
                            tile -> tile.block() == Blocks.air && tile.floor() == Blocks.shale,
                            Blocks.shaleBoulder);
                    context.setTileIf(
                            x,
                            y,
                            tile -> tile.block() == Blocks.air && tile.floor() == Blocks.darksand,
                            Blocks.boulder);
                    context.setTileIf(
                            x,
                            y,
                            tile -> tile.block() == Blocks.air && tile.floor() == Blocks.moss,
                            Blocks.sporeCluster);
                    context.setTileIf(
                            x, y, tile -> tile.block() == Blocks.air && tile.floor() == Blocks.ice, Blocks.snowBoulder);
                    context.setTileIf(
                            x,
                            y,
                            tile -> tile.block() == Blocks.air && tile.floor() == Blocks.snow,
                            Blocks.snowBoulder);
                }
            }
        }

        // Apply core radius rule

        final var rules = context.getRules();
        rules.polygonCoreProtection = false;
        rules.enemyCoreBuildRadius = DIAMETER / 2F * Vars.tilesize;
        context.setRules(rules);
    }
}
