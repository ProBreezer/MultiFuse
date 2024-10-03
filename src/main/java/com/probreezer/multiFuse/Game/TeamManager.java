package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamManager {
    public final Map<String, Team> teams = new HashMap<>();
    private final MultiFuse plugin;

    public TeamManager(MultiFuse plugin) {
        this.plugin = plugin;

        var config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "teams.yml"));

        config.getKeys(false).stream()
                .forEach(team -> {
                    plugin.getLogger().info("Setting up: " + team);
                    var teamSection = config.getConfigurationSection(team);
                    var spawnAreas = teamSection.getStringList("SpawnArea");

                    var newTeam = new Team(this.plugin, team, spawnAreas);
                    teams.put(team, newTeam);
                });

    }

    public Team getTeam(String name) {
        return teams.get(name);
    }

    public Team getOtherTeam(String name) {
        for (var team : teams.values()) {
            if (team.name != name) return team;
        }
        return null;
    }

    public void addPlayerToTeam(String teamName, Player player, GamePlayer gamePlayer) {
        plugin.getLogger().info("Adding player to " + teamName + " team: " + player.getName());
        for (var team : teams.values()) {
            if (team.players.containsKey(player.getUniqueId())) {
                team.players.remove(player.getUniqueId());
                break;
            }
        }

        if (teamName == null) return;
        var team = getTeam(teamName);
        team.players.put(player.getUniqueId(), gamePlayer);
    }

    public void removePlayerFromTeam(Player player) {
        for (var team : teams.values()) {
            if (team.players.containsKey(player.getUniqueId())) {
                plugin.getLogger().info("Removing player from " + team.name + " team: " + player.getName());
                team.players.remove(player.getUniqueId());
                break;
            }
        }
    }

    public boolean isPlayerInATeam(UUID playerId) {
        for (var team : teams.values()) {
            if (team.players.containsKey(playerId))
                return true;
        }
        return false;
    }

    public Team getTeamByPlayer(UUID playerId) {
        for (var team : teams.values()) {
            if (team.players.containsKey(playerId)) {
                return team;
            }
        }
        return null;
    }

    public List<String> getTeamPlayers(String teamName) {
        var team = getTeam(teamName);
        if (team == null || team.players.size() == 0) return null;
        return team.players.values().stream()
                .map(gamePlayer -> gamePlayer.name)
                .collect(Collectors.toList());
    }

    public List<String> getOtherTeamPlayers(String teamName) {
        for (var team : teams.values()) {
            if (team.name != teamName) {
                return team.players.values().stream()
                        .map(gamePlayer -> gamePlayer.name)
                        .collect(Collectors.toList());
            }
        }
        return null;
    }

    public String getTeamWithLowestAmountOfPlayers() {
        var teams = plugin.game.teamManager.teams;
        var lowestAmountOfPlayers = Integer.MAX_VALUE;
        var teamWithLowestAmountOfPlayers = "";

        for (var team : teams.values()) {
            var amountOfPlayers = team.players.size();

            if (amountOfPlayers <= lowestAmountOfPlayers) {
                lowestAmountOfPlayers = amountOfPlayers;
                teamWithLowestAmountOfPlayers = team.name;
            }
        }
        return teamWithLowestAmountOfPlayers;
    }

    public String getTeamWithHighestFuseHealth() {
        var teams = plugin.game.teamManager.teams;
        var highestHealth = -1;
        var teamWithHighestHealth = "";

        for (var team : teams.values()) {
            var health = team.fuseManager.getTeamFuseHealth();

            if (health == highestHealth)
                teamWithHighestHealth = "draw";

            if (health > highestHealth) {
                highestHealth = health;
                teamWithHighestHealth = team.name;
            }
        }
        return teamWithHighestHealth;
    }

    public boolean allTeamPlayersOffline(String teamName) {
        var team = teams.get(teamName);

        if (team == null || team.players.isEmpty()) {
            return true;
        }

        for (GamePlayer player : team.players.values()) {
            var onlinePlayer = Bukkit.getPlayer(player.Id);
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                plugin.getLogger().info(player.name + " is online");
                return false;
            }
        }

        return true;
    }
}
