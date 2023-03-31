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

import java.util.Optional;
import mindustry.gen.Groups;
import mindustry.gen.Player;

public final class HexedUtils {

    private HexedUtils() {}

    public static String createLeaderboard(final HexedState state) {
        final var builder = new StringBuilder();
        builder.append("[accent]Leaderboard:");
        final var top = state.getLeaderboard().entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(10L)
                .toList();
        if (top.isEmpty()) {
            // Should not be possible though
            builder.append("\n[orange]No one has captured any hexes yet!");
            return builder.toString();
        }
        for (int i = 0; i < top.size(); i++) {
            final var entry = top.get(i);
            builder.append("\n[yellow]")
                    .append(i + 1)
                    .append(".[white] ")
                    .append(Optional.ofNullable(Groups.player.find(player -> player.team() == entry.getKey()))
                            .map(Player::coloredName)
                            .orElse("Unknown"))
                    .append(" [orange]>[white] ")
                    .append(entry.getValue())
                    .append(" hexes");
        }
        return builder.toString();
    }
}
