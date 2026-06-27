// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed.generation;

import com.xpdustry.hexed.HexedCaptureProgress;
import com.xpdustry.hexed.model.Hex;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import mindustry.game.Schematics;

public interface HexedMapContext extends MapContext {

    ImmutableSchematic DEFAULT_BASE_SCHEMATIC = loadDefaultBaseSchematic();

    String HEXED_PRESENCE_FLAG = "xpdustry:hexed-reloaded";

    Duration DEFAULT_GAME_DURATION = Duration.ofMinutes(90L);

    List<Hex> getHexes();

    void setHexes(final List<Hex> hexes);

    Duration getDuration();

    void setDuration(final Duration duration);

    ImmutableSchematic getBaseSchematic();

    void setBaseSchematic(final ImmutableSchematic schematic);

    HexedCaptureProgress getCaptureCalculator();

    void setCaptureCalculator(final HexedCaptureProgress calculator);

    private static ImmutableSchematic loadDefaultBaseSchematic() {
        try (final var stream = SimpleHexedMapContext.class.getResourceAsStream("/com/xpdustry/hexed/default.msch")) {
            return new ImmutableSchematic(Schematics.read(Objects.requireNonNull(stream)));
        } catch (final Exception e) {
            throw new RuntimeException("Failed to load the default base schematic.", e);
        }
    }
}
