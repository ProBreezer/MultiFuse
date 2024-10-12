package com.probreezer.multiFuse.Listeners;

import com.probreezer.multiFuse.Game.PlayerDataManager;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.InventoryUtils;
import com.probreezer.multiFuse.Utils.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

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
        var blockManager = plugin.game.blockManager;
        var block = event.getBlock();
        var blockType = block.getType();
        var blockName = blockType.name();
        var blockData = block.getBlockData();
        var blockExp = event.getExpToDrop();
        var player = event.getPlayer();
        var tool = player.getInventory().getItemInMainHand();

        event.setDropItems(false);

        if (!blockManager.isMineableBlock(blockName) || !isCorrectTool(block, tool) || onCooldown(player, block)) {
            event.setCancelled(true);
            return;
        }

        var blockProperties = blockManager.getMineableBlock(blockName);

        if (blockProperties == null) {
            event.setCancelled(true);
            return;
        }

        if (blockManager.isCoinBlock(blockName)) {
            var randomNumber = RandomUtils.getWeightedRandom(1, blockProperties.MaxCoins);
            PlayerDataManager.incrementCoins(player, randomNumber);
        } else {
            var dropItemName = blockProperties.getDropName();
            var dropItem = blockProperties.getDropItem(dropItemName);
            var itemsGiven = InventoryUtils.giveItems(event.getPlayer(), Material.valueOf(dropItemName), dropItem.getQuantity(), dropItem.getMaxQuantity(), dropItem.name, dropItem.description);
            if (itemsGiven == null || itemsGiven <= 0) {
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> block.setType(blockProperties.replacementBlock), 1L);
        player.giveExp(blockExp);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            block.setType(blockType);
            block.setBlockData(blockData);
        }, blockProperties.respawn == 0 ? 1L : 20L * (blockProperties.respawn));
    }

    public boolean isCorrectTool(Block block, ItemStack tool) {
        if (block.getType().name().contains("LEAVES")) return true;

        Collection<ItemStack> drops = block.getDrops(tool);
        return !drops.isEmpty();
    }

    public boolean onCooldown(Player player, Block block) {
        var currentTime = System.currentTimeMillis();
        var key = new NamespacedKey(plugin, "lastMinedTime-" + block.getType().name());
        var lastMinedTime = player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.LONG, 0L);

        if (currentTime - lastMinedTime < 333) {
            return true;
        }

        player.getPersistentDataContainer().set(key, PersistentDataType.LONG, currentTime);
        return false;
    }

    @EventHandler
    public void onBlockFall(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            if (event.getBlock().getType() == Material.GRAVEL) {
                event.setCancelled(true);
            }
        }
    }
}
