package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.Blocks.TeamBlocks;
import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Team {
    public MultiFuse plugin;
    public YamlConfiguration config;
    public String name;
    public Map<UUID, GamePlayer> players = new HashMap<>();
    public TeamSpawnArea spawnArea;
    public TeamBlocks teamBlocks;
    public FuseManager fuseManager;

    public Team(MultiFuse plugin, String name, List<String> spawnAreas) {
        this.plugin = plugin;
        this.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "teams.yml"));
        this.name = name;
        this.spawnArea = new TeamSpawnArea(spawnAreas.get(0), spawnAreas.get(1));
        this.teamBlocks = new TeamBlocks(this);
        this.teamBlocks.loadBlocks(plugin);
        plugin.getLogger().info("Team " + name + " blocks has been setup");
        this.fuseManager = new FuseManager(this.plugin, this);
        this.fuseManager.setFuses();
    }

    public boolean isPlayerInTeam(UUID playerId) {
        return this.players.containsKey(playerId);
    }
}
