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
package com.xpdustry.hexed.api.generation;

import com.xpdustry.hexed.HexedPluginReloaded;
import com.xpdustry.hexed.api.model.Hex;
import java.util.Collections;
import java.util.List;
import mindustry.content.Items;
import mindustry.game.Gamemode;
import mindustry.type.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SimpleHexedMapContext extends SimpleMapContext implements HexedMapContext {

    private List<Hex> hexes = Collections.emptyList();
    private @Nullable ImmutableSchematic schematic = null;

    {
        final var rules = this.getRules();
        Gamemode.pvp.apply(rules);
        rules.pvp = true;
        rules.tags.put(HexedPluginReloaded.HEXED_PRESENCE_FLAG, "true");
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
    public @Nullable ImmutableSchematic getBaseSchematic() {
        return this.schematic;
    }

    @Override
    public void setBaseSchematic(final @Nullable ImmutableSchematic schematic) {
        this.schematic = schematic;
    }
}
