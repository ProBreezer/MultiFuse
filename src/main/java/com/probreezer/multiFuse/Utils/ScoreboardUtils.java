package com.probreezer.multiFuse.Utils;

import com.probreezer.multiFuse.Game.PlayerDataManager;
import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardUtils {
    private final MultiFuse plugin;
    private final ScoreboardManager manager;
    private final Scoreboard globalScoreboard;
    private final Objective sidebar;

    public ScoreboardUtils(MultiFuse plugin) {
        this.plugin = plugin;
        this.manager = Bukkit.getScoreboardManager();
        this.globalScoreboard = manager.getNewScoreboard();
        this.sidebar = globalScoreboard.registerNewObjective("sidebar", "dummy", ChatColor.GOLD + "MultiFuse");
        this.sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 0L, 5L);
    }

    public void setPlayerTeam(Player player, String teamColor) {
        var team = globalScoreboard.getTeam(teamColor);
        if (team == null) {
            team = globalScoreboard.registerNewTeam(teamColor);
            var color = ChatColor.valueOf(teamColor.toUpperCase());
            team.setColor(color);
        }

        team.addEntry(player.getName());
        player.setScoreboard(globalScoreboard);

        player.setPlayerListName(Text.getRolePrefix(player) + ChatColor.valueOf(teamColor.toUpperCase()) + team.getPrefix() + player.getName());
        player.setDisplayName(Text.getRolePrefix(player) + ChatColor.valueOf(teamColor.toUpperCase()) + team.getPrefix() + player.getName());

        updateScoreboard(player);
    }

    public void updateScoreboard(Player player) {
        sidebar.getScoreboard().getEntries().forEach(sidebar.getScoreboard()::resetScores);
        var score = 15;

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

        player.setScoreboard(globalScoreboard);
    }

    private String getBlankLine(int score) {
        return ChatColor.RESET.toString() + ChatColor.DARK_GRAY + String.format("%-" + score + "s", "");
    }

    public void removePlayer(Player player) {
        for (Team team : globalScoreboard.getTeams()) {
            team.removeEntry(player.getName());
        }
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
