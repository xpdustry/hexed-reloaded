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

import arc.math.Mathf;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Time;
import fr.xpdustry.distributor.api.DistributorProvider;
import fr.xpdustry.distributor.api.event.EventHandler;
import fr.xpdustry.distributor.api.plugin.PluginListener;
import fr.xpdustry.distributor.api.scheduler.MindustryTimeUnit;
import fr.xpdustry.hexed.event.HexCaptureEvent;
import fr.xpdustry.hexed.generator.AnukenHexGenerator;
import fr.xpdustry.hexed.generator.HexGenerator;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.core.GameState.State;
import mindustry.game.EventType;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.type.ItemStack;
import mindustry.world.blocks.storage.CoreBlock;

public final class HexedLogic implements PluginListener {

    private static final int CONTROLLER_TIMER = 0;
    private static final int PLAYER_TIMER = 1;

    private final Interval interval = new Interval(2);
    private final HexedPluginReloaded hexed;

    public HexedLogic(final HexedPluginReloaded hexed) {
        this.hexed = hexed;
    }

    private Rules createHexedRules() {
        final var rules = new Rules();
        Gamemode.pvp.apply(rules);
        rules.pvp = true;
        rules.tags.put(HexedPluginReloaded.HEXED_PRESENCE_FLAG, "true");
        rules.loadout = ItemStack.list(
                Items.copper,
                300,
                Items.lead,
                500,
                Items.graphite,
                150,
                Items.metaglass,
                150,
                Items.silicon,
                150,
                Items.plastanium,
                50);
        rules.buildCostMultiplier = 1f;
        rules.buildSpeedMultiplier = 0.75F;
        rules.blockHealthMultiplier = 1.2f;
        rules.unitBuildSpeedMultiplier = 1f;
        rules.polygonCoreProtection = true;
        rules.unitDamageMultiplier = 1.1f;
        rules.canGameOver = false;
        return rules;
    }

    @Override
    public void onPluginInit() {
        final var parent = Vars.netServer.assigner;
        Vars.netServer.assigner = new HexedTeamAssigner(this.hexed, parent);
    }

    @EventHandler
    public void onPlayerJoin(final EventType.PlayerJoin event) {
        if (!this.hexed.isActive() || event.player.team() == Team.derelict) {
            return;
        }
        final var hexes = this.hexed.getHexedState().getHexes().stream()
                .filter(hex -> this.hexed.getHexedState().getController(hex) == null
                        && this.hexed.getHexedState().canSpawn(hex))
                .toList();

        if (hexes.isEmpty()) {
            Call.infoMessage(
                    event.player.con(),
                    "There are currently no empty hex spaces available.\nAssigning into spectator mode.");
            event.player.unit().kill();
            event.player.team(Team.derelict);
        } else {
            final var hex = hexes.get(Mathf.random(0, hexes.size() - 1));
            placeLoadout(event.player, hex.getTileX(), hex.getTileY());
            this.hexed.getHexedState().updateProgress(hex);
        }
    }

    @EventHandler
    public void onPlayerLeave(final EventType.PlayerLeave event) {
        if (this.hexed.isActive() && event.player.team() != Team.derelict) {
            this.killTeam(event.player.team());
        }
    }

    @EventHandler
    public void onBlockDestroy(final EventType.BlockDestroyEvent event) {
        // reset last spawn times so this hex becomes vacant for a while.
        if (this.hexed.isActive() && event.tile.block() instanceof CoreBlock) {
            final var hex = this.hexed.getHexedState().getHex(event.tile.x, event.tile.y);
            if (hex != null) {
                this.hexed.getHexedState().resetSpawnTimer(hex);
                this.hexed.getHexedState().updateProgress(hex);
            }
        }
    }

    @Override
    public void onPluginServerCommandsRegistration(final CommandHandler handler) {
        // TODO Move the start command in a dedicated class
        handler.register("hexed", "Begin hosting with the Hexed gamemode.", args -> {
            if (!Vars.state.is(State.menu)) {
                Log.err("Stop the server first.");
                return;
            }

            Vars.logic.reset();
            this.hexed.getLogger().info("Generating map...");

            // TODO Use the map generation API of Xpdustry/Router, the vanilla one is awful
            final HexGenerator generator = new AnukenHexGenerator();
            Vars.world.loadGenerator(generator.getWorldHeight(), generator.getWorldWidth(), tiles -> this.hexed
                    .getHexedState()
                    .setHexes(generator.generate(tiles)));

            this.hexed.getLogger().info("Map generated.");

            Vars.state.rules = this.createHexedRules();
            Vars.logic.play();
            Vars.netServer.openServer();
        });
    }

