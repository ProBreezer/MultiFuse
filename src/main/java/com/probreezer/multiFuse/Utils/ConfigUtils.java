package com.probreezer.multiFuse.Utils;

import com.probreezer.multiFuse.MultiFuse;

import java.io.File;

public class ConfigUtils {
    public static void createCustomConfigs(MultiFuse plugin) {
        var configFiles = new String[]{"teams.yml", "menus.yml", "kits.yml"};

        for (String configFile : configFiles) {
            var file = new File(plugin.getDataFolder(), configFile);
            if (!file.exists()) {
                plugin.saveResource(configFile, false);
            }
        }
    }
}
