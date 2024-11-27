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

import arc.util.CommandHandler;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.ProxiedBy;
import com.xpdustry.hexed.api.HexedAPIProvider;
import com.xpdustry.hexed.api.event.HexPlayerJoinEvent;
import com.xpdustry.hexed.api.event.HexPlayerQuitEvent;
import com.xpdustry.hexed.api.generation.HexedMapContext;
import com.xpdustry.hexed.api.generation.MapGenerator;
import fr.xpdustry.distributor.api.DistributorProvider;
import fr.xpdustry.distributor.api.command.ArcCommandManager;
import fr.xpdustry.distributor.api.command.sender.CommandSender;
import fr.xpdustry.distributor.api.plugin.PluginListener;
import java.time.Duration;
import java.util.stream.Collectors;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

final class HexedCommands implements PluginListener {

    private final HexedPluginReloaded hexed;
    private final ArcCommandManager<CommandSender> clientCommandManager;
    private final ArcCommandManager<CommandSender> serverCommandManager;

    public HexedCommands(final HexedPluginReloaded hexed) {
        this.hexed = hexed;
        this.clientCommandManager = ArcCommandManager.standard(hexed);
        this.serverCommandManager = ArcCommandManager.standard(hexed);
    }

    @CommandMethod("hexed start [generator]")
    @CommandDescription("Begin hosting with the Hexed game mode.")
    @CommandPermission("com.xpdustry.hexed.start")
    public void onHexedStartCommand(
            final CommandSender sender, final @Argument(value = "generator", defaultValue = "anuke") String name) {
        if (Vars.state.isGame()) {
            sender.sendWarning("Stop the server first.");
            return;
        }

        final MapGenerator<HexedMapContext> generator =
                HexedAPIProvider.get().getGenerators().get(name);
        if (generator == null) {
            sender.sendWarning("Generator named " + name + " not found.");
            return;
        }

        if (HexedAPIProvider.get().start(generator)) {
            sender.sendMessage("Hexed game started.");
        } else {
            sender.sendWarning("An error occurred while starting the hexed game.");
        }
    }

    @CommandMethod("leaderboard")
    @CommandDescription("Display the leaderboard.")
    public void onLeaderboardCommand(final CommandSender sender) {
        sender.sendMessage(HexedUtils.createLeaderboard(this.hexed.getHexedState()));
    }

    @CommandMethod("hexed list [player]")
    @ProxiedBy("hexes")
    @CommandDescription("Display the captured hexes of a player.")
    public void onHexesCommand(final CommandSender sender, @Argument("player") @Nullable Player player) {
        if (player == null) {
            if (sender.isConsole()) {
                sender.sendWarning("You need to specify the player.");
                return;
            }
            player = sender.getPlayer();
        }
        if (player.team() == Team.derelict) {
            sender.sendWarning(player.coloredName() + " [white]is not in a team!");
            return;
        }
        final var hexes = this.hexed.getHexedState().getControlled(player.team());
        if (hexes.isEmpty()) {
            sender.sendWarning(player.coloredName() + "has not captured any hexes yet!");
            return;
        }
        sender.sendMessage(player.coloredName() + " [accent]have captured [white]"
                + hexes.size()
                + "[accent] hexes at [white]"
                + hexes.stream()
                        .map(hex -> "(" + hex.getTileX() + ", " + hex.getTileY() + ")")
                        .collect(Collectors.joining(", ", "[", "]")));
    }

    @CommandMethod("hexed spectate")
    @CommandDescription("Spectate the game.")
    public void onSpectateCommand(final CommandSender sender) {
        if (sender.isConsole()) {
            sender.sendWarning("You can't do that.");
            return;
        }
        if (sender.getPlayer().team() != Team.derelict) {
            DistributorProvider.get()
                    .getEventBus()
                    .post(new HexPlayerQuitEvent(
                            sender.getPlayer(), sender.getPlayer().team(), false));
        } else {
            sender.sendWarning("You are already spectating.");
        }
    }

    @CommandMethod("hexed join")
    @CommandDescription("Join the game.")
    public void onJoinCommand(final CommandSender sender) {
        if (sender.isConsole()) {
            sender.sendWarning("You can't do that.");
            return;
        }
        if (sender.getPlayer().team() == Team.derelict) {
            DistributorProvider.get().getEventBus().post(new HexPlayerJoinEvent(sender.getPlayer(), false));
        } else {
            sender.sendWarning("You are already in the game.");
        }
    }

    @CommandMethod("hexed set-time <minutes>")
    @CommandDescription("Set the time counter.")
    @CommandPermission("com.xpdustry.hexed.set-time")
    public void onSetTimeCommand(final CommandSender sender, final @Argument("minutes") int minutes) {
        HexedAPIProvider.get().getHexedState().setCounter(Duration.ofMinutes(minutes));
    }

    @Override
    public void onPluginServerCommandsRegistration(final CommandHandler handler) {
        this.serverCommandManager.initialize(handler);
        this.serverCommandManager.createAnnotationParser(CommandSender.class).parse(this);
    }

    @Override
    public void onPluginClientCommandsRegistration(final CommandHandler handler) {
        this.clientCommandManager.initialize(handler);
        this.clientCommandManager.createAnnotationParser(CommandSender.class).parse(this);
    }
}
