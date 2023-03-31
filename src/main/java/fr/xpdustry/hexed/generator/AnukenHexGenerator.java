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
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Structs;
import arc.util.Tmp;
import arc.util.noise.Simplex;
import fr.xpdustry.hexed.model.Hex;
import fr.xpdustry.hexed.model.Hexagon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.maps.Map;
import mindustry.maps.filters.GenerateFilter;
import mindustry.maps.filters.GenerateFilter.GenerateInput;
import mindustry.maps.filters.OreFilter;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;

public final class AnukenHexGenerator implements HexGenerator {

    private static final int DIAMETER = 74;
    private static final int SPACING = 78;
    private static final int WIDTH = 516;
    private static final int HEIGHT = 516;

    // elevation --->
    // temperature
    // |
    // v
    private static final Block[][] floors = {
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

    private static final Block[][] blocks = {
        {Blocks.stoneWall, Blocks.stoneWall, Blocks.sandWall, Blocks.sandWall, Blocks.pine, Blocks.pine},
        {Blocks.stoneWall, Blocks.stoneWall, Blocks.duneWall, Blocks.duneWall, Blocks.pine, Blocks.pine},
        {Blocks.stoneWall, Blocks.stoneWall, Blocks.duneWall, Blocks.duneWall, Blocks.pine, Blocks.pine},
        {Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.stoneWall},
        {Blocks.iceWall, Blocks.snowWall, Blocks.snowWall, Blocks.snowWall, Blocks.stoneWall, Blocks.saltWall}
    };

    @Override
    public List<Hex> generate(final Tiles tiles) {
        int seed1 = Mathf.random(0, 10000);
        int seed2 = Mathf.random(0, 10000);
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
        final List<Hex> hexes = getHex();

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                int temp = Mathf.clamp(
                        (int) ((Simplex.noise2d(seed1, 12, 0.6, 1.0 / 400, x, y) - 0.5) * 10 * blocks.length),
                        0,
                        blocks.length - 1);
                int elev = Mathf.clamp(
                        (int) (((Simplex.noise2d(seed2, 12, 0.6, 1.0 / 700, x, y) - 0.5) * 10 + 0.15f)
                                * blocks[0].length),
                        0,
                        blocks[0].length - 1);

                Block floor = floors[temp][elev];
                Block wall = blocks[temp][elev];
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

                tiles.set(x, y, new Tile(x, y, floor.id, ore.id, wall.id));
            }
        }

        for (final var hex : hexes) {
            int x = hex.getTileX();
            int y = hex.getTileY();
            Geometry.circle(x, y, WIDTH, HEIGHT, DIAMETER, (cx, cy) -> {
                if (Intersector.isInsideHexagon(x, y, DIAMETER, cx, cy)) {
                    Tile tile = tiles.getn(cx, cy);
                    tile.setBlock(Blocks.air);
                }
            });
            float angle = 360f / 3 / 2f - 90;
            for (int a = 0; a < 3; a++) {
                float f = a * 120f + angle;

                Tmp.v1.trnsExact(f, SPACING + 12);
                if (Structs.inBounds(x + (int) Tmp.v1.x, y + (int) Tmp.v1.y, WIDTH, HEIGHT)) {
                    Tmp.v1.trnsExact(f, SPACING / 2f + 7);
                    Bresenham2.line(
                            x,
                            y,
                            x + (int) Tmp.v1.x,
                            y + (int) Tmp.v1.y,
                            (cx, cy) -> Geometry.circle(cx, cy, WIDTH, HEIGHT, 3, (c2x, c2y) -> tiles.getn(c2x, c2y)
                                    .setBlock(Blocks.air)));
                }
            }
        }

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Tile tile = tiles.getn(x, y);
                Block wall = tile.block();
                Block floor = tile.floor();

                if (wall == Blocks.air) {
                    if (Mathf.chance(0.03)) {
                        if (floor == Blocks.sand) {
                            wall = Blocks.sandBoulder;
                        } else if (floor == Blocks.stone) {
                            wall = Blocks.boulder;
                        } else if (floor == Blocks.shale) {
                            wall = Blocks.shaleBoulder;
                        } else if (floor == Blocks.darksand) {
                            wall = Blocks.boulder;
                        } else if (floor == Blocks.moss) {
                            wall = Blocks.sporeCluster;
                        } else if (floor == Blocks.ice) {
                            wall = Blocks.snowBoulder;
                        } else if (floor == Blocks.snow) {
                            wall = Blocks.snowBoulder;
                        }
                    }
                }
                tile.setBlock(wall);
            }
        }

        Vars.state.map = new Map(StringMap.of("name", "Hex"));

        return Collections.unmodifiableList(hexes);
    }

    @Override
    public int getWorldWidth() {
        return 516;
    }

    @Override
    public int getWorldHeight() {
        return 516;
    }

    public List<Hex> getHex() {
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
        return hexes;
    }
}
