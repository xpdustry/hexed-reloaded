// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed;

import com.xpdustry.hexed.generation.HexedMapContext;
import com.xpdustry.hexed.generation.MapGenerator;
import java.util.Objects;
import mindustry.Vars;

public interface HexedAPI {

    static HexedAPI get() {
        return (HexedAPI) Objects.requireNonNull(Vars.mods.getMod(HexedPluginReloaded.class)).main;
    }

    HexedState getHexedState();

    boolean isEnabled();

    boolean start(final MapGenerator<HexedMapContext> generator);
}
