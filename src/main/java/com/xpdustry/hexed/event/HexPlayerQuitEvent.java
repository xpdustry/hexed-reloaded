// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed.event;

import mindustry.game.Team;
import mindustry.gen.Player;

public record HexPlayerQuitEvent(Player player, Team team, boolean virtual) {}
