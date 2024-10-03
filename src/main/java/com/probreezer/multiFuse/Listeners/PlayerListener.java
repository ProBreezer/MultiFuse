package com.probreezer.multiFuse.Listeners;

import com.probreezer.multiFuse.Blocks.MineBlock;
import com.probreezer.multiFuse.Game.GamePlayer;
import com.probreezer.multiFuse.Game.Menu;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.InventoryUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Optional;
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
        var playerId = player.getUniqueId();
        var playerManager = plugin.game.playerManager;
        var isPlayerPresent = playerManager.getPlayer(playerId).isPresent();
        player.sendTitle("§6MultiFuse!", "§1A ProBreezer Gamemode", 10, 70, 20);
        event.setJoinMessage(ChatColor.GOLD + player.getName() + ChatColor.GRAY + " has " + ChatColor.GREEN + "joined" + ChatColor.GRAY + " the game");

        if (plugin.game.state) {
            if (!isPlayerPresent) {
                player.setGameMode(GameMode.SPECTATOR);
                return;
            }
            playerRespawn(player, playerManager.getPlayer(playerId));
        }

        if (!plugin.game.state) {
            if (!isPlayerPresent) {
                playerManager.addPlayer(new GamePlayer(plugin, player));
            }

            InventoryUtils.clearInventory(player);
            Menu.giveMenuItems(plugin, player);
            updateLobbyCountdown(plugin);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var game = plugin.game;
        var gameState = game.state;
        var playerManager = game.playerManager;
        var player = event.getPlayer();
        var teamManager = game.teamManager;
        var team = teamManager.getTeamByPlayer(player.getUniqueId());

        if (Bukkit.getOnlinePlayers().isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, Bukkit::shutdown, 1L);
            return;
        }

        if (gameState && team != null) {
            var teamName = team.name;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                var allTeamPlayersOffline = teamManager.allTeamPlayersOffline(teamName);

                if (allTeamPlayersOffline) {
                    var otherTeam = teamManager.getOtherTeam(teamName);
                    if (otherTeam != null) {
                        game.endGame(otherTeam.name);
                    }
                }
            }, 5L);
        } else {
            playerManager.removePlayer(player.getUniqueId());
            teamManager.removePlayerFromTeam(player);
            Bukkit.getScheduler().runTaskLater(plugin, () -> updateLobbyCountdown(plugin), 1L);
        }
    }


    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victimPlayer) || !(event.getDamager() instanceof Player attackerPlayer)) {
            return;
        }

        var victimTeam = plugin.game.teamManager.getTeamByPlayer(victimPlayer.getUniqueId());
        var attackerTeam = plugin.game.teamManager.getTeamByPlayer(attackerPlayer.getUniqueId());

        var victimTeamName = victimTeam.name;
        var attackerTeamName = attackerTeam.name;

        if (victimTeamName.equalsIgnoreCase(attackerTeamName)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        var teamManager = plugin.game.teamManager;
        var player = event.getEntity();
        var gamePlayer = plugin.game.playerManager.getPlayer(player.getUniqueId());
        var killer = player.getKiller();
        var playerTeam = teamManager.getTeamByPlayer(player.getUniqueId());
        var killerTeam = teamManager.getTeamByPlayer(killer.getUniqueId());
        var playerChatColor = ChatColor.valueOf(playerTeam.name.toUpperCase());
        var killerChatColor = ChatColor.valueOf(killerTeam.name.toUpperCase());

        if (killer != null) {
            event.setDeathMessage(playerChatColor + player.getName() + ChatColor.GRAY + " was killed by " + killerChatColor + killer.getName());
        } else {
            event.setDeathMessage(playerChatColor + player.getName() + ChatColor.GRAY + " killed themselves");
        }

        event.getDrops().clear();
        dropItems(player);
        playerRespawn(player, gamePlayer);
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
        return item -> item != null && (Material.GLOWSTONE_DUST == item.getType() || MineBlock.isDropItem(item.getType()));
    }

    private Predicate<ItemStack> getKeepItems(Player player) {
        return item -> item != null && (Material.GLOWSTONE_DUST == item.getType() || MineBlock.isDropItem(item.getType()) || isArmor(player, item.getType()) || item.getType() == Material.SHIELD);
    }

    private void playerRespawn(Player player, Optional<GamePlayer> gamePlayer) {
        var config =  plugin.getConfig();
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

                if (gamePlayer.isPresent() && gamePlayer.get().spawn != null) {
                    player.teleport(gamePlayer.get().spawn);
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (gamePlayer.isPresent() && gamePlayer.get().spawn != null) {
                            player.teleport(gamePlayer.get().spawn);
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
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        var pickupItem = event.getItem();
        var itemStack = pickupItem.getItemStack();
        var dropItem = itemStack.getType();
        var player = event.getPlayer();
        var droppedItem = MineBlock.getByDropItem(dropItem);

        if (droppedItem == null) return;

        var maxAmount = droppedItem.getMaxQuantity();
        if (maxAmount == null) return;

        event.setCancelled(true);

        int amountToPickup = itemStack.getAmount();
        Integer amountPickedUp = InventoryUtils.giveItems(player, dropItem, amountToPickup, maxAmount);

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