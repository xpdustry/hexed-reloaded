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

import arc.util.CommandHandler;
import com.xpdustry.distributor.api.DistributorProvider;
import com.xpdustry.distributor.api.command.CommandSender;
import com.xpdustry.distributor.api.command.cloud.MindustryCommandManager;
import com.xpdustry.distributor.api.plugin.PluginListener;
import com.xpdustry.hexed.event.HexPlayerJoinEvent;
import com.xpdustry.hexed.event.HexPlayerQuitEvent;
import com.xpdustry.hexed.generation.HexedMapContext;
import com.xpdustry.hexed.generation.MapGenerator;
import java.time.Duration;
import java.util.stream.Collectors;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Default;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.ProxiedBy;
import org.incendo.cloud.execution.ExecutionCoordinator;

final class HexedCommands implements PluginListener {

    private final HexedPluginReloaded hexed;

    public HexedCommands(final HexedPluginReloaded hexed) {
        this.hexed = hexed;
    }

    @Command("hexed start [generator]")
    @CommandDescription("Begin hosting with the Hexed game mode.")
    @Permission("com.xpdustry.hexed.start")
    public void onHexedStartCommand(
            final CommandSender sender, final @Argument(value = "generator") @Default("anuke") String name) {
        if (Vars.state.isGame()) {
            sender.error("Stop the server first.");
            return;
        }

        final MapGenerator<HexedMapContext> generator =
                HexedAPIProvider.get().getGenerators().get(name);
        if (generator == null) {
            sender.error("Generator named " + name + " not found.");
            return;
        }

        if (HexedAPIProvider.get().start(generator)) {
            sender.reply("Hexed game started.");
        } else {
            sender.error("An error occurred while starting the hexed game.");
        }
    }

    @Command("leaderboard")
    @CommandDescription("Display the leaderboard.")
    public void onLeaderboardCommand(final CommandSender sender) {
        sender.reply(HexedUtils.createLeaderboard(this.hexed.getHexedState()));
    }

    @Command("hexed list [player]")
    @ProxiedBy("hexes")
    @CommandDescription("Display the captured hexes of a player.")
    public void onHexesCommand(final CommandSender sender, @Argument("player") @Nullable Player player) {
        if (player == null) {
            if (sender.isServer()) {
                sender.error("You need to specify the player.");
                return;
            }
            player = sender.getPlayer();
        }
        if (player.team() == Team.derelict) {
            sender.error(player.coloredName() + " [white]is not in a team!");
            return;
        }
        final var hexes = this.hexed.getHexedState().getControlled(player.team());
        if (hexes.isEmpty()) {
            sender.error(player.coloredName() + "has not captured any hexes yet!");
            return;
        }
        sender.reply(player.coloredName() + " [accent]have captured [white]"
                + hexes.size()
                + "[accent] hexes at [white]"
                + hexes.stream()
                        .map(hex -> "(" + hex.getTileX() + ", " + hex.getTileY() + ")")
                        .collect(Collectors.joining(", ", "[", "]")));
    }

    @Command("hexed spectate")
    @CommandDescription("Spectate the game.")
    public void onSpectateCommand(final CommandSender sender) {
        if (sender.isServer()) {
            sender.error("You can't do that.");
            return;
        }
        if (sender.getPlayer().team() != Team.derelict) {
            DistributorProvider.get()
                    .getEventBus()
                    .post(new HexPlayerQuitEvent(
                            sender.getPlayer(), sender.getPlayer().team(), false));
        } else {
            sender.error("You are already spectating.");
        }
    }

    @Command("hexed join")
    @CommandDescription("Join the game.")
    public void onJoinCommand(final CommandSender sender) {
        if (sender.isServer()) {
            sender.error("You can't do that.");
            return;
        }
        if (sender.getPlayer().team() == Team.derelict) {
            DistributorProvider.get().getEventBus().post(new HexPlayerJoinEvent(sender.getPlayer(), false));
        } else {
            sender.error("You are already in the game.");
        }
    }

    @Command("hexed set-time <minutes>")
    @CommandDescription("Set the time counter.")
    @Permission("com.xpdustry.hexed.set-time")
    public void onSetTimeCommand(final CommandSender sender, final @Argument("minutes") int minutes) {
        HexedAPIProvider.get().getHexedState().setCounter(Duration.ofMinutes(minutes));
    }

    @Override
    public void onPluginServerCommandsRegistration(final CommandHandler handler) {
        this.onPluginSharedCommandsRegistration(handler);
    }

    @Override
    public void onPluginClientCommandsRegistration(final CommandHandler handler) {
        this.onPluginSharedCommandsRegistration(handler);
    }

    private void onPluginSharedCommandsRegistration(final CommandHandler handler) {
        final var manager = new MindustryCommandManager<>(
                this.hexed, handler, ExecutionCoordinator.simpleCoordinator(), SenderMapper.identity());
        final var annotations = new AnnotationParser<>(manager, CommandSender.class);
        annotations.parse(this);
    }
}
