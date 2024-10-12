package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.ConfigUtils;
import com.probreezer.multiFuse.Utils.LocationUtils;
import com.probreezer.multiFuse.Utils.SpawnPointUtils;
import com.probreezer.multiFuse.Utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataManager {
    private static MultiFuse plugin;
    private static NamespacedKey spawnKey;
    private static NamespacedKey teamKey;
    private static NamespacedKey kitKey;
    private static NamespacedKey coinsKey;
    private static NamespacedKey killsKey;
    private static NamespacedKey deathsKey;

    private PlayerDataManager() {
        // Private constructor to prevent instantiation
    }

    public static void initialise(MultiFuse plugin) {
        PlayerDataManager.plugin = plugin;
        spawnKey = new NamespacedKey(plugin, "spawn");
        teamKey = new NamespacedKey(plugin, "team");
        kitKey = new NamespacedKey(plugin, "kit");
        coinsKey = new NamespacedKey(plugin, "coins");
        killsKey = new NamespacedKey(plugin, "kills");
        deathsKey = new NamespacedKey(plugin, "deaths");
    }

    public static void setTeam(Player player, String team) {
        var currentTeam = getTeam(player);

        if (team != null) {
            var newTeamPlayers = getTeamPlayers(team);
            for (String playerName : newTeamPlayers) {
            }

            var newTeamSize = getTeamPlayers(team).size();
            int otherTeamSize;

            if (currentTeam != null) {
                otherTeamSize = getTeamPlayers(currentTeam).size();
                if (otherTeamSize != 0) otherTeamSize--;
            } else {
                otherTeamSize = getOtherTeamPlayers(team).size();
            }

            newTeamSize++;

            var loadBalancingCheck = (newTeamSize - otherTeamSize) <= 1;

            if (!loadBalancingCheck) {
                player.sendMessage(Text.PREFIX + "Too many players on " + ChatColor.valueOf(team.toUpperCase()) + team + "§7 team");
                return;
            }
            var playerSpawnPoint = SpawnPointUtils.getRandomSpawnPoint(team);
            player.getPersistentDataContainer().set(spawnKey, PersistentDataType.STRING, playerSpawnPoint);
        }

        player.getPersistentDataContainer().set(teamKey, PersistentDataType.STRING, team);

        team = team != null ? team : "Gray";
        plugin.game.scoreboard.setPlayerTeam(player, team);
    }

    public static String getTeam(Player player) {
        return player.getPersistentDataContainer().get(teamKey, PersistentDataType.STRING);
    }

    public static ArrayList<String> getTeamPlayers(String team) {
        var onlinePlayers = Bukkit.getOnlinePlayers();
        var teamPlayers = new ArrayList<String>();

        for (var player : onlinePlayers) {
            if (getTeam(player) == null || !getTeam(player).equalsIgnoreCase(team)) continue;
            teamPlayers.add(player.getName());
        }

        return teamPlayers;
    }

    public static ArrayList<Player> getOtherTeamPlayers(String team) {
        var onlinePlayers = Bukkit.getOnlinePlayers();
        var teamPlayers = new ArrayList<Player>();

        for (var player : onlinePlayers) {
            if (getTeam(player) == null || getTeam(player).equalsIgnoreCase(team)) continue;
            teamPlayers.add(player);
        }

        return teamPlayers;
    }

    public static String getTeamWithLowestAmountOfPlayers() {
        Map<String, Integer> teamCounts = new HashMap<>();
        String lowestTeam = null;
        var lowestCount = Integer.MAX_VALUE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            var team = getTeam(player);
            if (team == null) continue;

            var count = teamCounts.getOrDefault(team, 0) + 1;
            teamCounts.put(team, count);

            if (count < lowestCount) {
                lowestCount = count;
                lowestTeam = team;
            }
        }

        return lowestTeam != null ? lowestTeam : null;
    }

    public static Boolean AllTeamPlayersOffline(String team) {
        var teamPlayers = getTeamPlayers(team);
        return teamPlayers.isEmpty();
    }

    public static Location getSpawn(Player player) {
        var stringLocation = player.getPersistentDataContainer().get(spawnKey, PersistentDataType.STRING);
        return stringLocation.isEmpty() ? null : LocationUtils.stringToLocation(stringLocation);
    }

    public static void setKit(Player player, String kit) {
        player.getPersistentDataContainer().set(kitKey, PersistentDataType.STRING, kit);
    }

    public static String getKit(Player player) {
        var kitConfig = ConfigUtils.getConfig("kits");
        return player.getPersistentDataContainer().getOrDefault(kitKey, PersistentDataType.STRING, kitConfig.getString("default"));
    }

    public static void incrementCoins(Player player, int amount) {
        var coins = getCoins(player);
        player.getPersistentDataContainer().set(coinsKey, PersistentDataType.INTEGER, coins + amount);
    }

    public static void removeCoins(Player player, int amount) {
        var coins = getCoins(player);
        player.getPersistentDataContainer().set(coinsKey, PersistentDataType.INTEGER, coins - amount);
    }

    public static int getCoins(Player player) {
        return player.getPersistentDataContainer().getOrDefault(coinsKey, PersistentDataType.INTEGER, 0);
    }

    public static void incrementKills(Player player) {
        var kills = getKills(player);
        player.getPersistentDataContainer().set(killsKey, PersistentDataType.INTEGER, kills + 1);
    }

    public static int getKills(Player player) {
        return player.getPersistentDataContainer().getOrDefault(killsKey, PersistentDataType.INTEGER, 0);
    }

    public static Map<String, Integer> getAllPlayerKills() {
        Map<String, Integer> playerKills = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            var kills = getKills(player);
            playerKills.put(player.getName(), kills);
        }

        return playerKills;
    }

    public static void incrementDeaths(Player player) {
        var deaths = getDeaths(player);
        player.getPersistentDataContainer().set(deathsKey, PersistentDataType.INTEGER, deaths + 1);
    }

    public static int getDeaths(Player player) {
        return player.getPersistentDataContainer().getOrDefault(deathsKey, PersistentDataType.INTEGER, 0);
    }

    public static Map<String, Integer> getAllPlayerDeaths() {
        Map<String, Integer> playerDeaths = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            var deaths = getDeaths(player);
            playerDeaths.put(player.getName(), deaths);
        }

        return playerDeaths;
    }

    public static void resetPlayerData(Player player) {
        var pdc = player.getPersistentDataContainer();

        for (NamespacedKey key : pdc.getKeys()) {
            if (key.getNamespace().equals(plugin.getName())) {
                pdc.remove(key);
            }
        }
    }
}

