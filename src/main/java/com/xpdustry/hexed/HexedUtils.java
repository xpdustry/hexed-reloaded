// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed;

import java.util.Optional;
import mindustry.gen.Groups;
import mindustry.gen.Player;

final class HexedUtils {

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
                    .append(Optional.ofNullable(
                                    Groups.player.find(player -> player.team().equals(entry.getKey())))
                            .map(Player::coloredName)
                            .orElse("Unknown"))
                    .append(" [orange]>[white] ")
                    .append(entry.getValue())
                    .append(" hexes");
        }
        return builder.toString();
    }
}
