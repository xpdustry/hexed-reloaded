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

import mindustry.game.Rules;

public class SimpleMapContext implements MapContext {

    private int width = 1;
    private int height = 1;
    private MapTile[][] tiles = {{new MapTile()}};
    private String name = "Unknown";
    private Rules rules = new Rules();

    @Override
    public void resize(final int width, final int height) {
        if (width < 1) throw new RuntimeException("Width cannot be lower than zero: " + width);
        if (height < 1) throw new RuntimeException("Height cannot be lower then zero: " + height);

        this.tiles = new MapTile[width][height];
        for (int y = 0; y < this.tiles.length; y++) {
            for (int x = 0; x < this.tiles[y].length; x++) {
                this.tiles[y][x] = new MapTile();
            }
        }

        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public MapTile getTile(final int x, final int y) {
        return this.tiles[y][x];
    }

    @Override
    public Rules getRules() {
        return this.rules.copy();
    }

    @Override
    public void setRules(final Rules rules) {
        this.rules = rules.copy();
    }

    @Override
    public String getMapName() {
        return this.name;
    }

    @Override
    public void setMapName(final String name) {
        this.name = name;
    }

    @Override
    public void forEachTile(final TileConsumer action) {
        for (int y = 0; y < this.tiles.length; y++) {
            for (int x = 0; x < this.tiles[y].length; x++) {
                action.accept(x, y, this.tiles[y][x]);
            }
        }
    }

    @Override
    public void forEachTile(final int x, final int y, final int w, final int h, final TileConsumer action) {
        for (int ry = y; ry < y + h; ry++) {
            for (int rx = x; rx < x + w; rx++) {
                action.accept(rx, ry, this.tiles[ry][rx]);
            }
        }
    }
}
