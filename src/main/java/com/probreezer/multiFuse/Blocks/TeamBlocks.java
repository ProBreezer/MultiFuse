package com.probreezer.multiFuse.Blocks;

import com.probreezer.multiFuse.Game.Team;
import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

public class TeamBlocks {
    private final Team team;
    private final YamlConfiguration config;
    public Material blockMaterial;

    public TeamBlocks(Team team) {
        this.team = team;
        this.config = team.config;
    }

    public void loadBlocks(MultiFuse plugin) {
        var teamSection = this.config.getConfigurationSection(this.team.name);

        plugin.getLogger().info("Loading team blocks" + team);
        plugin.getLogger().info(teamSection.getString("FuseBlock"));

        this.blockMaterial = Material.getMaterial(teamSection.getString("FuseBlock"));

        if (blockMaterial == null) {
            plugin.getLogger().severe("Invalid materials for team " + this.team.name);
        }

        plugin.getLogger().info("Loaded team " + team);
    }
}
