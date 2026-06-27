// SPDX-License-Identifier: GPL-3.0-only
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
