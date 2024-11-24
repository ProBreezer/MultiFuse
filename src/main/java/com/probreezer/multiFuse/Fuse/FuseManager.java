package com.probreezer.multiFuse.Fuse;

import com.probreezer.multiFuse.Game.Game;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Shop.Shop;
import com.probreezer.multiFuse.Utils.ConfigUtils;
import com.probreezer.multiFuse.Utils.Coordinates;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FuseManager {

    private final MultiFuse plugin;
    private final Game game;
    private final YamlConfiguration config;
    public List<Fuse> fuses = new ArrayList<>();

    public FuseManager(MultiFuse plugin) {
        this.plugin = plugin;
        this.game = plugin.game;
        this.config = ConfigUtils.getConfig("teams");

        setFuses();
    }

    public Fuse getFuse(Block block) {
        return fuses.stream()
                .filter(fuse -> fuse.block.equals(block))
                .findFirst()
                .orElse(null);
    }

    public void setFuses() {
        var config = plugin.getConfig();

        for (var team : this.config.getKeys(false)) {
            var teamSection = this.config.getConfigurationSection(team);
            var fuses = teamSection.getStringList("Fuses");
            var totalFuseHealth = config.getInt("TotalFuseHealth", 100);
            var numberOfFuses = fuses.size();
            new Shop(plugin, team);

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
        String winningTeam = null;

        Map<String, Integer> teamScores = fuses.stream()
                .collect(Collectors.groupingBy(Fuse::getTeamColour, Collectors.summingInt(Fuse::getHealth)));

        Optional<Map.Entry<String, Integer>> maxEntry = teamScores.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        if (maxEntry.isPresent()) {
            var maxScore = maxEntry.get().getValue();
            var teamsWithMaxScore = teamScores.values().stream().filter(score -> score == maxScore).count();

            if (teamsWithMaxScore == 1) {
                winningTeam = maxEntry.get().getKey();
            }
        }
        return winningTeam;
    }
}
