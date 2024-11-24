package com.probreezer.multiFuse.Menus;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Menu {
    public static void giveMenuItems(MultiFuse plugin, Player player) {
        var configFile = new File(plugin.getDataFolder(), "menus.yml");
        var config = YamlConfiguration.loadConfiguration(configFile);
        var menuConfig = config.getConfigurationSection("menus");

        if (menuConfig == null) {
            plugin.getLogger().severe("No menu configuration found");
            return;
        }

        var inventory = player.getInventory();
        inventory.clear();

        var placement = 0;

        for (String key : menuConfig.getKeys(false)) {
            var sectionConfig = menuConfig.getConfigurationSection(key);
            var material = Optional.ofNullable(sectionConfig.getString("material")).orElse("WHITE_WOOL");
            var colour = Optional.ofNullable(sectionConfig.getString("colour")).orElse("GOLD");
            var prompt = Optional.ofNullable(sectionConfig.getString("prompt")).orElse("");

            var item = createMenuItem(Material.valueOf(material), ChatColor.valueOf(colour) + prompt);
            inventory.setItem(placement, item);
            placement++;
        }
    }

    public static void openTeamMenu(MultiFuse plugin, Player player) {
        var game = plugin.game;
        var teamMenu = Bukkit.createInventory(null, 9, ChatColor.BOLD + "Select Your Team");

        var teams = game.teams;
        var playerTeam = PlayerDataManager.getTeam(player);
        var placement = 0;

        for (var team : teams) {
            var teamIcon = createPlayerMenuItem(Material.valueOf(team.toUpperCase() + "_WOOL"), team);
            if (playerTeam != null && playerTeam.equalsIgnoreCase(team)) applyEnchantment(teamIcon);
            teamMenu.setItem(placement, teamIcon);

            placement++;
        }

        var noTeam = createMenuItem(Material.GRAY_WOOL, ChatColor.GRAY + "No Team");
        if (playerTeam == null) applyEnchantment(noTeam);
        teamMenu.setItem(8, noTeam);

        player.openInventory(teamMenu);
    }

    public static void openKitMenu(MultiFuse plugin, Player player) {
        var classMenu = Bukkit.createInventory(null, 9, ChatColor.BOLD + "Select Your Kit");
        var kits = plugin.game.kitManager.kits;
        var placement = 0;

        for (String kitName : kits.keySet()) {
            var kit = kits.get(kitName);
            var kitIcon = createMenuItem(kit.item, ChatColor.GRAY + kitName);
            var playerKit = PlayerDataManager.getKit(player);

            if (kitName.equals(playerKit)) applyEnchantment(kitIcon);

            classMenu.setItem(placement, kitIcon);
            placement++;
        }

        player.openInventory(classMenu);
    }

    public static void openShopMenu(MultiFuse plugin, Player player) {
        var shopMenu = Bukkit.createInventory(null, 54, ChatColor.BOLD + "Shop");
        var shopConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "shop.yml"));

        for (var shopItem : shopConfig.getKeys(false)) {
            var shopItemConfig = shopConfig.getConfigurationSection(shopItem);
            var shopIcon = createShopMenuItem(Material.valueOf(shopItem.toUpperCase()), shopItemConfig);
            var placement = shopItemConfig.getInt("Slot");
            shopMenu.setItem(placement, shopIcon);
        }

        player.openInventory(shopMenu);
    }


    private static ItemStack createMenuItem(Material material, String displayName) {
        var item = new ItemStack(material);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createPlayerMenuItem(Material material, String team) {
        var playerNames = PlayerDataManager.getTeamPlayers(team);
        var item = new ItemStack(material);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.valueOf(team.toUpperCase()) + team + " Team");

            if (!playerNames.isEmpty()) {
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Players:");
                for (String playerName : playerNames) {
                    lore.add(ChatColor.GRAY + "- " + playerName);
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createShopMenuItem(Material material, ConfigurationSection config) {
        var item = new ItemStack(material);
        var meta = item.getItemMeta();

        if (meta != null && config.contains("Cost")) {
            var cost = config.getInt("Cost");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "Cost: " + cost + " Gold Coins");
            meta.setLore(lore);

            var shopItemKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("MultiFuse"), "ShopItem");
            var costKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("MultiFuse"), "Shop" + material.name());
            meta.getPersistentDataContainer().set(shopItemKey, PersistentDataType.STRING, "Shop");
            meta.getPersistentDataContainer().set(costKey, PersistentDataType.INTEGER, cost);

            item.setItemMeta(meta);
        }

        if (config.contains("Amount")) {
            item.setAmount(config.getInt("Amount"));
        }

        return item;
    }

    private static void applyEnchantment(ItemStack item) {
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.LOYALTY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
    }
}
