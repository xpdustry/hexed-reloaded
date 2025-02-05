/*
 * HexedReloaded, a reimplementation of the hexed gamemode from Anuke,
 * with more features and better performances.
 *
 * Copyright (C) 2025  Xpdustry
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

import com.xpdustry.hexed.HexedCaptureProgress;
import com.xpdustry.hexed.model.Hex;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import mindustry.game.Schematics;

public interface HexedMapContext extends MapContext {

    ImmutableSchematic DEFAULT_BASE_SCHEMATIC = loadDefaultBaseSchematic();

    String HEXED_PRESENCE_FLAG = "xpdustry:hexed-reloaded";

    Duration DEFAULT_GAME_DURATION = Duration.ofMinutes(90L);

    List<Hex> getHexes();

    void setHexes(final List<Hex> hexes);

    Duration getDuration();

    void setDuration(final Duration duration);

    ImmutableSchematic getBaseSchematic();

    void setBaseSchematic(final ImmutableSchematic schematic);

    HexedCaptureProgress getCaptureCalculator();

    void setCaptureCalculator(final HexedCaptureProgress calculator);

    private static ImmutableSchematic loadDefaultBaseSchematic() {
        try (final var stream = SimpleHexedMapContext.class.getResourceAsStream("/com/xpdustry/hexed/default.msch")) {
            return new ImmutableSchematic(Schematics.read(Objects.requireNonNull(stream)));
        } catch (final Exception e) {
            throw new RuntimeException("Failed to load the default base schematic.", e);
        }
    }
}
