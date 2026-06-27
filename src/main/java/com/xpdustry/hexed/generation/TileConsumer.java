// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed.generation;

import java.util.List;

@FunctionalInterface
public interface TileConsumer {

    static TileConsumer aggregate(final List<? extends TileConsumer> consumers) {
        return (x, y, tile) -> {
            for (final var consumer : consumers) {
                consumer.accept(x, y, tile);
            }
        };
    }

    void accept(int x, int y, MapTile tile);
}
