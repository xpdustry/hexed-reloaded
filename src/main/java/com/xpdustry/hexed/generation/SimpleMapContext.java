// SPDX-License-Identifier: GPL-3.0-only
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

        this.tiles = new MapTile[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
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
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
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
