package com.probreezer.multiFuse.Listeners;

import com.probreezer.multiFuse.MultiFuse;

public class EventLoader {
    public static void registerEvents(MultiFuse plugin) {
        var pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(new WorldListener(plugin), plugin);
        pluginManager.registerEvents(new BlockListener(plugin), plugin);
        pluginManager.registerEvents(new PlayerListener(plugin), plugin);
        pluginManager.registerEvents(new MenuListener(plugin), plugin);
        pluginManager.registerEvents(new ChatListener(plugin), plugin);
    }
}
