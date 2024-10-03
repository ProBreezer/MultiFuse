package com.probreezer.multiFuse.Utils;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramUtils {
    private final JavaPlugin plugin;
    private final Map<String, Hologram> holograms;

    public HologramUtils(JavaPlugin plugin) {
        this.plugin = plugin;
        this.holograms = new HashMap<>();
    }

    public void addHologram(String key, List<String> lines, Location location) {
        if (holograms.containsKey(key)) {
            plugin.getLogger().warning("Hologram with key '" + key + "' already exists. Updating instead.");
            updateHologram(key, lines, location);
            return;
        }

        var hologram = DHAPI.createHologram(key, location, lines);
        if (hologram != null) {
            holograms.put(key, hologram);
        } else {
            plugin.getLogger().warning("Failed to create hologram with key: " + key);
        }
    }

    public void removeHologram(String key) {
        var hologram = holograms.remove(key);
        if (hologram != null) {
            DHAPI.removeHologram(key);
        } else {
            plugin.getLogger().warning("Attempted to remove non-existent hologram with key: " + key);
        }
    }

    public void updateHologram(String key, List<String> lines, Location location) {
        var hologram = holograms.get(key);
        if (hologram == null) {
            plugin.getLogger().warning("Attempted to update non-existent hologram with key: " + key);
            return;
        }

        if (lines != null && !lines.isEmpty()) {
            DHAPI.setHologramLines(hologram, lines);
        }
        if (location != null) {
            DHAPI.moveHologram(hologram, location);
        }
    }

    public void removeAllHolograms() {
        for (var key : holograms.keySet()) {
            DHAPI.removeHologram(key);
        }
        holograms.clear();
    }
}
