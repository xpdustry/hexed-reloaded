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

import arc.struct.Seq;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import mindustry.core.NetServer.TeamAssigner;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;

public final class HexedTeamAssigner implements TeamAssigner {

    private final HexedPluginReloaded hexed;
    private final TeamAssigner parent;
    private final List<Team> teams = Arrays.stream(Team.all)
            .filter(t -> !Arrays.asList(Team.baseTeams).contains(t))
            .collect(Collectors.toList());

    {
        Collections.shuffle(this.teams);
    }

    public HexedTeamAssigner(final HexedPluginReloaded hexed, final TeamAssigner parent) {
        this.hexed = hexed;
        this.parent = parent;
    }

    @Override
    public Team assign(final Player player, final Iterable<Player> players) {
        final var used = Seq.with(players).map(Player::team).asSet();

        if (this.hexed.isActive()) {
            for (final var team : this.teams) {
                if (!team.active()
                        && !used.contains(team)
                        && !this.hexed.getHexedState().isDying(team)) {
                    Collections.shuffle(this.teams);
                    return team;
                }
            }

            Call.infoMessage(
                    player.con(), "There are currently no empty hex spaces available.\nAssigning into spectator mode.");
            return Team.derelict;
        } else {
            return this.parent.assign(player, players);
        }
    }
}
