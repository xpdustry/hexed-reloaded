// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed;

import com.xpdustry.hexed.generation.ImmutableSchematic;
import com.xpdustry.hexed.model.Hex;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mindustry.game.Team;
import org.jspecify.annotations.Nullable;

public interface HexedState {

    List<Hex> getHexes();

    List<Hex> getControlled(final Team team);

    @Nullable Team getController(final Hex hex);

    @Nullable Hex getHex(final int x, final int y);

    boolean isAvailable(final Hex hex);

    boolean isAvailable(final Team team);

    void markUnavailableFor(final Team team, final int seconds);

    ImmutableSchematic getBaseSchematic();

    float getProgress(final Hex hex, final Team team);

    Duration getDuration();

    Duration getCounter();

    void setCounter(final Duration counter);

    void incrementCounter(final float delta);

    @SuppressWarnings("NullableProblems") // "team != null && team != Team.derelict" is confusing idea...
    default Map<Team, Integer> getLeaderboard() {
        return this.getHexes().stream()
                .map(this::getController)
                .filter(team -> team != null && !team.equals(Team.derelict))
                .collect(Collectors.toMap(team -> team, team -> 1, Integer::sum));
    }
}
