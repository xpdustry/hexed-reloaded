// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed;

import arc.math.Mathf;
import arc.util.Interval;
import arc.util.Time;
import com.xpdustry.distributor.api.Distributor;
import com.xpdustry.distributor.api.annotation.EventHandler;
import com.xpdustry.distributor.api.collection.MindustryCollections;
import com.xpdustry.distributor.api.plugin.PluginListener;
import com.xpdustry.hexed.event.HexCaptureEvent;
import com.xpdustry.hexed.event.HexLostEvent;
import com.xpdustry.hexed.event.HexPlayerJoinEvent;
import com.xpdustry.hexed.event.HexPlayerQuitEvent;
import com.xpdustry.hexed.event.HexedGameOverEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.blocks.storage.CoreBlock;

final class HexedLogic implements PluginListener {

    private static final int CONTROLLER_TIMER = 0;
    private static final int PLAYER_TIMER = 1;

    private final Interval interval = new Interval(2);
    private final HexedPluginReloaded hexed;

    public HexedLogic(final HexedPluginReloaded hexed) {
        this.hexed = hexed;
    }

    @Override
    public void onPluginInit() {
        Vars.netServer.assigner = new HexedTeamAssigner(this.hexed, Vars.netServer.assigner);
    }

    @EventHandler
    public void onPlayerJoin(final EventType.PlayerJoin event) {
        Distributor.get().getEventBus().post(new HexPlayerJoinEvent(event.player));
    }

    @EventHandler
    public void onPlayerJoin(final HexPlayerJoinEvent event) {
        if (!this.hexed.isEnabled()) {
            return;
        }
        if (event.player().team().equals(Team.derelict)) {
            event.player().team(Vars.netServer.assignTeam(event.player()));
        }
        if (event.player().team().equals(Team.derelict)) {
            return;
        }

        final var hexes = this.hexed.getHexedState().getHexes().stream()
                .filter(hex -> this.hexed.getHexedState().getController(hex) == null
                        && this.hexed.getHexedState().isAvailable(hex))
                .toList();

        if (hexes.isEmpty()) {
            Call.infoMessage(
                    event.player().con(),
                    "There are currently no empty hex spaces available.\nAssigning into spectator mode.");
            if (event.player().unit() != null) {
                event.player().unit().kill();
            }
            event.player().team(Team.derelict);
        } else {
            final var hex = hexes.get(Mathf.random(0, hexes.size() - 1));
            this.placeBaseSchematic(event.player(), hex.getTileX(), hex.getTileY());
            this.hexed.getHexedState0().updateProgress(hex);
        }
    }

    @EventHandler
    public void onPlayerLeave(final EventType.PlayerLeave event) {
        Distributor.get().getEventBus().post(new HexPlayerQuitEvent(event.player, event.player.team(), false));
    }

    @EventHandler
    public void onPlayerQuit(final HexPlayerQuitEvent event) {
        if (this.hexed.isEnabled()) {
            if (!event.team().equals(Team.derelict)) {
                event.team().data().destroyToDerelict();
                this.hexed.getHexedState().markUnavailableFor(event.team(), 10);
            }
            if (event.player().unit() != null) {
                event.player().unit().kill();
            }
            event.player().team(Team.derelict);
            event.player().clearUnit();
        }
    }

    @EventHandler
    public void onBlockDestroy(final EventType.BlockDestroyEvent event) {
        // reset last spawn times so this hex becomes vacant for a while.
        if (this.hexed.isEnabled() && event.tile.block() instanceof CoreBlock) {
            final var hex = this.hexed.getHexedState().getHex(event.tile.x, event.tile.y);
            if (hex != null) {
                this.hexed.getHexedState0().resetSpawnTimer(hex);
                this.hexed.getHexedState0().updateProgress(hex);
            }
        }
    }

