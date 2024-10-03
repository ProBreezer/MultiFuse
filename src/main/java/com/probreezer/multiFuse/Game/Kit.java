package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;
import java.util.Map;

public class Kit {
    private final MultiFuse plugin;
    private final String name;
    private final Map<Integer, ItemStack> items;
    public Material item;

    public Kit(MultiFuse plugin, String name, Material item) {
        this.plugin = plugin;
        this.name = name;
        this.item = item;
        this.items = new HashMap<>();
    }

    public void addItem(Material material, int amount, int slot) {
        items.put(slot, new ItemStack(material, amount));
    }

    private Color getColour(String team) {
        var teamColor = Color.WHITE;

        switch (team.toUpperCase()) {
            case "RED" -> teamColor = Color.RED;
            case "BLUE" -> teamColor = Color.BLUE;
        }

        return teamColor;
    }

    private DyeColor getDyeColour(String team) {
        var teamColor = DyeColor.WHITE;

        switch (team.toUpperCase()) {
            case "RED" -> teamColor = DyeColor.RED;
            case "BLUE" -> teamColor = DyeColor.BLUE;
        }

        return teamColor;
    }

    private ItemStack setKitColour(ItemStack item, Color colour, DyeColor dyeColour) {
        if (item.getItemMeta() instanceof LeatherArmorMeta meta) {
            meta.setColor(colour);
            item.setItemMeta(meta);
        } else if (item.getItemMeta() instanceof BlockStateMeta meta) {
            if (meta.getBlockState() instanceof Banner banner) {
                banner.setBaseColor(dyeColour);
                banner.addPattern(new Pattern(dyeColour, PatternType.BASE));
                banner.update();
                meta.setBlockState(banner);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    public int getItemCount() {
        return items.size();
    }

    public void applyTo(Player player) {
        var team = plugin.game.teamManager.getTeamByPlayer(player.getUniqueId());
        var teamColour = team != null ? getColour(team.name) : Color.WHITE;
        var teamDyeColour = team != null ? getDyeColour(team.name) : DyeColor.WHITE;
        var inventory = player.getInventory();

        plugin.getLogger().info("Applying kit " + name + " to player [" + team.name + "] " + player.getName());

        items.forEach((slot, item) -> {
            if ((item.getType().name().startsWith("LEATHER_") && item.getType() != Material.LEATHER) || item.getType() == Material.SHIELD) {
                setKitColour(item, teamColour, teamDyeColour);
            }

            if (slot >= 100) {
                switch (slot) {
                    case 100 -> inventory.setBoots(item);
                    case 101 -> inventory.setLeggings(item);
                    case 102 -> inventory.setChestplate(item);
                    case 103 -> inventory.setHelmet(item);
                    case 104 -> inventory.setItemInOffHand(item);
                }
            } else {
                inventory.setItem(slot, item);
            }
        });
    }
}