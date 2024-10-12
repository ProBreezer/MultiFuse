package com.probreezer.multiFuse.Utils;

import com.probreezer.multiFuse.Game.PlayerDataManager;
import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardUtils {
    private final MultiFuse plugin;
    private final ScoreboardManager manager;
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();

    public ScoreboardUtils(MultiFuse plugin) {
        this.plugin = plugin;
        this.manager = Bukkit.getScoreboardManager();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 0L, 5L);
    }


    public void setPlayerTeam(Player player, String teamColor) {
        var playerScoreboard = playerScoreboards.computeIfAbsent(player.getUniqueId(), k -> Bukkit.getScoreboardManager().getNewScoreboard());
        var team = playerScoreboard.getTeam(teamColor);
        if (team == null) {
            team = playerScoreboard.registerNewTeam(teamColor);
            ChatColor color = ChatColor.valueOf(teamColor.toUpperCase());
            team.setColor(color);
        }

        team.addEntry(player.getName());
        player.setScoreboard(playerScoreboard);

        player.setPlayerListName(Text.getRolePrefix(player) + ChatColor.valueOf(teamColor.toUpperCase()) + team.getPrefix() + player.getName());
        player.setDisplayName(Text.getRolePrefix(player) + ChatColor.valueOf(teamColor.toUpperCase()) + team.getPrefix() + player.getName());

        updateScoreboard(player);
    }


    public void updateScoreboard(Player player) {
        var config = plugin.getConfig();
        boolean debug = config.getBoolean("Debug", false);

        Scoreboard playerScoreboard = playerScoreboards.computeIfAbsent(player.getUniqueId(), k -> Bukkit.getScoreboardManager().getNewScoreboard());
        Objective sidebar = playerScoreboard.getObjective("sidebar");
        if (sidebar == null) {
            sidebar = playerScoreboard.registerNewObjective("sidebar", "dummy", ChatColor.GOLD + "MultiFuse" + (config.getBoolean("Debug", false) ? (ChatColor.DARK_PURPLE + " [DEBUG]") : ""));
            sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        for (String entry : playerScoreboard.getEntries()) {
            playerScoreboard.resetScores(entry);
        }

        int score = 15;

        // Team information
        var teams = plugin.game.teams;
        for (var team : teams) {
            var i = 0;

            sidebar.getScore(ChatColor.valueOf(team.toUpperCase()) + team + " Team").setScore(score--);
            for (var fuse : plugin.game.fuseManager.getFuses(team)) {
                i++;
                sidebar.getScore(ChatColor.valueOf(team.toUpperCase()) + " - " + "Fuse " + i + ": " + fuse.percentageHealth + "%").setScore(score--);
            }

            sidebar.getScore(getBlankLine(score)).setScore(score--);
        }

        // Player information
        sidebar.getScore(ChatColor.GOLD + "Coins: " + ChatColor.GRAY + PlayerDataManager.getCoins(player)).setScore(score--);
        sidebar.getScore(ChatColor.GOLD + "Kills: " + ChatColor.GRAY + PlayerDataManager.getKills(player)).setScore(score--);
        sidebar.getScore(ChatColor.GOLD + "Deaths: " + ChatColor.GRAY + PlayerDataManager.getDeaths(player)).setScore(score--);

        player.setScoreboard(playerScoreboard);
    }


    private String getBlankLine(int score) {
        return ChatColor.RESET.toString() + ChatColor.DARK_GRAY + String.format("%-" + score + "s", "");
    }

    public void removePlayer(Player player) {
        Scoreboard playerScoreboard = playerScoreboards.remove(player.getUniqueId());
        if (playerScoreboard != null) {
            for (Team team : playerScoreboard.getTeams()) {
                team.removeEntry(player.getName());
            }
        }
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void onPlayerJoin(Player player) {
        var playerScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        playerScoreboards.put(player.getUniqueId(), playerScoreboard);
        updateScoreboard(player);
    }

    public void onPlayerQuit(Player player) {
        removePlayer(player);
    }
}