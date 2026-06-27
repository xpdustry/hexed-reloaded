// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed.model;

import mindustry.Vars;

public interface Hex {

    static Hex hexagon(final int identifier, final int x, final int y, final int diameter) {
        return new Hexagon(identifier, x, y, diameter);
    }

    static Hex square(final int identifier, final int x, final int y, final int size) {
        return new Rectangle(identifier, x, y, size, size);
    }

    static Hex rectangle(final int identifier, final int x, final int y, final int width, final int height) {
        return new Rectangle(identifier, x, y, width, height);
    }

    default float getX() {
        return this.getTileX() * Vars.tilesize;
    }

    default float getY() {
        return this.getTileY() * Vars.tilesize;
    }

    default float getDiameter() {
        return this.getTileDiameter() * Vars.tilesize;
    }

    default float getRadius() {
        return this.getDiameter() / 2F;
    }

    default int getTileRadius() {
        return this.getTileDiameter() / 2;
    }

    int getIdentifier();

    int getTileX();

    int getTileY();

    int getTileDiameter();

    boolean contains(final int x, final int y);
}
