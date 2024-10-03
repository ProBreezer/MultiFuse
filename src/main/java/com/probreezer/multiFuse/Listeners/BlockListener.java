package com.probreezer.multiFuse.Listeners;

import com.probreezer.multiFuse.Blocks.MineBlock;
import com.probreezer.multiFuse.Game.Fuse;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

public class BlockListener implements Listener {

    private final MultiFuse plugin;

    public BlockListener(MultiFuse plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var block = event.getBlock();
        var originalType = block.getType();
        var originalData = block.getBlockData();

        event.setExpToDrop(0);
        event.setDropItems(false);

        if (!MineBlock.isMineable(originalType)) {
            event.setCancelled(true);
            return;
        }

        var blockProperties = MineBlock.getByBlockMaterial(originalType);
        InventoryUtils.giveItems(event.getPlayer(), blockProperties.dropItem, blockProperties.quantity, blockProperties.getMaxQuantity());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            block.setType(originalType);
            block.setBlockData(originalData);
        }, 20L * (blockProperties.respawnTime != null ? blockProperties.respawnTime : 0));
    }

    @EventHandler
    public void onBlockFall(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            if (event.getBlock().getType() == Material.GRAVEL) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFuseHit(BlockBreakEvent event) {
        var teams = plugin.game.teamManager.teams;
        Fuse fuse = null;

        for (var team : teams.values()) {
            if (team.fuseManager.getFuse(event.getBlock()) != null) {
                fuse = team.fuseManager.getFuse(event.getBlock());
            }
        }

        var player = event.getPlayer();
        var playerTeam = plugin.game.teamManager.getTeamByPlayer(player.getUniqueId());

        if (fuse == null || playerTeam == null) {
            return;
        }

        if (!fuse.colour.equalsIgnoreCase(playerTeam.name)) {
            fuse.takeDamage(playerTeam.name);
        }
    }

    @EventHandler
    public void onFuseHeal(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        var player = event.getPlayer();
        var block = event.getClickedBlock();

        var currentTime = System.currentTimeMillis();
        long lastHealTime = player.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "lastFuseHealTime"), PersistentDataType.LONG, 0L);

        if (currentTime - lastHealTime < 500) {
            return;
        }

        var teamManager = plugin.game.teamManager;
        var teams = teamManager.teams;
        var playerTeam = teamManager.getTeamByPlayer(player.getUniqueId());
        Fuse fuse = null;


        for (var team : teams.values()) {
            if (team.fuseManager.getFuse(block) != null) {
                fuse = team.fuseManager.getFuse(block);
            }
        }

        if (fuse == null || playerTeam == null) {
            return;
        }

        var item = player.getInventory().getItemInMainHand().getType();

        if (!fuse.colour.equalsIgnoreCase(playerTeam.name)) {
            return;
        }

        if (Material.GLOWSTONE_DUST == item) {
            fuse.repair(player);
        }

        if (Material.EMERALD == item) {
            fuse.respawn(player);
        }

        player.getPersistentDataContainer().set(new NamespacedKey(plugin, "lastFuseHealTime"), PersistentDataType.LONG, currentTime);
    }
}
