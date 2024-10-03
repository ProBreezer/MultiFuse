package com.probreezer.multiFuse.Utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class InventoryUtils {
    public static Integer giveItems(Player player, Material dropItem, int quantity, Integer maxQuantity) {
        var inventory = player.getInventory();

        if (maxQuantity != null) {
            int currentAmount = 0;
            for (ItemStack item : inventory.getContents()) {
                if (item != null && (item.getType() == dropItem ||
                        (dropItem == Material.GLOWSTONE_DUST && item.getItemMeta().getDisplayName().equals("Repair Dust")))) {
                    currentAmount += item.getAmount();
                }
            }

            if ((currentAmount + quantity) > maxQuantity) {
                quantity = maxQuantity - currentAmount;
            }

            if (quantity <= 0) return 0;
        }

        ItemStack itemToAdd;
        if (dropItem == Material.GLOWSTONE_DUST) {
            itemToAdd = new ItemStack(dropItem, quantity);
            var meta = itemToAdd.getItemMeta();
            meta.setDisplayName("Repair Dust");
            itemToAdd.setItemMeta(meta);
        } else {
            itemToAdd = new ItemStack(dropItem, quantity);
        }

        HashMap<Integer, ItemStack> leftover = inventory.addItem(itemToAdd);
        var actuallyAdded = itemToAdd.getAmount();

        if (!leftover.isEmpty()) {
            actuallyAdded -= leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
            inventory.removeItem(leftover.values().toArray(new ItemStack[0]));
        }

        return actuallyAdded;
    }


    public static void clearInventory(Player player) {
        var inventory = player.getInventory();
        inventory.clear();
    }
}