    @Override
    public void onPluginUpdate() {
        if (!this.hexed.isActive()) {
            this.hexed.getHexedState().setCounter(0);
            return;
        } else {
            this.hexed.getHexedState().incrementCounter(Time.delta);
        }

        if (this.interval.get(CONTROLLER_TIMER, 2 * 60)) {
            for (final var hex : this.hexed.getHexedState().getHexes()) {
                final var oldController = this.hexed.getHexedState().getController(hex);
                this.hexed.getHexedState().updateProgress(hex);
                final var newController = this.hexed.getHexedState().getController(hex);

                if (oldController != newController && newController != null && newController != Team.derelict) {
                    final var player = Groups.player.find(p -> p.team() == newController);
                    if (player == null) {
                        this.hexed.getLogger().warn("Team {} has not player.", newController.name);
                        continue;
                    }
                    DistributorProvider.get().getEventBus().post(new HexCaptureEvent(player, hex));
                }
            }
        }

        if (this.interval.get(PLAYER_TIMER, 60)) {
            for (final var player : Groups.player) {
                if (player.team() != Team.derelict && player.team().cores().isEmpty()) {
                    player.team(Team.derelict);
                    player.clearUnit();
                    this.killTeam(player.team());
                }

                if (player.team() == Team.derelict) {
                    player.clearUnit();
                }

                if (this.hexed.getHexedState().getControlled(player.team()).size()
                        == this.hexed.getHexedState().getHexes().size()) {
                    this.endGame();
                    break;
                }
            }
        }

        if (this.hexed.getHexedState().getCounter() > HexedState.GAME_DURATION) {
            this.endGame();
        }
    }

    private void killTeam(final Team team) {
        this.hexed.getHexedState().setDying(team, true);

        for (int x = 0; x < Vars.world.width(); x++) {
            for (int y = 0; y < Vars.world.height(); y++) {
                final var tile = Vars.world.tile(x, y);
                if (tile.build != null) {
                    // TODO Use a queue for destroying blocks due to pvp autopause
                    DistributorProvider.get()
                            .getPluginScheduler()
                            .scheduleSync(this.hexed)
                            .delay(Mathf.random(6 * 60), MindustryTimeUnit.TICKS)
                            .execute(() -> {
                                // We never know
                                if (tile.build != null && tile.team() == team) {
                                    tile.build.kill();
                                }
                            });
                }
            }
        }
        DistributorProvider.get()
                .getPluginScheduler()
                .scheduleAsync(this.hexed)
                .delay(8, MindustryTimeUnit.SECONDS)
                .execute(() -> this.hexed.getHexedState().setDying(team, false));
    }

    private void endGame() {
        // TODO Verify if effective
        var maxTeam = Team.derelict;
        var max = 0;
        for (final var team : Vars.state.teams.getActive()) {
            if (team.team == Team.derelict) {
                continue;
            }
            final var count =
                    this.hexed.getHexedState().getControlled(team.team).size();
            if (count > max) {
                max = count;
                maxTeam = team.team;
            }
        }
        DistributorProvider.get().getEventBus().post(new GameOverEvent(maxTeam));
    }

    private void placeLoadout(final Player player, int x, int y) {
        final var core = this.hexed.getLoadout().tiles.find(s -> s.block instanceof CoreBlock);
        final int cx = x - core.x;
        final int cy = y - core.y;

        for (final var stile : this.hexed.getLoadout().tiles) {
            final var tile = Vars.world.tile(stile.x + cx, stile.y + cy);
            if (tile == null) {
                return;
            }

            if (tile.block() != Blocks.air) {
                tile.removeNet();
            }

            tile.setNet(stile.block, player.team(), stile.rotation);

            if (stile.config != null) {
                tile.build.configureAny(stile.config);
            }
            if (tile.block() instanceof CoreBlock) {
                for (final var stack : Vars.state.rules.loadout) {
                    Call.setItem(tile.build, stack.item, stack.amount);
                }
            }
        }
    }
}
