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

import java.util.Random;

public abstract class GeneratorFunction implements TileConsumer {

    private static final Random RANDOM = new Random();
    private int seed = 0;

    public int getSeed() {
        return this.seed;
    }

    public void setSeed(final int seed) {
        this.seed = seed;
    }

    public void randomize() {
        this.seed = RANDOM.nextInt(1_000_000_000);
    }
}
