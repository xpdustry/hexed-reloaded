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

import arc.math.Mathf;
import arc.util.Align;
import arc.util.Interval;
import arc.util.Strings;
import arc.util.Time;
import fr.xpdustry.distributor.api.event.EventHandler;
import fr.xpdustry.distributor.api.plugin.PluginListener;
import fr.xpdustry.hexed.event.HexCaptureEvent;
import fr.xpdustry.hexed.model.Hex;
import java.util.ArrayList;
import java.util.List;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Iconc;

public final class HexedRenderer implements PluginListener {

    private static final int HUD_TIMER = 0;
    private static final int DURATION_TIMER = 1;

    private final Interval timers = new Interval(2);
    private final HexedPluginReloaded hexed;

    public HexedRenderer(final HexedPluginReloaded hexed) {
        this.hexed = hexed;
    }

    @EventHandler
    public void onHexCapture(final HexCaptureEvent event) {
        Call.warningToast(
                Iconc.warning,
                "Hex #" + event.hex().getIdentifier() + " captured by "
                        + event.player().name());
    }

    @Override
    public void onPluginUpdate() {
        if (!this.hexed.isActive()) {
            return;
        }

        if (this.timers.get(HUD_TIMER, Time.toSeconds / 5)) {
            updateHud();
        }

        if (this.timers.get(DURATION_TIMER, Time.toSeconds)) {
            updateDuration();
        }
    }

    private void updateHud() {
        final List<Hex> hexes = new ArrayList<>(this.hexed.getHexedState().getHexes());
        for (final var player : Groups.player) {
            hexes.sort((a, b) -> {
                final var aDistance = Mathf.dst(a.getX(), a.getY(), player.x(), player.y());
                final var bDistance = Mathf.dst(b.getX(), b.getY(), player.x(), player.y());
                return Float.compare(aDistance, bDistance);
            });

            Hex hex = null;
            for (final var value : hexes) {
                if (value.contains(player.tileX(), player.tileY())) {
                    hex = value;
                    break;
                }
            }

            if (hex == null) {
                Call.hideHudText(player.con());
            } else {
                final var builder = new StringBuilder();
                builder.append("[white]Hex #").append(hex.getIdentifier()).append('\n');
                final var team = this.hexed.getHexedState().getController(hex);
                if (team != null) {
                    builder.append("[#").append(team.color).append("]Controlled");
                    final var controller = Groups.player.find(p -> p.team() == team);
                    if (controller == null) {
                        this.hexed.getLogger().warn("Team {} has not player.", team.name);
                        continue;
                    }
                    builder.append(" by ").append(controller.plainName());
                } else if (this.hexed.getHexedState().getProgress(hex, player.team()) > 0) {
                    builder.append("[lightgray]Capture progress: [accent]")
                            .append((int) this.hexed.getHexedState().getProgress(hex, player.team()))
                            .append("%");
                } else {
                    builder.append("[lightgray][[empty]");
                }

                Call.setHudText(player.con(), builder.toString());
            }
        }
    }

    private void updateDuration() {
        // TODO Spotless is being funky, cleanup, lol
        Call.infoPopup(
                "Time: "
                        + Strings.formatMillis(Math.max(
                                (HexedState.GAME_DURATION
                                                - (long) (this.hexed
                                                                .getHexedState()
                                                                .getCounter()
                                                        / 60L))
                                        * 1000L,
                                0L)),
                1,
                Align.bottom,
                0,
                0,
                0,
                0);
    }
}
