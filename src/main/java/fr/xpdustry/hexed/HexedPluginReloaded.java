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

import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.mod.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Template plugin.
 */
@SuppressWarnings("unused")
public final class HexedPluginReloaded extends Plugin {

    /**
     * This method is called when game initializes.
     */
    @Override
    public void init() {
        Log.info("Bonjour !");
    }

    /**
     * This method is called when the game register the server-side commands.
     */
    @Override
    public void registerServerCommands(final @NonNull CommandHandler handler) {}

    /**
     * This method is called when the game register the client-side commands.
     */
    @Override
    public void registerClientCommands(final @NonNull CommandHandler handler) {}
}
