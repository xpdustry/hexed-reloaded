// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed.model;

record Rectangle(int identifier, int x, int y, int w, int h) implements Hex {

    @Override
    public int getIdentifier() {
        return this.identifier;
    }

    @Override
    public int getTileX() {
        return this.x;
    }

    @Override
    public int getTileY() {
        return this.y;
    }

    @Override
    public int getTileDiameter() {
        return (this.w + this.h) / 2;
    }

    @Override
    public boolean contains(final int x, final int y) {
        final var hw = (this.w / 2);
        final var hh = (this.h / 2);
        return x >= this.x - hw && x < this.x + hw && y >= this.y - hh && y < this.y + hh;
    }
}
