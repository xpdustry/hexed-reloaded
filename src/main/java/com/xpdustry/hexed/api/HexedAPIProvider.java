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
package com.xpdustry.hexed.api;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public final class HexedAPIProvider {

    private static @MonotonicNonNull HexedAPI INSTANCE = null;

    public static HexedAPI get() {
        return Objects.requireNonNull(INSTANCE, "The Hexed API is not initialized.");
    }

    public static void set(final HexedAPI api) {
        if (INSTANCE != null) throw new IllegalStateException("The Hexed API is already initialized.");
        HexedAPIProvider.INSTANCE = api;
    }

    private HexedAPIProvider() {
        throw new UnsupportedOperationException();
    }
}
