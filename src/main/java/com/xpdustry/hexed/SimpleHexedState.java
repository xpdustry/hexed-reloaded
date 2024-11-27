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
package com.xpdustry.hexed;

import arc.math.geom.Point2;
import arc.struct.IntFloatMap;
import arc.struct.IntMap;
import arc.util.Time;
import arc.util.Timekeeper;
import com.xpdustry.hexed.api.HexedState;
import com.xpdustry.hexed.api.generation.ImmutableSchematic;
import com.xpdustry.hexed.api.model.Hex;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.world.blocks.storage.CoreBlock;
import org.checkerframework.checker.nullness.qual.Nullable;

final class SimpleHexedState implements HexedState {

    // Item requirement to capture a hex
    private static final int ITEM_REQUIREMENT = 210;

    private final Map<Hex, Team> controllers = new HashMap<>();
    private final List<Hex> hexes;
    private final Set<Team> dying = new HashSet<>();
    private final IntMap<Hex> positions = new IntMap<>();
    private final IntMap<Timekeeper> spawnTimers = new IntMap<>();
    private final IntMap<IntFloatMap> progress = new IntMap<>();
    private final Duration duration;
    private float counter = 0f;
    private final ImmutableSchematic base;

    SimpleHexedState(final ImmutableSchematic base, final List<Hex> hexes, final Duration duration) {
        this.base = base;
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
        return (this.progress
                                .get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new IntFloatMap(4))
                                .get(team.id)
                        / ITEM_REQUIREMENT)
                * 100F;
    }

    public void updateProgress(final Hex hex) {
        final var center = Vars.world.tile(hex.getTileX(), hex.getTileY());

        if (center.block() instanceof CoreBlock) {
            this.controllers.put(hex, center.team());
        }

        final var progress = this.progress.get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new IntFloatMap(4));
        progress.clear();
        Groups.unit
                .intersect(
                        hex.getX() - hex.getRadius(),
                        hex.getY() - hex.getRadius(),
                        hex.getDiameter(),
                        hex.getDiameter())
                .each(u -> {
                    if (!u.isPlayer() && hex.contains(u.tileX(), u.tileY())) {
                        progress.increment(u.team().id, u.health() / 10F);
                    }
                });

        for (int cx = hex.getTileX() - hex.getTileRadius(); cx < hex.getTileX() + hex.getTileRadius(); cx++) {
            for (int cy = hex.getTileY() - hex.getTileRadius(); cy < hex.getTileY() + hex.getTileRadius(); cy++) {
                final var tile = Vars.world.tile(cx, cy);
                if (tile != null
                        && tile.synthetic()
                        && hex.contains(tile.x, tile.y)
                        && tile.block().requirements != null) {
                    for (final var stack : tile.block().requirements) {
                        progress.increment(tile.team().id, stack.amount * stack.item.cost);
                    }
                }
            }
        }

        final var data = Vars.state.teams.getActive().max(t -> progress.get(t.team.id));
        if (data != null && progress.get(data.team.id) >= ITEM_REQUIREMENT) {
            this.controllers.put(hex, data.team);
            return;
        }

        this.controllers.put(hex, null);
    }
}
