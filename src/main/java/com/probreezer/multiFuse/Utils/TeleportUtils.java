package com.probreezer.multiFuse.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportUtils {
    public static void teleportAllPLayers(Location location) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(location);
        }
    }
}