/*
 * HexedReloaded, A reimplementation of the hexed gamemode from Anuke, with more features and better performances.
 *
 * Copyright (C) 2024  Xpdustry
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

import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MapTile implements Cloneable {

    private Block block = Blocks.stoneWall;
    private Block overlay = Blocks.air;
    private Floor floor = Blocks.stone.asFloor();
    private Building building = new Building();

    public Building getBuilding() {
        return this.building;
    }

    public Block getBlock() {
        return this.block;
    }

    public void setBlock(final Block block) {
        this.block = block;
        this.getBuilding().setConfiguration(null);
    }

    public Block getOverlay() {
        return this.overlay;
    }

    public void setOverlay(final Block overlay) {
        this.overlay = overlay;
    }

    public Floor getFloor() {
        return this.floor;
    }

    public void setFloor(final Floor floor) {
        this.floor = floor;
    }

    @Override
    public MapTile clone() {
        try {
            final var clone = (MapTile) super.clone();
            clone.building = clone.building.clone();
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public final class Building implements Cloneable {

        private float health = -1;
        private @Nullable Object configuration = null;
        private Team team = Team.derelict;

        private Building() {}

        public float getHealth() {
            return this.health < 0 ? MapTile.this.block.health : this.health;
        }

        public void setHealth(final float health) {
            this.health = Math.max(health, -1);
        }

        public @Nullable Object getConfiguration() {
            return this.configuration;
        }

        public void setConfiguration(final @Nullable Object configuration) {
            this.configuration = configuration;
            if (configuration != null) {
                Class<?> type = configuration.getClass();
                if (configuration instanceof Item) {
                    type = Item.class;
                } else if (configuration instanceof Block) {
                    type = Block.class;
                } else if (configuration instanceof Liquid) {
                    type = Liquid.class;
                } else if (configuration instanceof UnitType) {
                    type = UnitType.class;
                }
                if (!(MapTile.this.block.configurable && MapTile.this.block.configurations.containsKey(type))) {
                    throw new IllegalArgumentException(
                            "Unsupported configuration type for block " + MapTile.this.block + ": " + configuration);
                }
            }
        }

        public Team getTeam() {
            return this.team;
        }

        public void setTeam(final Team team) {
            this.team = team;
        }

        @Override
        public Building clone() {
            try {
                return (Building) super.clone();
            } catch (final CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
