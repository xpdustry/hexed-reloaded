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

import arc.struct.Seq;
import java.util.Arrays;
import mindustry.core.NetServer.TeamAssigner;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;

public final class HexedTeamAssigner implements TeamAssigner {

    private static final Team[] HEX_TEAMS =
            Arrays.stream(Team.all).filter(t -> t.id > 6).toArray(Team[]::new);

    private final HexedPluginReloaded hexed;
    private final TeamAssigner parent;

    public HexedTeamAssigner(final HexedPluginReloaded hexed, final TeamAssigner parent) {
        this.hexed = hexed;
        this.parent = parent;
    }

    @Override
    public Team assign(final Player player, final Iterable<Player> players) {
        final var used = Seq.with(players).map(Player::team).asSet();

        if (this.hexed.isActive()) {
            for (final var team : HEX_TEAMS) {
                if (!team.active()
                        && !used.contains(team)
                        && !this.hexed.getHexedState().isDying(team)) {
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
