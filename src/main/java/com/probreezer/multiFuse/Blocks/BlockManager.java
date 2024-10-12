package com.probreezer.multiFuse.Blocks;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;

public class BlockManager {
    public static HashMap<String, TeamBlock> TeamBlocks = new HashMap<>();
    private final MultiFuse plugin;
    public HashMap<String, MineableBlock> MineableBlocks = new HashMap<>();
    private YamlConfiguration teamConfig;
    private YamlConfiguration blockConfig;

    public BlockManager(MultiFuse plugin) {
        this.plugin = plugin;
        this.teamConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "teams.yml"));
        this.blockConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "blocks.yml"));

        loadTeamBlocks();
        loadMineableBlocks();
    }

    public static Material getTeamFuseBlock(String team) {
        var teamName = team.toLowerCase();
        return TeamBlocks.get(teamName).fuseMaterial;
    }

    public static Material getTeamShopBlock(String team) {
        var teamName = team.toLowerCase();
        return TeamBlocks.get(teamName).shopMaterial;
    }

    private void loadTeamBlocks() {
        var teamsSection = this.teamConfig;

        for (String team : teamsSection.getKeys(false)) {
            var teamName = team.toLowerCase();
            plugin.getLogger().info("Loading blocks - " + teamName + " team");

            var teamSection = teamsSection.getConfigurationSection(team);
            var fuseBlock = teamSection.getString("FuseBlock");
            var shopBlock = teamSection.getString("ShopBlock");

            if (fuseBlock == null || shopBlock == null) {
                plugin.getLogger().severe("Invalid materials  - " + teamName + " team");
                continue;
            }

            var teamBlock = new TeamBlock(teamName, fuseBlock, shopBlock);
            TeamBlocks.put(teamName, teamBlock);
            plugin.getLogger().info("Loaded team " + teamName);
        }
    }

    private void loadMineableBlocks() {
        var blocks = this.blockConfig;

        for (String block : blocks.getKeys(false)) {
            var keyName = block.toUpperCase();
            var blockSection = blocks.getConfigurationSection(block);

            if (keyName.startsWith("%")) {
                var searchTerm = keyName.substring(1);
                for (var material : Material.values()) {
                    if (material.name().contains(searchTerm)) {
                        var blockName = material.name();
                        loadBlock(blockName, blockSection);
                    }
                }
            } else {
                loadBlock(keyName, blockSection);
            }
        }
    }

    private void loadBlock(String blockName, ConfigurationSection blockSection) {
        plugin.getLogger().info("Loading block - " + blockName);
        var mineableBlock = new MineableBlock(blockSection);
        MineableBlocks.put(blockName, mineableBlock);
        plugin.getLogger().info("Loaded block - " + blockName);
    }

    public boolean isMineableBlock(String block) {
        var blockName = block.toUpperCase();
        return MineableBlocks.containsKey(blockName);
    }

    public boolean isCoinBlock(String block) {
        var blockName = block.toUpperCase();
        return MineableBlocks.get(blockName).Coins;
    }

    public MineableBlock getMineableBlock(String block) {
        var blockName = block.toUpperCase();
        return MineableBlocks.get(blockName);
    }

    public boolean isDropItem(String block) {
        var blockName = block.toUpperCase();
        for (var mineableBlock : MineableBlocks.values()) {
            for (var dropItem : mineableBlock.dropItems.keySet()) {
                if (dropItem.equalsIgnoreCase(blockName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public DropItem getDropItem(String block) {
        var blockName = block.toUpperCase();
        for (var mineableBlock : MineableBlocks.values()) {
            for (var dropItem : mineableBlock.dropItems.keySet()) {
                if (dropItem.equalsIgnoreCase(blockName)) {
                    return mineableBlock.dropItems.get(dropItem);
                }
            }
        }
        return null;
    }
}