package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.Blocks.BlockManager;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.*;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Game {
    public final World world;
    private final MultiFuse plugin;
    private final FileConfiguration config;
    public ArrayList<String> teams = new ArrayList<>();
    public BlockManager blockManager;
    public FuseManager fuseManager;
    public KitManager kitManager;
    public ReplacerUtils wallManager;
    public ScoreboardUtils scoreboard;
    public CountdownManager countdownManager;
    public boolean state;
    public boolean overtime;

    public Game(MultiFuse plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        var teamConfig = ConfigUtils.getConfig("teams");
        for (var team : teamConfig.getKeys(false)) {
            teams.add(team);
        }

        this.world = plugin.getServer().getWorlds().getFirst();
        this.blockManager = new BlockManager(plugin);
        this.fuseManager = new FuseManager(plugin, teams);
        this.kitManager = new KitManager(plugin);
        this.wallManager = new ReplacerUtils(plugin);
        this.scoreboard = new ScoreboardUtils(plugin);
        this.countdownManager = new CountdownManager(plugin);
        this.state = false;
        this.overtime = false;

        loadGame();
    }

    private void loadGame() {
        this.world.setPVP(false);
        this.world.setDifficulty(Difficulty.EASY);
        this.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        this.kitManager.loadKits();
    }

    public void startGame() {
        plugin.getLogger().info("Starting Game...");
        world.setPVP(true);
        this.state = true;

        for (var player : Bukkit.getOnlinePlayers()) {
            plugin.getLogger().info("Setting up player: " + player.getName());
            InventoryUtils.clearInventory(player);

            //Team
            if (PlayerDataManager.getTeam(player) == null) {
                PlayerDataManager.setTeam(player, PlayerDataManager.getTeamWithLowestAmountOfPlayers());
            }

            //Kit
            var playerKit = PlayerDataManager.getKit(player);
            this.kitManager.applyKit(plugin, playerKit, player);

            player.teleport(PlayerDataManager.getSpawn(player));
            player.setGameMode(GameMode.SURVIVAL);
            this.scoreboard.setPlayerTeam(player, PlayerDataManager.getTeam(player));
        }

        removeWall();
    }

    private void removeWall() {
        var corner1 = new Location(Bukkit.getWorld("world"), 10, 1, -70);
        var corner2 = new Location(Bukkit.getWorld("world"), -10, 120, 70);
        var countdownConfig = config.getConfigurationSection("Phases.Preparation");
        this.countdownManager.createCountdown("preparation", countdownConfig.getInt("Time"), countdownConfig.getString("Title"), BarColor.valueOf(countdownConfig.getString("Colour")), () -> {
            this.wallManager.Remover(corner1, corner2);
            endGameCountdown();
        });
        this.countdownManager.startCountdown("preparation");
    }

    private void endGameCountdown() {
        var countdownConfig = config.getConfigurationSection("Phases.Game");
        this.countdownManager.createCountdown("game", countdownConfig.getInt("Time"), countdownConfig.getString("Title"), BarColor.valueOf(countdownConfig.getString("Colour")), () -> {
            endGame(fuseManager.getTeamWithHighestTotalFuseHealth());
        });
        this.countdownManager.startCountdown("game");
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

        this.countdownManager.cancelAllCountdowns();
        world.setPVP(false);
        this.state = false;
        this.wallManager.restoreBlocks();

        runStats();

        Bukkit.broadcastMessage(Text.PREFIX + "Server is restarting in 20 seconds...");

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Bukkit.broadcastMessage(Text.PREFIX + "Server is restarting now!");
            Bukkit.spigot().restart();
        }, 20L * 20L);
    }

    private void startOvertime() {
        plugin.getLogger().info("Starting Overtime...");
        var countdownConfig = config.getConfigurationSection("Phases.Overtime");
        this.countdownManager.createCountdown("overtime", countdownConfig.getInt("Time"), countdownConfig.getString("Title"), BarColor.valueOf(countdownConfig.getString("Colour")), () -> {
            endGame(fuseManager.getTeamWithHighestTotalFuseHealth());
        });
        this.countdownManager.startCountdown("overtime");
    }

    private void runStats() {
        record Stat(String name, Map<String, Integer> data) {
        }

        List<Stat> stats = List.of(
                new Stat("Kills", PlayerDataManager.getAllPlayerKills()),
                new Stat("Deaths", PlayerDataManager.getAllPlayerDeaths())
        );

        StringBuilder message = new StringBuilder();
        message.append(Text.PREFIX).append("--------------=+=--------------\n");

        for (var stat : stats) {
            Map.Entry<String, Integer> topPlayer = stat.data.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (topPlayer != null) {
                var playerName = topPlayer.getKey();
                var player = Bukkit.getPlayer(playerName);
                var team = PlayerDataManager.getTeam(player);
                var value = topPlayer.getValue();


                message.append(Text.PREFIX)
                        .append(ChatColor.GOLD)
                        .append(stat.name())
                        .append(": ")
                        .append(ChatColor.valueOf(team.toUpperCase()))
                        .append(playerName)
                        .append(" (")
                        .append(value)
                        .append(")\n");
            }
        }

        message.append(Text.PREFIX).append("-------------------------------");

        Bukkit.broadcastMessage(message.toString());
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

            this.scoreboard.removePlayer(player);
            PlayerDataManager.resetPlayerData(player);
        }
    }
}
