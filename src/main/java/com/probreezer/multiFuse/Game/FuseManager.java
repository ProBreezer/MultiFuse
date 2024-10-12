package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.ConfigUtils;
import com.probreezer.multiFuse.Utils.Coordinates;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FuseManager {

    private final MultiFuse plugin;
    private final Game game;
    private final YamlConfiguration config;
    public List<Fuse> fuses = new ArrayList<>();

    public FuseManager(MultiFuse plugin, ArrayList<String> teams) {
        this.plugin = plugin;
        this.game = plugin.game;
        this.config = ConfigUtils.getConfig("teams");

        setFuses(teams);
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

    public void setFuses(ArrayList<String> teams) {
        var config = plugin.getConfig();

        for (var team : this.config.getKeys(false)) {
            var teamSection = this.config.getConfigurationSection(team);
            var fuses = teamSection.getStringList("Fuses");
            var totalFuseHealth = config.getInt("TotalFuseHealth", 100);
            var numberOfFuses = fuses.size();
            var shop = new Shop(plugin, team);

            for (int i = 0; i < numberOfFuses; i++) {
                var Fuse = fuses.get(i);
                var id = i + 1;
                var health = totalFuseHealth / numberOfFuses;
                var FuseCoordinates = new Coordinates(Fuse);
                var fuse = new Fuse(plugin, this, id, team, health, FuseCoordinates);
                this.fuses.add(fuse);
            }
        }
    }

    public List<Fuse> getFuses(String team) {
        return fuses.stream()
                .filter(fuse -> fuse.colour.equalsIgnoreCase(team))
                .toList();
    }

    public int getTeamFuseHealth(String team) {
        return fuses.stream()
                .filter(fuse -> fuse.colour.equalsIgnoreCase(team))
                .mapToInt(fuse -> (int) fuse.health)
                .sum();
    }

    public String getTeamWithHighestTotalFuseHealth() {
        return fuses.stream()
                .collect(Collectors.groupingBy(Fuse::getTeamColour, Collectors.summingInt(Fuse::getHealth)))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
