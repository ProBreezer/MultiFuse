package com.probreezer.multiFuse.Listeners;

import com.probreezer.multiFuse.Game.PlayerDataManager;
import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

public class FuseListener implements Listener {

    private final MultiFuse plugin;

    public FuseListener(MultiFuse plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFuseHit(BlockBreakEvent event) {
        var fuseManager = plugin.game.fuseManager;
        var fuse = fuseManager.getFuse(event.getBlock());

        var player = event.getPlayer();
        var playerTeam = PlayerDataManager.getTeam(player);

        if (fuse == null || playerTeam == null) {
            return;
        }

        if (!fuse.colour.equalsIgnoreCase(playerTeam)) {
            fuse.takeDamage(playerTeam);
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
        var lastHealTime = player.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "lastFuseHealTime"), PersistentDataType.LONG, 0L);

        if (currentTime - lastHealTime < 500) {
            return;
        }

        var playerTeam = PlayerDataManager.getTeam(player);
        var fuseManager = plugin.game.fuseManager;
        var fuse = fuseManager.getFuse(block);

        if (fuse == null || playerTeam == null) {
            return;
        }

        var item = player.getInventory().getItemInMainHand().getType();

        if (!fuse.colour.equalsIgnoreCase(playerTeam)) {
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
