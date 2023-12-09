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

import mindustry.game.Rules;

public class SimpleMapContext implements MapContext {

    private int width = 1;
    private int height = 1;
    private MapTile[][] tiles = {{new MapTile()}};
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
    public void forEachTile(final TileConsumer action) {
        for (int y = 0; y < this.tiles.length; y++) {
            for (int x = 0; x < this.tiles[y].length; x++) {
                action.accept(x, y, this.tiles[y][x]);
            }
        }
    }

    @Override
    public Rules getRules() {
        return this.rules.copy();
    }

    @Override
    public void setRules(final Rules rules) {
        this.rules = rules.copy();
    }
}
