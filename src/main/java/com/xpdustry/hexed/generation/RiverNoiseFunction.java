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

import arc.util.noise.Ridged;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;

public final class RiverNoiseFunction extends GeneratorFunction {

    private float scale = 40;
    private float threshold1 = 0f;
    private float threshold2 = 0.1f;
    private float octaves = 1;
    private float falloff = 0.5f;
    private Floor floor1 = Blocks.water.asFloor();
    private Floor floor2 = Blocks.deepwater.asFloor();
    private Block block = Blocks.sandWall;
    private Block target = Blocks.air;

    public float getScale() {
        return this.scale;
    }

    public void setScale(final float scale) {
        this.scale = scale;
    }

    public float getThreshold1() {
        return this.threshold1;
    }

    public void setThreshold1(final float threshold1) {
        this.threshold1 = threshold1;
    }

    public float getThreshold2() {
        return this.threshold2;
    }

    public void setThreshold2(final float threshold2) {
        this.threshold2 = threshold2;
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

    public Floor getFloor1() {
        return this.floor1;
    }

    public void setFloor1(final Floor floor1) {
        this.floor1 = floor1;
    }

    public Floor getFloor2() {
        return this.floor2;
    }

    public void setFloor2(final Floor floor2) {
        this.floor2 = floor2;
    }

    public Block getBlock() {
        return this.block;
    }

    public void setBlock(final Block block) {
        this.block = block;
    }

    public Block getTarget() {
        return this.target;
    }

    public void setTarget(final Block target) {
        this.target = target;
    }

    @Override
    public void accept(final int x, final int y, final MapTile tile) {
        final float noise = Ridged.noise2d(
                this.getSeed() + 1,
                (int) ((float) x),
                (int) ((float) y),
                (int) this.octaves,
                this.falloff,
                1f / this.scale);

        if (noise >= this.threshold1
                && (this.target == Blocks.air || tile.getFloor() == this.target || tile.getBlock() == this.target)) {

            if (this.floor1 != Blocks.air) {
                tile.setFloor(this.floor1);
            }

            if (tile.getBlock().solid && this.block != Blocks.air && tile.getBlock() != Blocks.air) {
                tile.setBlock(this.block);
            }

            if (noise >= this.threshold2 && this.floor2 != Blocks.air) {
                tile.setFloor(this.floor2);
            }
        }
    }
}
