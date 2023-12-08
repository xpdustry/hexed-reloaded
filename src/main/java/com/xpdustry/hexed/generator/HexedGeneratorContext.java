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
package com.xpdustry.hexed.generator;

import com.xpdustry.hexed.model.Hex;
import fr.xpdustry.nucleus.mindustry.testing.map.MapContext;
import java.util.List;
import java.util.function.Predicate;
import mindustry.game.Rules;
import mindustry.game.Schematic;
import mindustry.world.Block;
import mindustry.world.Tile;

public interface HexedGeneratorContext extends MapContext {

    void setTileIf(final int x, final int y, Predicate<Tile> condition, final Block block);

    void setTile(final int x, final int y, final Block floor, final Block overlay, final Block wall);

    void setRules(final Rules rules);

    Rules getRules();

    List<Hex> getHexes();

    void setHexes(final List<Hex> hexes);

    // TODO Returns a mutable schematic, create a proxy class to prevent this
    Schematic getLoadout();

    void setLoadout(final Schematic loadout);
}
