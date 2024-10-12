package com.probreezer.multiFuse.Utils;

import com.probreezer.multiFuse.Game.TeamSpawnArea;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Random;

import static com.probreezer.multiFuse.Utils.ConfigUtils.getConfig;

public class SpawnPointUtils {
    public static String getRandomSpawnPoint(String team) {
        var world = Bukkit.getServer().getWorlds().getFirst();
        var teamConfig = getConfig("teams");
        var teamSection = teamConfig.getConfigurationSection(team);
        var spawnAreas = teamSection.getStringList("SpawnArea");
        var spawnArea = new TeamSpawnArea(spawnAreas.get(0), spawnAreas.get(1));
        var location = getLocation(spawnArea, world);
        var locationString = LocationUtils.locationToString(location);

        return locationString;
    }

    private static Location getLocation(TeamSpawnArea spawnArea, World world) {
        var random = new Random();

        double x1 = spawnArea.x1, x2 = spawnArea.x2, y1 = spawnArea.y1, y2 = spawnArea.y2, z1 = spawnArea.z1, z2 = spawnArea.z2;

        var minX = Math.min(x1, x2);
        var maxX = Math.max(x1, x2);
        var minY = Math.min(y1, y2);
        var maxY = Math.max(y1, y2);
        var minZ = Math.min(z1, z2);
        var maxZ = Math.max(z1, z2);

        var randomX = minX + ((maxX - minX) * random.nextDouble());
        var randomY = minY + ((maxY - minY) * random.nextDouble());
        var randomZ = minZ + ((maxZ - minZ) * random.nextDouble());

        var location = new Location(world, randomX, randomY, randomZ);
        return location;
    }
}
