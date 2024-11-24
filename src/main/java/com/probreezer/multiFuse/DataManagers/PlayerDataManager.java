package com.probreezer.multiFuse.DataManagers;

import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.ConfigUtils;
import com.probreezer.untitledNetworkCore.Managers.DisplayNameManager;
import com.probreezer.untitledNetworkCore.PrefixManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataManager {
    private static MultiFuse plugin;
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
        teamKey = new NamespacedKey(plugin, "team");
        kitKey = new NamespacedKey(plugin, "kit");
        coinsKey = new NamespacedKey(plugin, "coins");
        killsKey = new NamespacedKey(plugin, "kills");
        deathsKey = new NamespacedKey(plugin, "deaths");
    }

    public static void setTeam(Player player, String team) {
        var currentTeam = getTeam(player);
        var persistentData = player.getPersistentDataContainer();
        TextComponent message;

        if (team == null) {
            team = "Gray";
            persistentData.remove(teamKey);
            if (currentTeam != null) {
                player.sendMessage(PrefixManager.PREFIX.append(
                        Component.text("You have been removed from the ", NamedTextColor.GRAY)
                                .append(Component.text(currentTeam, NamedTextColor.NAMES.value(currentTeam.toLowerCase())))
                                .append(Component.text(" team", NamedTextColor.GRAY))));
            }
        } else {
            var teamColor = NamedTextColor.NAMES.value(team.toLowerCase());
            var newTeamSize = getTeamPlayers(team).size() + 1;
            var otherTeamSize = (currentTeam != null)
                    ? Math.max(0, getTeamPlayers(currentTeam).size() - 1)
                    : getOtherTeamPlayers(team).size();

            var loadBalancingCheck = (newTeamSize - otherTeamSize) <= 1;
            if (loadBalancingCheck) {
                persistentData.set(teamKey, PersistentDataType.STRING, team);

                if (!plugin.game.state) {
                    player.sendMessage(PrefixManager.PREFIX.append(
                            Component.text("You have joined the ", NamedTextColor.GRAY)
                                    .append(Component.text(team, teamColor))
                                    .append(Component.text(" team", NamedTextColor.GRAY))));
                }
            } else {
                player.sendMessage(PrefixManager.PREFIX.append(
                        Component.text("Too many players on the ", NamedTextColor.GRAY)
                                .append(Component.text(team, teamColor))
                                .append(Component.text(" team", NamedTextColor.GRAY))));
                return;
            }
        }

        DisplayNameManager.updatePlayerDisplayName(player, team);
        plugin.game.scoreboardManager.setPlayerTeam(player, team);
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
        var teams = ConfigUtils.getConfig("teams");
        Map<String, Integer> teamCounts = new HashMap<>();

        for (var team : teams.getKeys(false)) {
            teamCounts.put(team, 0);
        }

        for (var player : Bukkit.getOnlinePlayers()) {
            var team = getTeam(player);
            if (team == null) continue;
            teamCounts.merge(team, 1, Integer::sum);
        }

        return teamCounts.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }


    public static Boolean AllTeamPlayersOffline(String team) {
        var teamPlayers = getTeamPlayers(team);
        return teamPlayers.isEmpty();
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

