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
package com.xpdustry.hexed.api.model;

import arc.math.geom.Intersector;

public final class SimpleHex implements Hex {

    private final int identifier;
    private final int x;
    private final int y;
    private final int diameter;

    public SimpleHex(final int identifier, final int x, final int y, final int diameter) {
        this.identifier = identifier;
        this.x = x;
        this.y = y;
        this.diameter = diameter;
    }

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
