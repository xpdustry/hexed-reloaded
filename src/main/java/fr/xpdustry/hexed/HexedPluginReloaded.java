/*
 * This file is part of HexedPluginReloaded. A reimplementation of the hex gamemode, with more features and better performances.
 *
 * MIT License
 *
 * Copyright (c) 2023 Xpdustry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package fr.xpdustry.hexed;

import fr.xpdustry.distributor.api.plugin.AbstractMindustryPlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.world.blocks.storage.CoreBlock;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

@SuppressWarnings("unused")
public final class HexedPluginReloaded extends AbstractMindustryPlugin {

    public static final String HEXED_PRESENCE_FLAG = "hexed-reloaded";

    private final HexedState state = new HexedState();
    private @MonotonicNonNull Schematic loadout = null;

    @Override
    public void onInit() {
        this.addListener(new HexedLogic(this));
        this.addListener(new HexedRenderer(this));
        this.loadout = this.loadLoadout();
    }

    public boolean isActive() {
        return Vars.state.rules.tags.getBool(HEXED_PRESENCE_FLAG) && !Vars.state.isMenu();
    }

    public HexedState getHexedState() {
        return this.state;
    }

    Schematic getLoadout() {
        return this.loadout;
    }

    private Schematic loadLoadout() {
        var file = this.getDirectory().resolve("loadout.msch");
        final Schematic schematic;
        if (Files.exists(file)) {
            try (final var stream = Files.newInputStream(file)) {
                schematic = Schematics.read(stream);
            } catch (final IOException e) {
                throw new RuntimeException("Failed to load the loadout in the plugin directory.", e);
            }
        } else {
            try (final var stream = this.getClass().getResourceAsStream("/loadout.msch")) {
                schematic = Schematics.read(Objects.requireNonNull(stream));
            } catch (final IOException e) {
                throw new RuntimeException("Failed to load the default loadout.", e);
            }
        }
        final var core = schematic.tiles.find(s -> s.block instanceof CoreBlock);
        if (core == null) {
            throw new IllegalArgumentException("The loadout has no core tile.");
        }
        return schematic;
    }
}
