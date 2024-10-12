package com.probreezer.multiFuse.Blocks;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Random;

public class MineableBlock {
    public HashMap<String, DropItem> dropItems = new HashMap<>();
    public Boolean Coins = false;
    public int MaxCoins = 1;
    public Material replacementBlock = Material.AIR;
    public int respawn = 0;

    public MineableBlock(ConfigurationSection blockSection) {
        if (blockSection.contains("Coins")) {
            this.Coins = true;
            this.MaxCoins = blockSection.getInt("Coins");
        } else {
            var itemsSection = blockSection.getConfigurationSection("Items");
            for (String key : itemsSection.getKeys(false)) {
                this.dropItems.put(key.toUpperCase(), new DropItem(itemsSection.getConfigurationSection(key)));
            }
        }

        if (blockSection.contains("Replace")) {
            this.replacementBlock = Material.valueOf(blockSection.getString("Replace").toUpperCase());
        }

        if (blockSection.contains("Respawn")) {
            this.respawn = blockSection.getInt("Respawn");
        }
    }

    public String getDropName() {
        if (dropItems.isEmpty()) return null;
        if (dropItems.size() == 1) return dropItems.keySet().iterator().next();

        int totalChance = 0;
        for (var item : dropItems.values()) {
            if (item.chance != null) {
                totalChance += item.chance;
            }
        }

        if (totalChance == 0) return dropItems.keySet().iterator().next();

        var random = new Random();
        int randomNumber = random.nextInt(totalChance);
        int currentSum = 0;

        for (var entry : dropItems.entrySet()) {
            var itemName = entry.getKey();
            var item = entry.getValue();

            if (item.chance != null) {
                currentSum += item.chance;
                if (randomNumber < currentSum) {
                    return itemName;
                }
            }
        }
        return null;
    }

    public boolean isDropItem(String drop) {
        return dropItems.containsKey(drop.toUpperCase());
    }

    public DropItem getDropItem(String drop) {
        var dropItem = dropItems.get(drop.toUpperCase());
        if (dropItem == null) return null;
        return dropItem;
    }
}