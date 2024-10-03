package com.probreezer.multiFuse.Utils;

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

        Bukkit.getScheduler().runTaskTimer(plugin, this::updateScoreboard, 0L, 5L);
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

        player.setPlayerListName(team.getPrefix() + player.getName());
        player.setDisplayName(team.getPrefix() + player.getName() + ChatColor.RESET);
    }

    public void updateScoreboard() {
        sidebar.getScoreboard().getEntries().forEach(sidebar.getScoreboard()::resetScores);
        var teams = plugin.game.teamManager.teams;
        var score = 12;

        var count = 0;
        var size = teams.size();
        for (var team : teams.values()) {
            count++;
            var i = 0;

            sidebar.getScore(ChatColor.valueOf(team.name.toUpperCase()) + team.name + " Team").setScore(score--);
            for (var fuse : team.fuseManager.getFuses()) {
                i++;
                sidebar.getScore(ChatColor.valueOf(team.name.toUpperCase()) + " - " + "Fuse " + i + ": " + fuse.percentageHealth + "%").setScore(score--);
            }

            if (count != size) sidebar.getScore(" ");
        }

        Bukkit.getOnlinePlayers().forEach(player -> player.setScoreboard(globalScoreboard));
    }

    public void removePlayer(Player player) {
        for (Team team : globalScoreboard.getTeams()) {
            team.removeEntry(player.getName());
        }
    }
}
