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

import arc.math.geom.Point2;
import arc.struct.IntFloatMap;
import arc.struct.IntMap;
import arc.util.Time;
import arc.util.Timekeeper;
import com.xpdustry.hexed.generation.ImmutableSchematic;
import com.xpdustry.hexed.model.Hex;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mindustry.Vars;
import mindustry.game.Team;
import org.checkerframework.checker.nullness.qual.Nullable;

final class HexedStateImpl implements HexedState {

    private final Map<Hex, Team> controllers = new HashMap<>();
    private final List<Hex> hexes;
    private final Set<Team> dying = new HashSet<>();
    private final IntMap<Hex> positions = new IntMap<>();
    private final IntMap<Timekeeper> spawnTimers = new IntMap<>();
    private final IntMap<IntFloatMap> progress = new IntMap<>();
    private final Duration duration;
    private float counter = 0f;
    private final ImmutableSchematic base;
    private final HexedCaptureProgress calculator;

    HexedStateImpl(
            final ImmutableSchematic base,
            final HexedCaptureProgress calculator,
            final List<Hex> hexes,
            final Duration duration) {
        this.base = base;
        this.calculator = calculator;
        this.duration = duration;
        this.hexes = List.copyOf(hexes);
        for (final var hex : this.hexes) {
            this.positions.put(Point2.pack(hex.getTileX(), hex.getTileY()), hex);
        }
    }

    @Override
    public List<Hex> getHexes() {
        return this.hexes;
    }

    @Override
    public List<Hex> getControlled(final Team team) {
        return this.hexes.stream()
                .filter(hex -> this.getController(hex) == team)
                .toList();
    }

    @Override
    public @Nullable Team getController(final Hex hex) {
        return this.controllers.get(hex);
    }

    @Override
    public @Nullable Hex getHex(final int x, final int y) {
        return this.positions.get(Point2.pack(x, y));
    }

    @Override
    public boolean isAvailable(final Hex hex) {
        return (this.getController(hex) == null)
                && this.spawnTimers
                        .get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new Timekeeper(6 * 60))
                        .get();
    }

    public void resetSpawnTimer(final Hex hex) {
        this.spawnTimers
                .get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new Timekeeper(6 * 60))
                .reset();
    }

    @Override
    public boolean isDying(final Team team) {
        return this.dying.contains(team);
    }

    public void setDying(final Team team, final boolean dying) {
        if (dying) {
            this.dying.add(team);
        } else {
            this.dying.remove(team);
        }
    }

    @Override
    public Duration getDuration() {
        return this.duration;
    }

    @Override
    public void setCounter(final Duration counter) {
        this.counter = counter.toMillis() * Time.toSeconds;
    }

    @Override
    public Duration getCounter() {
        return Duration.ofMillis((long) ((this.counter / Time.toSeconds) * 1000L));
    }

    @Override
    public void incrementCounter(final float delta) {
        this.counter += delta;
    }

    @Override
    public ImmutableSchematic getBaseSchematic() {
        return this.base;
    }

    @Override
    public float getProgress(final Hex hex, final Team team) {
        final var progress = this.getProgress0(hex, team);
        final var controller = this.getController(hex);
        if (controller != null && controller != team) {
            return (progress / this.getProgress0(hex, controller)) * 100F;
        }
        return progress;
    }

    private float getProgress0(final Hex hex, final Team team) {
        return this.progress
                        .get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new IntFloatMap(4))
                        .get(team.id)
                * 100F;
    }

    public void updateProgress(final Hex hex) {
        final var progress = this.progress.get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new IntFloatMap(4));
        progress.clear();
        this.calculator.calculate(hex, progress);
        final var data = Vars.state.teams.getActive().max(t -> progress.get(t.team.id));
        if (data != null && progress.get(data.team.id) >= 1F) {
            this.controllers.put(hex, data.team);
        } else {
            this.controllers.put(hex, null);
        }
    }
}
