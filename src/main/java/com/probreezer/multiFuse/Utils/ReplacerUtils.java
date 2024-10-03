package com.probreezer.multiFuse.Utils;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ReplacerUtils {

    private final MultiFuse plugin;
    private final List<BlockState> originalBlocks;

    public ReplacerUtils(MultiFuse plugin) {
        this.plugin = plugin;
        this.originalBlocks = new ArrayList<>();
    }

    public void Remover(Location corner1, Location corner2) {
        var world = plugin.getServer().getWorlds().getFirst();

        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        Set<Material> materialsToReplace = EnumSet.of(
                Material.QUARTZ_BLOCK,
                Material.QUARTZ_STAIRS,
                Material.QUARTZ_SLAB,
                Material.QUARTZ_PILLAR,
                Material.CHISELED_QUARTZ_BLOCK,
                Material.SMOOTH_QUARTZ,
                Material.WHITE_STAINED_GLASS,
                Material.WHITE_STAINED_GLASS_PANE,
                Material.IRON_BARS,
                Material.BEACON
        );

        originalBlocks.clear();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    var block = world.getBlockAt(x, y, z);
                    if (materialsToReplace.contains(block.getType())) {
                        originalBlocks.add(block.getState());
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }

    public void restoreBlocks() {
        for (BlockState state : originalBlocks) {
            state.update(true, false);
        }
        originalBlocks.clear();
    }
}

