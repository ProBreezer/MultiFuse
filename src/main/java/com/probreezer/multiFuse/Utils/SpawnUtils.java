package com.probreezer.multiFuse.Utils;

import com.probreezer.multiFuse.MultiFuse;

public class SpawnUtils {
    public static void setSpawn(MultiFuse plugin) {
        var config = plugin.getConfig();
        var world = plugin.getServer().getWorlds().getFirst();

        int x = config.getInt("Spawn.x");
        int y = config.getInt("Spawn.y");
        int z = config.getInt("Spawn.z");

        world.setSpawnLocation(x, y, z);
    }
}
