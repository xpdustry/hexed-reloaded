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
package fr.xpdustry.hexed;

import arc.math.geom.Point2;
import arc.struct.IntFloatMap;
import arc.struct.IntMap;
import arc.util.Timekeeper;
import fr.xpdustry.hexed.model.Hex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.world.blocks.storage.CoreBlock;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class HexedState {

    // Item requirement to capture a hex
    private static final int ITEM_REQUIREMENT = 210;

    public static final int GAME_DURATION = 60 * 60 * 90;

    private final Map<Hex, Team> controllers = new HashMap<>();
    private final List<Hex> hexes = new ArrayList<>();
    private final Set<Team> dying = new HashSet<>();
    private final IntMap<Hex> positions = new IntMap<>();
    private final IntMap<Timekeeper> spawnTimers = new IntMap<>();
    private final IntMap<IntFloatMap> progress = new IntMap<>();
    private float counter = 0f;
    private @MonotonicNonNull Schematic loadout;

    public void setHexes(final List<Hex> hexes) {
        this.hexes.clear();
        this.controllers.clear();
        this.dying.clear();
        this.positions.clear();
        this.spawnTimers.clear();
        this.progress.clear();
        this.hexes.addAll(hexes);
        for (final var hex : this.hexes) {
            this.positions.put(Point2.pack(hex.getTileX(), hex.getTileY()), hex);
        }
    }

    public void setCounter(final float counter) {
        this.counter = counter;
    }

    public float getCounter() {
        return this.counter;
    }

    public void incrementCounter(final float delta) {
        this.counter += delta;
    }

    public List<Hex> getHexes() {
        return Collections.unmodifiableList(hexes);
    }

    public List<Hex> getControlled(final Team team) {
        return this.hexes.stream().filter(hex -> getController(hex) == team).toList();
    }

    public @Nullable Team getController(final Hex hex) {
        return controllers.get(hex);
    }

    public @Nullable Hex getHex(final int x, final int y) {
        return this.positions.get(Point2.pack(x, y));
    }

    public boolean isDying(final Team team) {
        return dying.contains(team);
    }

    public void setDying(final Team team, final boolean dying) {
        if (dying) {
            this.dying.add(team);
        } else {
            this.dying.remove(team);
        }
    }

    public boolean canSpawn(final Hex hex) {
        return this.spawnTimers
                .get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new Timekeeper(6 * 60))
                .get();
    }

    public void resetSpawnTimer(final Hex hex) {
        this.spawnTimers
                .get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new Timekeeper(6 * 60))
                .reset();
    }

    public Schematic getLoadout() {
        return loadout;
    }

    public void setLoadout(final Schematic loadout) {
        this.loadout = loadout;
    }

    public Map<Team, Integer> getLeaderboard() {
        return this.hexes.stream()
                .map(this::getController)
                .filter(team -> team != null && team != Team.derelict)
                .collect(Collectors.toMap(team -> team, team -> 1, Integer::sum));
    }

    public void updateProgress(final Hex hex) {
        final var center = Vars.world.tile(hex.getTileX(), hex.getTileY());

        if (center.block() instanceof CoreBlock) {
            controllers.put(hex, center.team());
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
            controllers.put(hex, data.team);
            return;
        }

        controllers.put(hex, null);
    }

    public float getProgress(final Hex hex, final Team team) {
        final var progress = getProgress0(hex, team);
        final var controller = getController(hex);
        if (controller != null && controller != team) {
            return (progress / getProgress0(hex, controller)) * 100F;
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
}
