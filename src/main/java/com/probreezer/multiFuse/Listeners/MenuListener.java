package com.probreezer.multiFuse.Listeners;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.probreezer.multiFuse.Game.Menu.openClassMenu;
import static com.probreezer.multiFuse.Game.Menu.openTeamMenu;

public class MenuListener implements Listener {

    private final MultiFuse plugin;

    public MenuListener(MultiFuse plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && !plugin.game.state) {
            var player = event.getPlayer();
            var item = player.getInventory().getItemInMainHand();
            var meta = item.getItemMeta();

            if (item == null || !item.hasItemMeta()) {
                return;
            }

            if (item.getType() == Material.WHITE_WOOL && meta.getDisplayName().equalsIgnoreCase(ChatColor.GOLD + "Select Team")) {
                openTeamMenu(plugin, player);
                event.setCancelled(true);
            }

            if (item.getType() == Material.BOOK && meta.getDisplayName().equalsIgnoreCase(ChatColor.GOLD + "Select Class")) {
                openClassMenu(plugin, player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        var player = (Player) event.getWhoClicked();
        var gamePlayer = plugin.game.playerManager.getPlayer(player.getUniqueId());
        var team = plugin.game.teamManager.getTeamByPlayer(player.getUniqueId());
        var clickedItem = event.getCurrentItem();

        if (event.getView().getTitle().equals(ChatColor.BOLD + "Select Your Team")) {
            event.setCancelled(true);

            if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                var itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                var itemNameSplit = itemName.split(" ")[0].trim();
                var teamColour = itemNameSplit.equalsIgnoreCase("No") ? null : itemNameSplit;

                if (team == null || (team != null && team.name != teamColour)) {
                    gamePlayer.get().setTeam(player, teamColour);
                }
            }
            player.closeInventory();
        }

        if (event.getView().getTitle().equals(ChatColor.BOLD + "Select Your Class")) {
            event.setCancelled(true);

            if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                var itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                var kit = itemName;

                if (gamePlayer.isPresent() && gamePlayer.get().kit != kit) {
                    gamePlayer.get().setKit(kit);
                }
            }
            player.closeInventory();
        }
    }
}
