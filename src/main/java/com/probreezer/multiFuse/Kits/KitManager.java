package com.probreezer.multiFuse.Kits;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class KitManager {
    private final MultiFuse plugin;
    private final YamlConfiguration config;
    public String defaultKit;
    public Map<String, Kit> kits;

    public KitManager(MultiFuse plugin) {
        this.plugin = plugin;
        this.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "kits.yml"));
        this.defaultKit = config.getString("default");
        this.kits = new HashMap<>();
    }

    public void loadKits() {
        var kitsSection = config.getConfigurationSection("kits");
        if (kitsSection == null) {
            plugin.getLogger().warning("No kits section found in configuration!");
            return;
        }

        for (String kitName : kitsSection.getKeys(false)) {
            var kitSection = kitsSection.getConfigurationSection(kitName);
            if (kitSection == null) {
                plugin.getLogger().warning("Invalid configuration for kit: " + kitName);
                continue;
            }

            var kit = new Kit(plugin, kitName, Material.valueOf(kitSection.getString("icon")));

            var itemsList = kitSection.getList("items");
            if (itemsList == null || itemsList.isEmpty()) {
                plugin.getLogger().warning("No items found for kit: " + kitName);
                continue;
            }

            for (Object itemObj : itemsList) {
                if (!(itemObj instanceof Map)) {
                    plugin.getLogger().warning("Invalid item configuration in kit: " + kitName);
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> itemMap = (Map<String, Object>) itemObj;

                try {
                    var material = Material.valueOf((String) itemMap.get("material"));
                    int amount = itemMap.containsKey("amount") ? (int) itemMap.get("amount") : 1;
                    int slot = (int) itemMap.get("slot");
                    kit.addItem(material, amount, slot);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in kit " + kitName + ": " + e.getMessage());
                } catch (ClassCastException e) {
                    plugin.getLogger().warning("Invalid data type in kit " + kitName + ": " + e.getMessage());
                }
            }
            plugin.getLogger().info("Loaded kit: " + kitName + " with " + kit.getItemCount() + " items");
            kits.put(kitName, kit);
        }
    }

    public void applyKit(MultiFuse plugin, String kitName, Player player) {
        plugin.getLogger().info("Applying kit " + kitName + " to " + player.getName());
        var kit = kits.get(kitName);
        if (kit != null) {
            plugin.getLogger().info("Kit found");
            kit.applyTo(player);
        }
    }
}
