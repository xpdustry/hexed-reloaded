// SPDX-License-Identifier: GPL-3.0-only
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

final class HexedTeamAssigner implements TeamAssigner {

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

        if (this.hexed.isEnabled()) {
            for (final var team : this.teams) {
                if (!used.contains(team) && this.hexed.getHexedState().isAvailable(team)) {
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
