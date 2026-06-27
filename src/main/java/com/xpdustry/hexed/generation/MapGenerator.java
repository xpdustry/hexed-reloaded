// SPDX-License-Identifier: GPL-3.0-only
package com.xpdustry.hexed.generation;

public interface MapGenerator<C extends MapContext> {

    // TODO Use "void generate(C)" instead
    C generate();
}
