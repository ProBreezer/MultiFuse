package com.probreezer.multiFuse.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.probreezer.multiFuse.Utils.ConfigUtils.getConfig;

public class SpawnPointUtils {
    public static String getRandomSpawnPoint(String team) {
        var world = Bukkit.getServer().getWorlds().getFirst();
        var teamConfig = getConfig("teams");
        var teamSection = teamConfig.getConfigurationSection(team);
        var spawnAreas = teamSection.getStringList("SpawnArea");
        List<Coordinates> spawnArea = new ArrayList<>();

        for (var area : spawnAreas) {
            spawnArea.add(new Coordinates(area));
        }

        var location = getLocation(spawnArea, world);
        var locationString = LocationUtils.locationToString(location);

        return locationString;
    }

    private static Location getLocation(List<Coordinates> coordinates, World world) {
        var random = new Random();

        var spawnArea1 = coordinates.getFirst();
        var spawnArea2 = coordinates.getLast();

        double x1 = spawnArea1.x, y1 = spawnArea1.y, z1 = spawnArea1.z;
        double x2 = spawnArea2.x, y2 = spawnArea2.y, z2 = spawnArea2.z;

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
