// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed.generation;

import mindustry.game.Rules;

public interface MapContext {

    void resize(final int width, final int height);

    int getWidth();

    int getHeight();

    MapTile getTile(int x, int y);

    Rules getRules();

    void setRules(final Rules rules);

    String getMapName();

    void setMapName(final String name);

    void forEachTile(final TileConsumer action);

    void forEachTile(final int x, final int y, final int w, final int h, final TileConsumer action);
}
