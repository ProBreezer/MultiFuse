package com.probreezer.multiFuse.Listeners;

import com.probreezer.multiFuse.Game.Menu;
import com.probreezer.multiFuse.Game.PlayerDataManager;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.InventoryUtils;
import com.probreezer.multiFuse.Utils.Text;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.function.Predicate;

import static com.probreezer.multiFuse.Utils.CountdownManager.updateLobbyCountdown;

public class PlayerListener implements Listener {

    private final MultiFuse plugin;

    public PlayerListener(MultiFuse plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var team = PlayerDataManager.getTeam(player);
        var isPlayerPresent = team != null;
        player.sendTitle("§6MultiFuse!", "§1A ProBreezer Gamemode", 10, 70, 20);
        event.setJoinMessage(Text.getRolePrefix(player) + ChatColor.GOLD + player.getName() + ChatColor.GRAY + " has " + ChatColor.GREEN + "joined" + ChatColor.GRAY + " the game");
        team = team != null ? team : "Gray";
        plugin.game.scoreboard.setPlayerTeam(player, team);

        if (plugin.game.state) {
            if (!isPlayerPresent) {
                player.setGameMode(GameMode.SPECTATOR);
                return;
            }
            playerRespawn(player);
        }

        if (!plugin.game.state) {
            InventoryUtils.clearInventory(player);
            Menu.giveMenuItems(plugin, player);
            updateLobbyCountdown(plugin);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var game = plugin.game;
        var gameState = game.state;
        var player = event.getPlayer();
        var team = PlayerDataManager.getTeam(player);

        event.setQuitMessage(Text.getRolePrefix(player) + ChatColor.GOLD + player.getName() + ChatColor.GRAY + " has " + ChatColor.RED + "quit" + ChatColor.GRAY + " the game");

        if (Bukkit.getOnlinePlayers().isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, Bukkit::shutdown, 1L);
            return;
        }

        if (gameState && team != null) {
            var teamName = team;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                var allTeamPlayersOffline = PlayerDataManager.AllTeamPlayersOffline(teamName);

                if (allTeamPlayersOffline) {
                    var otherTeam = PlayerDataManager.getTeam(PlayerDataManager.getOtherTeamPlayers(teamName).get(0));
                    if (otherTeam != null) {
                        game.endGame(otherTeam);
                    }
                }
            }, 5L);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> updateLobbyCountdown(plugin), 1L);
        }
    }


    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victimPlayer) || !(event.getDamager() instanceof Player attackerPlayer)) {
            return;
        }

        var victimTeam = PlayerDataManager.getTeam(victimPlayer);
        var attackerTeam = PlayerDataManager.getTeam(attackerPlayer);

        var victimTeamName = victimTeam;
        var attackerTeamName = attackerTeam;

        if (victimTeamName.equalsIgnoreCase(attackerTeamName)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        var player = event.getEntity();
        var killer = player.getKiller();
        var playerTeam = PlayerDataManager.getTeam(player);
        var playerChatColor = ChatColor.valueOf(playerTeam.toUpperCase());

        if (killer != null) {
            PlayerDataManager.incrementKills(killer);
            var killerTeam = PlayerDataManager.getTeam(killer);
            var killerChatColor = ChatColor.valueOf(killerTeam.toUpperCase());
            event.setDeathMessage(playerChatColor + player.getName() + ChatColor.GRAY + " was killed by " + killerChatColor + killer.getName());
        } else {
            event.setDeathMessage(playerChatColor + player.getName() + ChatColor.GRAY + " killed themselves");
        }

        PlayerDataManager.incrementDeaths(player);
        event.getDrops().clear();
        dropItems(player);
        playerRespawn(player);
    }

    private void dropItems(Player player) {
        Predicate<ItemStack> shouldDropItems = getShouldDropItems(player);
        ItemStack[] drops = Arrays.stream(player.getInventory().getContents())
                .filter(item -> item != null && shouldDropItems.test(item))
                .toArray(ItemStack[]::new);

        for (ItemStack item : drops) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            player.getInventory().remove(item);
        }
    }


    private Predicate<ItemStack> getShouldDropItems(Player player) {
        return item -> item != null && (plugin.game.blockManager.isDropItem(item.getType().name()));
    }

    private Predicate<ItemStack> getKeepItems(Player player) {
        return item -> item != null && (plugin.game.blockManager.isDropItem(item.getType().name()) || isArmor(player, item.getType()) || item.getType() == Material.SHIELD);
    }

    private void playerRespawn(Player player) {
        var config = plugin.getConfig();
        var respawnTime = config.getInt("RespawnTime");
        var shouldKeepItems = getKeepItems(player);

        var inventory = Arrays.stream(player.getInventory().getContents())
                .filter(shouldKeepItems.negate())
                .toArray(ItemStack[]::new);
        var armor = player.getInventory().getArmorContents();
        var offHand = player.getInventory().getItemInOffHand();

        new BukkitRunnable() {
            @Override
            public void run() {
                player.spigot().respawn();
                player.getInventory().clear();
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(ChatColor.GRAY + "You will respawn in 10 seconds...");

                if (PlayerDataManager.getSpawn(player) != null) {
                    player.teleport(PlayerDataManager.getSpawn(player));
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (PlayerDataManager.getSpawn(player) != null) {
                            player.teleport(PlayerDataManager.getSpawn(player));
                        }

                        player.setGameMode(GameMode.SURVIVAL);
                        player.getInventory().setContents(inventory);
                        player.getInventory().setItemInOffHand(offHand);
                        player.getInventory().setArmorContents(armor);

                        player.setHealth(player.getMaxHealth());
                        player.setFoodLevel(20);
                        player.setSaturation(20f);
                    }
                }.runTaskLater(plugin, respawnTime * 20L);
            }
        }.runTaskLater(plugin, 5L);
    }

    private boolean isArmor(Player player, Material material) {
        var inventory = player.getInventory();
        var helmet = inventory.getHelmet();
        var chestplate = inventory.getChestplate();
        var leggings = inventory.getLeggings();
        var boots = inventory.getBoots();

        return material.name().endsWith("_HELMET") && helmet != null && helmet.getType() == material
                || material.name().endsWith("_CHESTPLATE") && chestplate != null && chestplate.getType() == material
                || material.name().endsWith("_LEGGINGS") && leggings != null && leggings.getType() == material
                || material.name().endsWith("_BOOTS") && boots != null && boots.getType() == material;
    }


    @EventHandler
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.game.state) {
                return;
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        var pickupItem = event.getItem();
        var itemStack = pickupItem.getItemStack();
        var pickupItemType = itemStack.getType();
        var pickupItemName = itemStack.getItemMeta().getDisplayName();
        var pickupItemLore = itemStack.getItemMeta().getLore();
        String pickupItemDescription = null;
        if (pickupItemLore != null) {
            pickupItemDescription = pickupItemLore.getFirst();
        }
        var player = event.getPlayer();
        var droppedItem = plugin.game.blockManager.getDropItem(pickupItemType.name());

        if (droppedItem == null) return;

        var maxAmount = droppedItem.getMaxQuantity();
        if (maxAmount == null) return;

        event.setCancelled(true);

        var amountToPickup = itemStack.getAmount();
        var amountPickedUp = InventoryUtils.giveItems(player, pickupItemType, amountToPickup, maxAmount, pickupItemName, pickupItemDescription);

        if (amountPickedUp == null || amountPickedUp == 0) return;

        if (amountPickedUp == itemStack.getAmount()) {
            pickupItem.remove();
        } else {
            itemStack.setAmount(itemStack.getAmount() - amountPickedUp);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, 1.0f);
    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!plugin.game.state) event.setCancelled(true);
    }
}