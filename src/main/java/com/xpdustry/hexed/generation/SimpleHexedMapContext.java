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
package com.xpdustry.hexed.generation;

import com.xpdustry.hexed.HexedCaptureProgress;
import com.xpdustry.hexed.model.Hex;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import mindustry.content.Items;
import mindustry.game.Gamemode;
import mindustry.type.ItemStack;

public class SimpleHexedMapContext extends SimpleMapContext implements HexedMapContext {

    private List<Hex> hexes = Collections.emptyList();
    private Duration duration = DEFAULT_GAME_DURATION;
    private ImmutableSchematic schematic = DEFAULT_BASE_SCHEMATIC;
    private HexedCaptureProgress calculator = HexedCaptureProgress.anuke();

    {
        final var rules = this.getRules();
        Gamemode.pvp.apply(rules);
        rules.pvp = true;
        rules.tags.put(HEXED_PRESENCE_FLAG, "true");
        rules.loadout = ItemStack.list(
                Items.copper,
                300,
                Items.lead,
                500,
                Items.graphite,
                150,
                Items.metaglass,
                150,
                Items.silicon,
                150,
                Items.plastanium,
                50);
        rules.buildCostMultiplier = 1f;
        rules.buildSpeedMultiplier = 0.75F;
        rules.blockHealthMultiplier = 1.2f;
        rules.unitBuildSpeedMultiplier = 1f;
        rules.polygonCoreProtection = true;
        rules.unitDamageMultiplier = 1.1f;
        rules.canGameOver = false;
        this.setRules(rules);
    }

    @Override
    public List<Hex> getHexes() {
        return this.hexes;
    }

    @Override
    public void setHexes(final List<Hex> hexes) {
        this.hexes = List.copyOf(hexes);
    }

    @Override
    public Duration getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(final Duration duration) {
        this.duration = duration;
    }

    @Override
    public ImmutableSchematic getBaseSchematic() {
        return this.schematic;
    }

    @Override
    public void setBaseSchematic(final ImmutableSchematic schematic) {
        this.schematic = schematic;
    }

    @Override
    public HexedCaptureProgress getCaptureCalculator() {
        return this.calculator;
    }

    @Override
    public void setCaptureCalculator(final HexedCaptureProgress calculator) {
        this.calculator = calculator;
    }
}
