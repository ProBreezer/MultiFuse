package com.probreezer.multiFuse.Utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;

public class InventoryUtils {
    public static Integer giveItems(Player player, Material dropItem, int quantity, Integer maxQuantity, String customName, String description) {
        var inventory = player.getInventory();

        quantity = getGiveAmount(player, dropItem, quantity, maxQuantity);
        if (quantity <= 0) return 0;

        var itemToAdd = createGiveItemStack(dropItem, quantity, customName, description);

        HashMap<Integer, ItemStack> leftover = inventory.addItem(itemToAdd);
        var actuallyAdded = itemToAdd.getAmount();

        if (!leftover.isEmpty()) {
            actuallyAdded -= leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
            inventory.removeItem(leftover.values().toArray(new ItemStack[0]));
        }

        return actuallyAdded;
    }

    public static int getGiveAmount(Player player, Material dropItem, int quantity, Integer maxQuantity) {
        if (maxQuantity == null) return quantity;

        var inventory = player.getInventory();
        var currentAmount = 0;

        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == dropItem) {
                currentAmount += item.getAmount();
            }
        }

        if ((currentAmount + quantity) > maxQuantity) {
            quantity = maxQuantity - currentAmount;
        }

        return quantity;
    }

    private static ItemStack createGiveItemStack(Material material, int amount, String customName, String description) {
        var item = new ItemStack(material, amount);
        var meta = item.getItemMeta();
        if (meta != null) {
            if (customName != null) {
                meta.displayName(Component.text(customName));
            }
            if (description != null) {
                meta.setLore(Collections.singletonList(description));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static int countItems(Player player, Material checkItem) {
        var inventory = player.getInventory();
        int count = 0;

        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == checkItem) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static void removeItems(Player player, Material removeItem, int quantity) {
        var inventory = player.getInventory();

        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == removeItem) {
                int newAmount = item.getAmount() - quantity;
                if (newAmount > 0) {
                    item.setAmount(newAmount);
                } else {
                    inventory.removeItem(item);
                }
                break;
            }
        }
    }


    public static void clearInventory(Player player) {
        var inventory = player.getInventory();
        inventory.clear();
    }
}

