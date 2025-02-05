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
package com.xpdustry.hexed;

import com.xpdustry.hexed.generation.ImmutableSchematic;
import com.xpdustry.hexed.model.Hex;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mindustry.game.Team;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface HexedState {

    List<Hex> getHexes();

    List<Hex> getControlled(final Team team);

    @Nullable Team getController(final Hex hex);

    @Nullable Hex getHex(final int x, final int y);

    boolean isAvailable(final Hex hex);

    boolean isDying(final Team team);

    ImmutableSchematic getBaseSchematic();

    float getProgress(final Hex hex, final Team team);

    Duration getDuration();

    Duration getCounter();

    void setCounter(final Duration counter);

    void incrementCounter(final float delta);

    default Map<Team, Integer> getLeaderboard() {
        return this.getHexes().stream()
                .map(this::getController)
                .filter(team -> team != null && team != Team.derelict)
                .collect(Collectors.toMap(team -> team, team -> 1, Integer::sum));
    }
}
