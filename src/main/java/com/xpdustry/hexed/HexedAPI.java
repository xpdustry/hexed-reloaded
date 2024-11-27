/*
 * HexedReloaded, A reimplementation of the hexed gamemode from Anuke, with more features and better performances.
 *
 * Copyright (C) 2024  Xpdustry
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
package com.xpdustry.hexed;

import com.xpdustry.hexed.generation.HexedMapContext;
import com.xpdustry.hexed.generation.ImmutableSchematic;
import com.xpdustry.hexed.generation.MapGenerator;
import java.time.Duration;
import java.util.Map;

public interface HexedAPI {

    String HEXED_PRESENCE_FLAG = "xpdustry:hexed-reloaded";

    Duration DEFAULT_GAME_DURATION = Duration.ofMinutes(90L);

    HexedState getHexedState();

    void registerGenerator(final String name, MapGenerator<HexedMapContext> generator);

    Map<String, MapGenerator<HexedMapContext>> getGenerators();

    ImmutableSchematic getDefaultBaseSchematic();

    boolean isEnabled();

    boolean start(final MapGenerator<HexedMapContext> generator);
}
