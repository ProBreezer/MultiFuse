package com.probreezer.multiFuse.Utils;

import com.probreezer.multiFuse.DataManagers.PlayerDataManager;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.untitledNetworkCore.UntitledNetworkCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
        var color = NamedTextColor.NAMES.value(teamColor.toLowerCase());
        if (team == null) {
            team = playerScoreboard.registerNewTeam(teamColor);
            team.color(color);
        }

        var playerName = player.getName();
        team.addEntry(playerName);
        player.setScoreboard(playerScoreboard);

        if (plugin.game.state) {
            updateScoreboard(player);
        }
    }

    public void updateScoreboard(Player player) {
        var config = plugin.getConfig();
        var debug = UntitledNetworkCore.isDebug();

        var playerScoreboard = playerScoreboards.computeIfAbsent(player.getUniqueId(), k -> Bukkit.getScoreboardManager().getNewScoreboard());
        var sidebar = playerScoreboard.getObjective("sidebar");
        var title = Component.text("MultiFuse")
                .color(NamedTextColor.GOLD)
                .append(Component.text(debug ? " [DEBUG]" : "", NamedTextColor.DARK_PURPLE));

        if (sidebar == null) {
            sidebar = playerScoreboard.registerNewObjective("sidebar", Criteria.DUMMY, title);
            sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            sidebar.displayName(title);
        }

        // Clear existing teams and scores
        for (var entry : playerScoreboard.getEntries()) {
            playerScoreboard.resetScores(entry);
        }
        for (var team : playerScoreboard.getTeams()) {
            team.unregister();
        }

        var score = 15;

        // Team information
        var teams = plugin.game.teams;
        for (var team : teams) {
            var i = 0;

            addEntry(playerScoreboard, sidebar, team + " Team", NamedTextColor.NAMES.value(team.toLowerCase()), score--);

            for (var fuse : plugin.game.fuseManager.getFuses(team)) {
                i++;
                addEntry(playerScoreboard, sidebar, " - Fuse " + i + ": " + fuse.percentageHealth + "%", NamedTextColor.NAMES.value(team.toLowerCase()), score--);
            }

            addEntry(playerScoreboard, sidebar, " ", NamedTextColor.WHITE, score--);
        }

        // Player information
        addEntry(playerScoreboard, sidebar, "Coins: " + PlayerDataManager.getCoins(player), NamedTextColor.GOLD, score--);
        addEntry(playerScoreboard, sidebar, "Kills: " + PlayerDataManager.getKills(player), NamedTextColor.GOLD, score--);
        addEntry(playerScoreboard, sidebar, "Deaths: " + PlayerDataManager.getDeaths(player), NamedTextColor.GOLD, score--);

        player.setScoreboard(playerScoreboard);
    }

    private void addEntry(Scoreboard scoreboard, Objective objective, String text, TextColor color, int score) {
        var team = scoreboard.registerNewTeam("line_" + score);
        team.prefix(Component.text(text, color));

        String entry = getUniqueString(score);
        team.addEntry(entry);

        objective.getScore(entry).setScore(score);

        team.suffix(Component.empty());
    }

    private String getUniqueString(int score) {
        return ChatColor.values()[score % 16] + "" + ChatColor.RESET;
    }


    public void removePlayer(Player player) {
        var playerScoreboard = playerScoreboards.remove(player.getUniqueId());
        if (playerScoreboard != null) {
            for (var team : playerScoreboard.getTeams()) {
                team.removeEntry(player.getName());
            }
        }
        var emptyScoreboard = manager.getNewScoreboard();
        player.setScoreboard(emptyScoreboard);
    }

    public void onPlayerQuit(Player player) {
        removePlayer(player);
    }
}