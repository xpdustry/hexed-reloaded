// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed.model;

import arc.math.geom.Intersector;

record Hexagon(int identifier, int x, int y, int diameter) implements Hex {

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
        return this.diameter;
    }

    @Override
    public boolean contains(final int x, final int y) {
        return Intersector.isInsideHexagon(this.x, this.y, this.diameter, x, y);
    }
}
