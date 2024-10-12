package com.probreezer.multiFuse.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {

    public static String locationToString(Location location) {
        if (location == null) {
            return null;
        }

        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    public static Location stringToLocation(String locationString) {
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }

        var parts = locationString.split(",");
        if (parts.length != 6) {
            return null;
        }

        var world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }

        double x, y, z;
        float yaw, pitch;

        try {
            x = Double.parseDouble(parts[1]);
            y = Double.parseDouble(parts[2]);
            z = Double.parseDouble(parts[3]);
            yaw = Float.parseFloat(parts[4]);
            pitch = Float.parseFloat(parts[5]);
        } catch (NumberFormatException e) {
            return null;
        }

        return new Location(world, x, y, z, yaw, pitch);
    }
}

