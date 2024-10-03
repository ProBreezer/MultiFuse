package com.probreezer.multiFuse.Blocks;

import org.bukkit.Material;

public enum MineBlock {
    MELON(Material.MELON, Material.MELON_SLICE, 5, 4),
    OAK_WOOD_PLANKS(Material.OAK_WOOD, Material.OAK_PLANKS, 10, 4),
    SPRUCE_WOOD_PLANKS(Material.SPRUCE_WOOD, Material.SPRUCE_PLANKS, 10, 4),
    OAK_LOG_PLANKS(Material.OAK_LOG, Material.OAK_PLANKS, 10, 4),
    SPRUCE_LOG_PLANKS(Material.SPRUCE_LOG, Material.SPRUCE_PLANKS, 10, 4),
    OAK_LEAVES(Material.OAK_LEAVES, Material.FEATHER, null, 1),
    BIRCH_LEAVES(Material.BIRCH_LEAVES, Material.FEATHER, null, 1),
    SPRUCE_LEAVES(Material.SPRUCE_LEAVES, Material.FEATHER, null, 1),
    STONE(Material.STONE, Material.COBBLESTONE, null, 1),
    GRAVEL(Material.GRAVEL, Material.FLINT, 10, 1),
    COAL(Material.COAL_ORE, Material.COAL, 10, 1),
    COPPER(Material.COPPER_ORE, Material.GLOWSTONE_DUST, 10, 1),
    IRON(Material.IRON_ORE, Material.IRON_INGOT, 30, 1),
    GOLD(Material.GOLD_ORE, Material.GOLD_INGOT, 30, 1),
    DIAMOND(Material.DIAMOND_ORE, Material.DIAMOND, 60, 1),
    EMERALD(Material.EMERALD_ORE, Material.EMERALD, 60, 1);

    public final Material dropItem;
    public final Integer respawnTime;
    public final int quantity;
    private final Material blockMaterial;

    MineBlock(Material blockMaterial, Material dropItem, Integer respawnTime, int quantity) {
        this.blockMaterial = blockMaterial;
        this.dropItem = dropItem;
        this.respawnTime = respawnTime;
        this.quantity = quantity;
    }

    public static MineBlock getByBlockMaterial(Material material) {
        for (MineBlock mineBlock : values()) {
            if (mineBlock.blockMaterial == material) {
                return mineBlock;
            }
        }
        return null;
    }

    public static MineBlock getByDropItem(Material material) {
        for (MineBlock mineBlock : values()) {
            if (mineBlock.dropItem == material) {
                return mineBlock;
            }
        }
        return null;
    }

    public static boolean isMineable(Material material) {
        for (MineBlock mineBlock : values()) {
            if (mineBlock.blockMaterial == material) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDropItem(Material material) {
        for (MineBlock mineBlock : values()) {
            if (mineBlock.dropItem == material) {
                return true;
            }
        }
        return false;
    }

    public Integer getMaxQuantity() {
        if (dropItem == Material.EMERALD) {
            return 1;
        }

        if (dropItem == Material.GLOWSTONE_DUST) {
            return 5;
        }
        return null;
    }
}