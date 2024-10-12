package com.probreezer.multiFuse.Commands;

import com.probreezer.multiFuse.MultiFuse;

public class CommandLoader {
    public static void registerCommands(MultiFuse plugin) {
        if (plugin.getCommand("countdown") != null) {
            plugin.getCommand("countdown").setExecutor(new CountdownCommands(plugin));
        } else {
            plugin.getLogger().warning("Failed to register 'countdown' command.");
        }
    }
}