    @Override
    public void onPluginUpdate() {
        if (!this.hexed.isEnabled()) {
            return;
        }

        this.hexed.getHexedState().incrementCounter(Time.delta);

        if (this.interval.get(CONTROLLER_TIMER, 2 * 60)) {
            for (final var hex : this.hexed.getHexedState().getHexes()) {
                final var oldController = this.hexed.getHexedState().getController(hex);
                this.hexed.getHexedState0().updateProgress(hex);
                final var newController = this.hexed.getHexedState().getController(hex);

                if (newController != null
                        && !newController.equals(oldController)
                        && !newController.equals(Team.derelict)) {
                    final var player = Groups.player.find(p -> p.team().equals(newController));
                    if (player != null) {
                        Distributor.get().getEventBus().post(new HexCaptureEvent(player, hex));
                    }
                }

                if (oldController != null
                        && !oldController.equals(newController)
                        && !oldController.equals(Team.derelict)) {
                    final var player = Groups.player.find(p -> p.team().equals(oldController));
                    if (player != null) {
                        Distributor.get().getEventBus().post(new HexLostEvent(player, hex));
                    }
                }
            }
        }

        if (this.interval.get(PLAYER_TIMER, 60)) {
            for (final var player : Groups.player) {
                if (!player.team().equals(Team.derelict)
                        && player.team().cores().isEmpty()) {
                    final var oldTeam = player.team();
                    Distributor.get().getEventBus().post(new HexPlayerQuitEvent(player, oldTeam, true));
                }

                if (player.team().equals(Team.derelict)) {
                    player.clearUnit();
                }

                if (this.hexed.getHexedState().getControlled(player.team()).size()
                        == this.hexed.getHexedState().getHexes().size()) {
                    this.endGame();
                    break;
                }
            }
        }

        if (this.hexed.getHexedState().getCounter().toMillis()
                > this.hexed.getHexedState().getDuration().toMillis()) {
            this.endGame();
        }
    }

    private void endGame() {
        if (!this.hexed.isEnabled() || Vars.state.gameOver) {
            return;
        }
        final var winners = MindustryCollections.immutableList(Vars.state.teams.getActive()).stream()
                .map(data -> data.team)
                .filter(team -> !team.equals(Team.derelict))
                .collect(maxList(Comparator.comparingInt(
                        team -> this.hexed.getHexedState().getControlled(team).size())));
        final var bus = Distributor.get().getEventBus();
        bus.post(new GameOverEvent(winners.size() == 1 ? winners.get(0) : Team.derelict));
        bus.post(new HexedGameOverEvent(winners));
    }

    @SuppressWarnings("EnumOrdinal")
    private void placeBaseSchematic(final Player player, final int x, final int y) {
        final var core = this.hexed.getHexedState().getBaseSchematic().getTiles().stream()
                .filter(s -> s.block() instanceof CoreBlock)
                .findFirst()
                .orElseThrow();
        final int cx = x - core.x();
        final int cy = y - core.y();

        for (final var stile : this.hexed.getHexedState().getBaseSchematic().getTiles()) {
            final var tile = Vars.world.tile(stile.x() + cx, stile.y() + cy);
            if (tile == null) {
                return;
            }

            if (!tile.block().equals(Blocks.air)) {
                tile.removeNet();
            }

            tile.setNet(stile.block(), player.team(), stile.rotation().ordinal());

            if (stile.configuration() != null) {
                tile.build.configureAny(stile.configuration());
            }
            if (tile.block() instanceof CoreBlock) {
                for (final var stack : Vars.state.rules.loadout) {
                    Call.setItem(tile.build, stack.item, stack.amount);
                }
            }
        }
    }

    // https://stackoverflow.com/a/29339106
    static <T> Collector<T, ?, List<T>> maxList(final Comparator<? super T> comp) {
        return Collector.of(
                ArrayList::new,
                (list, t) -> {
                    final int c;
                    if (list.isEmpty() || (c = comp.compare(t, list.get(0))) == 0) {
                        list.add(t);
                    } else if (c > 0) {
                        list.clear();
                        list.add(t);
                    }
                },
                (list1, list2) -> {
                    if (list1.isEmpty()) {
                        return list2;
                    }
                    if (list2.isEmpty()) {
                        return list1;
                    }
                    final int r = comp.compare(list1.get(0), list2.get(0));
                    if (r < 0) {
                        return list2;
                    } else if (r > 0) {
                        return list1;
                    } else {
                        list1.addAll(list2);
                        return list1;
                    }
                });
    }
}
