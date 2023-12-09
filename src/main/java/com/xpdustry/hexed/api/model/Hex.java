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

import mindustry.Vars;

public interface Hex {

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
