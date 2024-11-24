package com.probreezer.multiFuse.Listeners;

import com.probreezer.multiFuse.DataManagers.PlayerDataManager;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.InventoryUtils;
import com.probreezer.multiFuse.Utils.ItemUtils;
import com.probreezer.untitledNetworkCore.PrefixManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import static com.probreezer.multiFuse.Blocks.BlockManager.getTeamShopBlock;
import static com.probreezer.multiFuse.Menus.Menu.*;

public class MenuListener implements Listener {

    private final MultiFuse plugin;

    public MenuListener(MultiFuse plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var team = PlayerDataManager.getTeam(player);
        var action = event.getAction();
        var item = player.getInventory().getItemInMainHand();
        var targetBlock = player.getTargetBlockExact(5);
        var meta = item.getItemMeta();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (targetBlock != null) {
            if (targetBlock.getType().name().contains("_SHULKER_BOX")) {
                event.setCancelled(true);
            }

            if (team != null && targetBlock.getType() == getTeamShopBlock(team)) {
                openShopMenu(plugin, player);
                return;
            }
        }

        if (item == null || !item.hasItemMeta() || plugin.game.state) {
            return;
        }

        if (item.getType() == Material.WHITE_WOOL && meta.displayName().equals(Component.text("Select Team", NamedTextColor.GOLD))) {
            openTeamMenu(plugin, player);
            event.setCancelled(true);
            return;
        }

        if (item.getType() == Material.BOOK && meta.displayName().equals(Component.text("Select Class", NamedTextColor.GOLD))) {
            openKitMenu(plugin, player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        var player = (Player) event.getWhoClicked();
        var team = PlayerDataManager.getTeam(player);
        var clickedItem = event.getCurrentItem();

        if (event.getView().title().equals(Component.text("Select Your Team", Style.style(TextDecoration.BOLD)))) {
            event.setCancelled(true);

            if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                var itemName = PlainTextComponentSerializer.plainText().serialize(clickedItem.getItemMeta().displayName());
                var itemNameSplit = itemName.split(" ")[0].trim();
                var teamColour = itemNameSplit.equalsIgnoreCase("No") ? null : itemNameSplit;

                if (team == null || (team != null && !team.equalsIgnoreCase(teamColour))) {
                    PlayerDataManager.setTeam(player, teamColour);
                }
            }
            player.closeInventory();
        }

        if (event.getView().title().equals(Component.text("Select Your Kit", Style.style(TextDecoration.BOLD)))) {
            event.setCancelled(true);

            if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                var itemName = PlainTextComponentSerializer.plainText().serialize(clickedItem.getItemMeta().displayName());
                var kit = itemName;

                if (PlayerDataManager.getKit(player) != kit) {
                    PlayerDataManager.setKit(player, kit);
                }
            }
            player.closeInventory();
        }

        if (event.getView().title().equals(Component.text("Shop", Style.style(TextDecoration.BOLD)))) {
            event.setCancelled(true);

            var shopItemKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("MultiFuse"), "ShopItem");

            if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().getPersistentDataContainer().getOrDefault(shopItemKey, PersistentDataType.STRING, "") == "Shop") {
                var item = clickedItem.getType();
                var key = new NamespacedKey(plugin, "Shop" + item.name());
                var amount = clickedItem.getAmount();
                var cost = clickedItem.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                var playerCoins = PlayerDataManager.getCoins(player);

                if (playerCoins >= cost) {
                    PlayerDataManager.removeCoins(player, cost);
                    InventoryUtils.giveItems(player, item, amount, null, null, null);
                    var itemName = ItemUtils.getFriendlyItemName(item.name());
                    player.sendMessage(PrefixManager.PREFIX.append(Component.text("Purchased: " + amount + "x " + itemName, NamedTextColor.GRAY)));
                } else {
                    player.sendMessage(PrefixManager.ERRORPREFIX.append(Component.text("You do not have enough gold to purchase this item.", NamedTextColor.RED)));
                }
            }
            player.closeInventory();
        }
    }
}
