package com.probreezer.multiFuse.Lobby;

import com.probreezer.multiFuse.MultiFuse;

public class Spawn {
    public static void setSpawn(MultiFuse plugin) {
        var config = plugin.getConfig();
        var world = plugin.getServer().getWorlds().getFirst();

        int x = config.getInt("Spawn.x");
        int y = config.getInt("Spawn.y");
        int z = config.getInt("Spawn.z");

        world.setSpawnLocation(x, y, z);
    }
}
