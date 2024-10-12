package com.probreezer.multiFuse.Blocks;

import com.probreezer.multiFuse.Utils.RandomUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;

public class DropItem {
    public String name = null;
    public String description = null;
    public int quantity = 1;
    public ArrayList<Integer> randomQuantity = new ArrayList<>();
    public Integer maxQuantity = null;
    public Integer chance;

    public DropItem(ConfigurationSection itemSection) {
        if (itemSection.contains("Name")) {
            this.name = itemSection.getString("Name");
        }

        if (itemSection.contains("Description")) {
            this.name = itemSection.getString("Name");
        }

        if (itemSection.contains("Quantity")) {
            this.quantity = itemSection.getInt("Quantity");
        }

        if (itemSection.contains("RandomQuantity")) {
            var range = itemSection.getString("RandomQuantity").split(",");
            for (String value : range) {
                this.randomQuantity.add(Integer.parseInt(value.trim()));
            }
        }

        if (itemSection.contains("MaxQuantity")) {
            this.maxQuantity = itemSection.getInt("MaxQuantity");
        }

        if (itemSection.contains("Chance")) {
            this.chance = itemSection.getInt("Chance");
        }
    }

    public int getQuantity() {
        if (this.randomQuantity.isEmpty()) return this.quantity;

        var randomNumber = RandomUtils.getWeightedRandom(this.randomQuantity.get(0), this.randomQuantity.get(1));
        return randomNumber;
    }

    public Integer getMaxQuantity() {
        return this.maxQuantity;
    }
}
