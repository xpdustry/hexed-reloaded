// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed;

import arc.struct.IntFloatMap;
import com.xpdustry.hexed.model.Hex;

public interface HexedCaptureProgress {

    static HexedCaptureProgress anuke(final int requirement) {
        return new AnukeHexedCaptureProgress(requirement);
    }

    static HexedCaptureProgress anuke() {
        return new AnukeHexedCaptureProgress(210);
    }

    void calculate(final Hex hex, final IntFloatMap capture);
}
