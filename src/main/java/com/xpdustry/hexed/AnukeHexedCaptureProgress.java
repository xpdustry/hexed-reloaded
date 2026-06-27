// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed;

import arc.struct.IntFloatMap;
import com.xpdustry.hexed.model.Hex;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.world.blocks.storage.CoreBlock;

final class AnukeHexedCaptureProgress implements HexedCaptureProgress {

    private final int requirement;

    AnukeHexedCaptureProgress(final int requirement) {
        if (requirement <= 0) {
            throw new IllegalArgumentException("Requirement must be greater than 0");
        }
        this.requirement = requirement;
    }

    @Override
    public void calculate(final Hex hex, final IntFloatMap capture) {
        Groups.unit
                .intersect(
                        hex.getX() - hex.getRadius(),
                        hex.getY() - hex.getRadius(),
                        hex.getDiameter(),
                        hex.getDiameter())
                .each(u -> {
                    if (!u.isPlayer() && hex.contains(u.tileX(), u.tileY())) {
                        capture.increment(u.team().id, u.health() / 10F);
                    }
                });

        for (int cx = hex.getTileX() - hex.getTileRadius(); cx < hex.getTileX() + hex.getTileRadius(); cx++) {
            for (int cy = hex.getTileY() - hex.getTileRadius(); cy < hex.getTileY() + hex.getTileRadius(); cy++) {
                final var tile = Vars.world.tile(cx, cy);
                if (tile != null && tile.synthetic() && hex.contains(tile.x, tile.y)) {
                    if (tile.block() instanceof CoreBlock) {
                        capture.increment(tile.team().id, 1F);
                    } else if (tile.block().requirements != null) {
                        for (final var stack : tile.block().requirements) {
                            capture.increment(tile.team().id, stack.amount * stack.item.cost);
                        }
                    }
                }
            }
        }

        final var keys = capture.keys();
        while (keys.hasNext()) {
            final var key = keys.next();
            capture.put(key, capture.get(key) / this.requirement);
        }
    }
}
