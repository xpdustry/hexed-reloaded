/*
 * This file is part of HexedPluginReloaded. A reimplementation of the hex gamemode, with more features and better performances.
 *
 * MIT License
 *
 * Copyright (c) 2023 Xpdustry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.world.blocks.storage.CoreBlock;
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

    public void setHexes(final List<Hex> hexes) {
        this.hexes.clear();
        this.controllers.clear();
        this.dying.clear();
        this.positions.clear();
        this.spawnTimers.clear();
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

    /*
    public IntIntMap getLeaderboard() {
        final var leaderboard = new IntIntMap();
        for (final var hex : this.hexes) {
            final var team = this.getController(hex);
            if (team == null) {
                continue;
            }
            leaderboard.increment(team.id, 1);
        }
        return leaderboard;
    }
     */

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
        return (this.progress
                                .get(Point2.pack(hex.getTileX(), hex.getTileY()), () -> new IntFloatMap(4))
                                .get(team.id)
                        / ITEM_REQUIREMENT)
                * 100F;
    }
}
