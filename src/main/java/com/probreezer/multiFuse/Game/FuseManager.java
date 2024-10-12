package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.Blocks.TeamBlocks;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.Coordinates;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class FuseManager {

    private final MultiFuse plugin;
    private final Team team;
    private final TeamBlocks teamBlocks;
    private final YamlConfiguration config;
    public List<Fuse> fuses = new ArrayList<>();

    public FuseManager(MultiFuse plugin, Team team) {
        this.plugin = plugin;
        this.team = team;
        this.teamBlocks = team.teamBlocks;
        this.config = team.config;
    }

    public void setFuses() {
        var teamColour = team.name;
        var teamSection = this.config.getConfigurationSection(teamColour);
        var fuses = teamSection.getStringList("Fuses");
        var numberOfFuses = fuses.size();
            var shop = new Shop(plugin, team);

        for (int i = 0; i < numberOfFuses; i++) {
            var Fuse = fuses.get(i);
            var id = i + 1;
            var health = 100 / numberOfFuses;
            var FuseCoordinates = new Coordinates(Fuse);
            var fuse = new Fuse(plugin, teamBlocks, id, teamColour, health, FuseCoordinates);
            this.fuses.add(fuse);
        }
    }

    public Fuse getFuse(Block block) {
        return fuses.stream()
                .filter(fuse -> fuse.block.equals(block))
                .findFirst()
                .orElse(null);
    }

    public List<Fuse> getFuses() {
        return this.fuses;
    }

    public int getTeamFuseHealth() {
        return fuses.stream()
                .mapToInt(fuse -> (int) fuse.health)
                .sum();
    }
}
