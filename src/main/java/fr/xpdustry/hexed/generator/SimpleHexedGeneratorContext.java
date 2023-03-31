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
package fr.xpdustry.hexed.generator;

import fr.xpdustry.hexed.HexedPluginReloaded;
import fr.xpdustry.hexed.model.Hex;
import fr.xpdustry.nucleus.mindustry.testing.map.SimpleMapContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mindustry.content.Items;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public final class SimpleHexedGeneratorContext extends SimpleMapContext implements HexedGeneratorContext {

    private final List<Hex> hexes = new ArrayList<>();
    private Rules rules = new Rules();
    private @MonotonicNonNull Schematic loadout;

    {
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

        try (final var stream = this.getClass().getResourceAsStream("/loadout.msch")) {
            setLoadout(Schematics.read(Objects.requireNonNull(stream)));
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load the default loadout.", e);
        }
    }

    @Override
    public void setTileIf(int x, int y, Predicate<Tile> condition, Block block) {
        addConsumer(tiles -> {
            final var tile = tiles.getn(x, y);
            if (condition.test(tile)) {
                tile.setBlock(block);
            }
        });
    }

    @Override
    public void setTile(int x, int y, Block floor, Block overlay, Block wall) {
        addConsumer(tiles -> tiles.set(x, y, new Tile(x, y, floor, overlay, wall)));
    }

    @Override
    public void setRules(final Rules rules) {
        this.rules = rules.copy();
    }

    @Override
    public Rules getRules() {
        return rules.copy();
    }

    @Override
    public List<Hex> getHexes() {
        return Collections.unmodifiableList(hexes);
    }

    @Override
    public void setHexes(final List<Hex> hexes) {
        this.hexes.clear();
        this.hexes.addAll(hexes);
    }

    @Override
    public Schematic getLoadout() {
        return loadout;
    }

    @Override
    public void setLoadout(final Schematic loadout) {
        final var core = loadout.tiles.find(s -> s.block instanceof CoreBlock);
        if (core == null) {
            throw new IllegalArgumentException("The loadout has no core tile.");
        }
        this.loadout = loadout;
    }
}
