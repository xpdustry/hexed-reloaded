// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed.event;

import com.xpdustry.hexed.model.Hex;
import mindustry.gen.Player;

public record HexCaptureEvent(Player player, Hex hex) {}
