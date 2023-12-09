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
package com.xpdustry.hexed.generation;

import arc.util.noise.Simplex;
import java.util.ArrayList;
import java.util.List;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OreBlock;

import static mindustry.Vars.content;

public final class OreGeneratorFunction implements MapContext.TileConsumer {

    private int seed = 1;
    private float scale = 23;
    private float threshold = 0.81f;
    private float octaves = 2f;
    private float falloff = 0.3f;
    private float tilt = 0f;

    private OreBlock ore = (OreBlock) Blocks.oreCopper;
    private Block target = Blocks.air;

    public static List<OreGeneratorFunction> getDefaultOreFunctions() {
        final List<OreGeneratorFunction> functions = new ArrayList<>();
        for (final var block : content.blocks().select(b -> b.isOverlay() && b.asFloor().oreDefault)) {
            final var function = new OreGeneratorFunction();
            final var ore = (OreBlock) block;
            function.threshold = ore.oreThreshold;
            function.scale = ore.oreScale;
            function.ore = ore;
            functions.add(function);
        }
        return functions;
    }

    public int getSeed() {
        return this.seed;
    }

    public void setSeed(final int seed) {
        this.seed = seed;
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(final float scale) {
        this.scale = scale;
    }

    public float getThreshold() {
        return this.threshold;
    }

    public void setThreshold(final float threshold) {
        this.threshold = threshold;
    }

    public float getOctaves() {
        return this.octaves;
    }

    public void setOctaves(final float octaves) {
        this.octaves = octaves;
    }

    public float getFalloff() {
        return this.falloff;
    }

    public void setFalloff(final float falloff) {
        this.falloff = falloff;
    }

    public float getTilt() {
        return this.tilt;
    }

    public void setTilt(final float tilt) {
        this.tilt = tilt;
    }

    public OreBlock getOre() {
        return this.ore;
    }

    public void setOre(final Block ore) {
        this.ore = (OreBlock) ore;
    }

    public Block getTarget() {
        return this.target;
    }

    public void setTarget(final Block target) {
        this.target = target;
    }

    @Override
    public void accept(final int x, final int y, final MapTile tile) {
        final float noise = Simplex.noise2d(
                this.seed, this.octaves, this.falloff, 1f / this.scale, (float) x + 10, y + x * this.tilt + 10);
        if (noise > this.threshold
                && tile.getOverlay() != Blocks.spawn
                && (this.target == Blocks.air || tile.getFloor() == this.target || tile.getOverlay() == this.target)
                && tile.getFloor().hasSurface()) {
            tile.setOverlay(this.ore);
        }
    }
}
