/*
 * HexedReloaded, a reimplementation of the hexed gamemode from Anuke,
 * with more features and better performances.
 *
 * Copyright (C) 2025  Xpdustry
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

import arc.math.Mathf;
import arc.util.Align;
import arc.util.Interval;
import arc.util.Strings;
import arc.util.Time;
import com.xpdustry.distributor.api.annotation.EventHandler;
import com.xpdustry.distributor.api.annotation.TaskHandler;
import com.xpdustry.distributor.api.plugin.PluginListener;
import com.xpdustry.distributor.api.scheduler.MindustryTimeUnit;
import com.xpdustry.hexed.event.HexCaptureEvent;
import com.xpdustry.hexed.event.HexLostEvent;
import com.xpdustry.hexed.event.HexPlayerQuitEvent;
import com.xpdustry.hexed.event.HexedGameOverEvent;
import com.xpdustry.hexed.model.Hex;
import java.util.ArrayList;
import java.util.List;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Iconc;
import mindustry.gen.WorldLabel;
import mindustry.graphics.Layer;

final class HexedRenderer implements PluginListener {

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

    @EventHandler
    public void onHexLost(final HexLostEvent event) {
        Call.announce(
                event.player().con(),
                "[scarlet]You lost the hex #" + event.hex().getIdentifier() + " at ("
                        + event.hex().getTileX() + ", " + event.hex().getTileY() + ")");
    }

    @EventHandler
    public void onPlayerQuit(final HexPlayerQuitEvent event) {
        if (event.virtual()) {
            Call.sendMessage(event.player().name() + " [white]died of cringe.");
        }
    }

    @EventHandler
    public void onPlayEvent(final EventType.StateChangeEvent event) {
        if (this.hexed.isEnabled() && event.to == GameState.State.playing) {
            for (final var hex : this.hexed.getHexedState().getHexes()) {
                final var label = WorldLabel.create();
                label.set(hex.getX(), hex.getY() + (Vars.tilesize / 2F));
                label.text("#" + hex.getIdentifier());
                label.flags(WorldLabel.flagOutline);
                label.z(Layer.flyingUnitLow);
                label.fontSize(3.5F);
                label.add();
            }
        }
    }

    @EventHandler
    public void onGameOverEvent(final HexedGameOverEvent event) {
        if (event.winners().isEmpty()) {
            Call.infoMessage("No one won the game, too bad...");
        } else if (event.winners().size() == 1) {
            final var winner =
                    Groups.player.find(p -> p.team() == event.winners().get(0));
            if (winner != null) {
                Call.infoMessage(winner.coloredName() + " [accent]won the game with [white] "
                        + this.hexed
                                .getHexedState()
                                .getControlled(event.winners().get(0))
                                .size() + " []hexes!");
            }
        } else {
            Call.infoMessage("The game ended in a draw!");
        }
    }

    @TaskHandler(interval = 5L, unit = MindustryTimeUnit.MINUTES)
    public void onLeaderboardDisplay() {
        if (this.hexed.isEnabled() && Vars.state.isGame()) {
            Call.sendMessage(HexedUtils.createLeaderboard(this.hexed.getHexedState()));
        }
    }

    @Override
    public void onPluginUpdate() {
        if (!this.hexed.isEnabled()) {
            return;
        }

        if (this.timers.get(HUD_TIMER, Time.toSeconds / 5)) {
            this.updateHud();
        }

        if (this.timers.get(DURATION_TIMER, Time.toSeconds)) {
            this.updateDuration();
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

            if (hex == null || player.team() == Team.derelict) {
                Call.hideHudText(player.con());
            } else {
                final var builder = new StringBuilder();
                builder.append("[white]Hex #").append(hex.getIdentifier());
                final var team = this.hexed.getHexedState().getController(hex);
                if (team != null) {
                    builder.append("\n[#").append(team.color).append("]Controlled");
                    final var controller = Groups.player.find(p -> p.team() == team);
                    if (controller == null) {
                        // this.hexed.getLogger().warn("Team {} has no player.", team.name);
                        continue;
                    }
                    builder.append(" by ").append(controller.plainName());
                } else {
                    builder.append("\n[lightgray][[empty]");
                }
                if (team != player.team() && this.hexed.getHexedState().getProgress(hex, player.team()) > 0) {
                    builder.append("\n[lightgray]Capture progress: [accent]")
                            .append(Strings.fixed(this.hexed.getHexedState().getProgress(hex, player.team()), 1))
                            .append("%");
                }

                Call.setHudText(player.con(), builder.toString());
            }
        }
    }

    private void updateDuration() {
        final var remaining = Math.max(
                this.hexed
                        .getHexedState()
                        .getDuration()
                        .minus(this.hexed.getHexedState().getCounter())
                        .toMillis(),
                0L);
        Call.infoPopup("Time: " + Strings.formatMillis(remaining), 1, Align.bottom, 0, 0, 0, 0);
    }
}
