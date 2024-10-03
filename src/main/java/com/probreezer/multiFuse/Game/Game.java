package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.InventoryUtils;
import com.probreezer.multiFuse.Utils.ReplacerUtils;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Game {
    private final MultiFuse plugin;
    private final FileConfiguration config;
    public final World world;
    public TeamManager teamManager;
    public PlayerManager playerManager;
    public KitManager kitManager;
    public ReplacerUtils wallManager;
    public boolean state;
    public boolean overtime;

    public Game(MultiFuse plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.world = plugin.getServer().getWorlds().getFirst();
        this.teamManager = new TeamManager(plugin);
        this.playerManager = new PlayerManager(plugin);
        this.kitManager = new KitManager(plugin);
        this.wallManager = new ReplacerUtils(plugin);
        this.state = false;
        this.overtime = false;
    }

    public void startGame() {
        plugin.getLogger().info("Starting Game...");
        world.setPVP(true);
        this.state = true;

        for (var player : Bukkit.getOnlinePlayers()) {
            var gamePlayer = playerManager.getPlayer(player.getUniqueId());
            if (gamePlayer.isPresent()) {
                var gamePlayerDetails = gamePlayer.get();
                plugin.getLogger().info("Setting up player: " + player.getName());
                if (!teamManager.isPlayerInATeam(player.getUniqueId())) {
                    gamePlayerDetails.setTeam(player, teamManager.getTeamWithLowestAmountOfPlayers());
                }
                InventoryUtils.clearInventory(player);
                if (gamePlayerDetails.kit == null) {
                    gamePlayerDetails.setKit(kitManager.defaultKit);
                }
                this.kitManager.applyKit(plugin, gamePlayerDetails.kit, player);
                player.teleport(gamePlayerDetails.spawn);
                player.setGameMode(GameMode.SURVIVAL);
                this.plugin.scoreboard.setPlayerTeam(player, teamManager.getTeamByPlayer(player.getUniqueId()).name);
            }
        }

        removeWall();
    }

    private void removeWall() {
        var corner1 = new Location(Bukkit.getWorld("world"), 10, 1, -70);
        var corner2 = new Location(Bukkit.getWorld("world"), -10, 120, 70);
        var countdownManager = plugin.countdownManager;
        var countdownConfig = config.getConfigurationSection("Phases.Preparation");
        countdownManager.createCountdown("Preparation", countdownConfig.getInt("Time"), countdownConfig.getString("Title"), BarColor.valueOf(countdownConfig.getString("Colour")), () -> {
            this.wallManager.Remover(corner1, corner2);
            endGameCountdown();
        });
        countdownManager.startCountdown("Preparation");
    }

    private void endGameCountdown() {
        var countdownManager = plugin.countdownManager;
        var countdownConfig = config.getConfigurationSection("Phases.Game");
        countdownManager.createCountdown("Game", countdownConfig.getInt("Time"), countdownConfig.getString("Title"), BarColor.valueOf(countdownConfig.getString("Colour")), () -> {
            endGame(teamManager.getTeamWithHighestFuseHealth());
        });
        countdownManager.startCountdown("Game");
    }

    public void endGame(String winningTeam) {
        var isDraw = winningTeam.equalsIgnoreCase("draw");

        if (isDraw && !this.overtime) {
            startOvertime();
            this.overtime = true;
            return;
        }

        plugin.getLogger().info("Ending Game...");

        var endGameMessage = isDraw ? ChatColor.GRAY + "Draw!" : ChatColor.valueOf(winningTeam.toUpperCase()) + winningTeam + " Team Wins!";
            processPlayersEnd(endGameMessage);

        var countdownManager = plugin.countdownManager;
        countdownManager.cancelAllCountdowns();
        world.setPVP(false);
        this.state = false;
        this.wallManager.restoreBlocks();
    }

    private void startOvertime() {
        plugin.getLogger().info("Starting Overtime...");
        var countdownManager = plugin.countdownManager;
        var countdownConfig = config.getConfigurationSection("Phases.Overtime");
        countdownManager.createCountdown("Overtime", countdownConfig.getInt("Time"), countdownConfig.getString("Title"), BarColor.valueOf(countdownConfig.getString("Colour")), () -> {
            endGame(teamManager.getTeamWithHighestFuseHealth());
        });
        countdownManager.startCountdown("Overtime");
    }

    private void processPlayersEnd(String endGameMessage) {
        for (var player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(endGameMessage, "", 10, 70, 20);
            InventoryUtils.clearInventory(player);
            player.teleport(world.getSpawnLocation());
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20f);

            plugin.scoreboard.removePlayer(player);
            playerManager.getPlayer(player.getUniqueId()).ifPresent(gamePlayer -> gamePlayer.resetPlayer(player));
        }
    }
}
