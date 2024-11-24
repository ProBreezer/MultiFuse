package com.probreezer.multiFuse.Utils;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigUtils {
    public static void createCustomConfigs(MultiFuse plugin) {
        var configFiles = new String[]{"blocks.yml", "kits.yml", "menus.yml", "shop.yml", "teams.yml"};

        for (String configFile : configFiles) {
            var file = new File(plugin.getDataFolder(), configFile);
            if (!file.exists()) {
                plugin.getLogger().warning("Config file not found: " + configFile);
                plugin.getLogger().warning("Creating " + configFile + "...");
                plugin.saveResource(configFile, false);
            }
        }
    }

    public static YamlConfiguration getConfig(String name) {
        var plugin = Bukkit.getPluginManager().getPlugin("MultiFuse");
        var file = new File(plugin.getDataFolder(), name + ".yml");
        return YamlConfiguration.loadConfiguration(file);
    }
}
