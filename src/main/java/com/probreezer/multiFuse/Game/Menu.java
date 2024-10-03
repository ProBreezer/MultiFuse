package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

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

        var teams = game.teamManager.teams;
        var placement = 0;

        for (Team team : teams.values()) {
            var teamIcon = createPlayerMenuItem(game, Material.valueOf(team.name.toUpperCase() + "_WOOL"), team.name);
            if (team.isPlayerInTeam(player.getUniqueId())) applyEnchantment(teamIcon);
            teamMenu.setItem(placement, teamIcon);

            placement++;
        }

        var noTeam = createMenuItem(Material.GRAY_WOOL, ChatColor.GRAY + "No Team");
        if (!game.teamManager.isPlayerInATeam(player.getUniqueId())) applyEnchantment(noTeam);
        teamMenu.setItem(8, noTeam);

        player.openInventory(teamMenu);
    }

    public static void openClassMenu(MultiFuse plugin, Player player) {
        var classMenu = Bukkit.createInventory(null, 9, ChatColor.BOLD + "Select Your Class");
        var kits = plugin.game.kitManager.kits;
        var gamePlayerOptional = plugin.game.playerManager.getPlayer(player.getUniqueId());
        var placement = 0;

        for (String kitName : kits.keySet()) {
            var kit = kits.get(kitName);
            var kitIcon = createMenuItem(kit.item, ChatColor.GRAY + kitName);

            if (gamePlayerOptional.isPresent()) {
                var gamePlayer = gamePlayerOptional.get();
                if (kitName.equals(gamePlayer.kit)) applyEnchantment(kitIcon);
            }

            classMenu.setItem(placement, kitIcon);
            placement++;
        }

        player.openInventory(classMenu);
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

    private static ItemStack createPlayerMenuItem(Game game, Material material, String team) {
        var playerNames = game.teamManager.getTeamPlayers(team);
        var item = new ItemStack(material);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.valueOf(team.toUpperCase()) + team + " Team");

            if (playerNames != null) {
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

    private static void applyEnchantment(ItemStack item) {
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.LOYALTY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
    }
}
