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
package com.xpdustry.hexed;

import arc.util.CommandHandler;
import com.xpdustry.hexed.commands.AnnotationCommandManager;
import com.xpdustry.hexed.commands.HexedGameCommands;
import com.xpdustry.hexed.commands.HexedStartCommand;
import fr.xpdustry.distributor.api.plugin.AbstractMindustryPlugin;
import mindustry.Vars;

@SuppressWarnings("unused")
public final class HexedPluginReloaded extends AbstractMindustryPlugin {

    public static final String HEXED_PRESENCE_FLAG = "hexed-reloaded";

    private final AnnotationCommandManager clientCommandManager = new AnnotationCommandManager(this);
    private final AnnotationCommandManager serverCommandManager = new AnnotationCommandManager(this);

    private final HexedState state = new HexedState();

    @Override
    public void onInit() {
        this.addListener(new HexedLogic(this));
        this.addListener(new HexedRenderer(this));

        this.addListener(new HexedStartCommand(this));
        this.addListener(new HexedGameCommands(this));
    }

    @Override
    public void onServerCommandsRegistration(final CommandHandler handler) {
        this.serverCommandManager.initialize(handler);
    }

    @Override
    public void onClientCommandsRegistration(final CommandHandler handler) {
        this.clientCommandManager.initialize(handler);
    }

    public HexedState getHexedState() {
        return this.state;
    }

    public boolean isActive() {
        return Vars.state.rules.tags.getBool(HEXED_PRESENCE_FLAG) && Vars.state.isGame();
    }

    public AnnotationCommandManager getClientCommandManager() {
        return clientCommandManager;
    }

    public AnnotationCommandManager getServerCommandManager() {
        return serverCommandManager;
    }
}
