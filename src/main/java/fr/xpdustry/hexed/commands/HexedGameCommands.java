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
package fr.xpdustry.hexed.commands;

import arc.util.CommandHandler;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import fr.xpdustry.distributor.api.DistributorProvider;
import fr.xpdustry.distributor.api.command.sender.CommandSender;
import fr.xpdustry.distributor.api.plugin.PluginListener;
import fr.xpdustry.hexed.HexedPluginReloaded;
import fr.xpdustry.hexed.HexedState;
import fr.xpdustry.hexed.HexedUtils;
import fr.xpdustry.hexed.event.HexPlayerJoinEvent;
import fr.xpdustry.hexed.event.HexPlayerQuitEvent;
import java.util.stream.Collectors;
import mindustry.game.Team;
import mindustry.gen.Player;

public final class HexedGameCommands implements PluginListener {

    private final HexedPluginReloaded hexed;

    public HexedGameCommands(final HexedPluginReloaded hexed) {
        this.hexed = hexed;
    }

    @CommandMethod("leaderboard")
    @CommandDescription("Display the leaderboard.")
    public void onLeaderboardCommand(final CommandSender sender) {
        sender.sendMessage(HexedUtils.createLeaderboard(this.hexed.getHexedState()));
    }

    @CommandMethod("hexes [player]")
    @CommandDescription("Display the captured hexes of a player.")
    public void onHexesCommand(final CommandSender sender, @Argument("player") Player player) {
        if (player == null) {
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

    @CommandMethod("spectate")
    @CommandDescription("Spectate the game.")
    public void onSpectateCommand(final CommandSender sender) {
        if (sender.getPlayer().team() != Team.derelict) {
            DistributorProvider.get()
                    .getEventBus()
                    .post(new HexPlayerQuitEvent(
                            sender.getPlayer(), sender.getPlayer().team(), false));
        } else {
            sender.sendWarning("You are already spectating.");
        }
    }

    @CommandMethod("join")
    @CommandDescription("Join the game.")
    public void onJoinCommand(final CommandSender sender) {
        if (sender.getPlayer().team() == Team.derelict) {
            DistributorProvider.get().getEventBus().post(new HexPlayerJoinEvent(sender.getPlayer(), false));
        } else {
            sender.sendWarning("You are already in the game.");
        }
    }

    @CommandMethod("set-time <minutes>")
    @CommandDescription("Set the remaining time.")
    @CommandPermission("fr.xpdustry.hexed.set-time")
    public void onSetTimeCommand(final CommandSender sender, final @Argument("minutes") int minutes) {
        this.hexed.getHexedState().setCounter(HexedState.GAME_DURATION - (minutes * 60 * 60));
    }

    @Override
    public void onPluginClientCommandsRegistration(final CommandHandler handler) {
        this.hexed.getClientCommandManager().getAnnotationParser().parse(this);
    }

    @Override
    public void onPluginServerCommandsRegistration(final CommandHandler handler) {
        this.hexed.getServerCommandManager().getAnnotationParser().parse(this);
    }
}
