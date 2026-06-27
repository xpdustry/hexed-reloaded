// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed.event;

import java.util.List;
import mindustry.game.Team;

public record HexedGameOverEvent(List<Team> winners) {}
