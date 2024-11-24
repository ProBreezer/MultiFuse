package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.Blocks.BlockManager;
import com.probreezer.multiFuse.DataManagers.PlayerDataManager;
import com.probreezer.multiFuse.Fuse.FuseManager;
import com.probreezer.multiFuse.Kits.KitManager;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.*;
import com.probreezer.untitledNetworkCore.CountdownManagers.CountdownManager;
import com.probreezer.untitledNetworkCore.Managers.SpawnManager;
import com.probreezer.untitledNetworkCore.PrefixManager;
import com.probreezer.untitledNetworkCore.UntitledNetworkCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
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
    public ScoreboardUtils scoreboardManager;
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
        this.fuseManager = new FuseManager(plugin);
        this.kitManager = new KitManager(plugin);
        this.wallManager = new ReplacerUtils(plugin);
        this.countdownManager = new CountdownManager();
        this.scoreboardManager = new ScoreboardUtils(plugin);
        this.state = false;
        this.overtime = false;

        loadGame();
    }

    public static void updateLobbyCountdown(MultiFuse plugin) {
        if (plugin.game.state) return;
        var config = plugin.getConfig();
        var debug = UntitledNetworkCore.isDebug();
        var numberOfPlayers = plugin.getServer().getOnlinePlayers().size();
        var countdownManager = plugin.game.countdownManager;
        var countdownConfig = config.getConfigurationSection("Phases.Start");
        countdownManager.createCountdown("start", countdownConfig.getInt("Time"), countdownConfig.getString("Title"), BarColor.valueOf(countdownConfig.getString("Colour")), plugin.game::startGame);

        if (numberOfPlayers > (debug ? 0 : 1)) {
            countdownManager.startCountdown("start");
        } else {
            countdownManager.cancelCountdown("start");
        }
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
            var playerTeam = PlayerDataManager.getTeam(player);
            if (playerTeam == null) {
                playerTeam = PlayerDataManager.getTeamWithLowestAmountOfPlayers();
                PlayerDataManager.setTeam(player, playerTeam);
            }

            //Kit
            var playerKit = PlayerDataManager.getKit(player);
            this.kitManager.applyKit(playerKit, player);

            player.teleport(SpawnManager.getTeamSpawnLocation(playerTeam));
            player.setGameMode(GameMode.SURVIVAL);
            this.scoreboardManager.setPlayerTeam(player, PlayerDataManager.getTeam(player));
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
        var anyPlayersOnline = Bukkit.getOnlinePlayers()!= null && Bukkit.getOnlinePlayers().size() > 0;
        var isDraw = winningTeam == null;

        if (anyPlayersOnline && isDraw && !this.overtime) {
            startOvertime();
            this.overtime = true;
            return;
        }

        plugin.getLogger().info("Ending Game...");

        var endGameMessage = isDraw ? Component.text("Draw!", NamedTextColor.GRAY) : Component.text(winningTeam + " Team Wins!", NamedTextColor.NAMES.value(winningTeam.toLowerCase()));
        processPlayersEnd(endGameMessage);

        this.countdownManager.cancelAllCountdowns();
        this.scoreboardManager = null;
        world.setPVP(false);
        this.state = false;
        this.wallManager.restoreBlocks();

        runStats();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Bukkit.broadcast(PrefixManager.PREFIX.append(Component.text("Server restarting in 10 seconds...", NamedTextColor.GRAY)));
        }, 5L * 20L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Bukkit.broadcast(PrefixManager.PREFIX.append(Component.text("Server is restarting now!", NamedTextColor.GRAY)));
            Bukkit.spigot().restart();
        }, 15L * 20L);
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

        var message = Component.text();
        message.append(PrefixManager.PREFIX).append(Component.text("--------------=+=--------------\n", NamedTextColor.DARK_GRAY));

        for (var stat : stats) {
            Map.Entry<String, Integer> topPlayer = stat.data.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (topPlayer != null) {
                var playerName = topPlayer.getKey();
                var player = Bukkit.getPlayer(playerName);
                var team = PlayerDataManager.getTeam(player);
                var value = topPlayer.getValue();


                message.append(PrefixManager.PREFIX)
                        .append(Component.text(stat.name(), NamedTextColor.GOLD))
                        .append(Component.text(": ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(playerName + " (" + value + ")\n", NamedTextColor.NAMES.value(team.toLowerCase())));
            }
        }

        message.append(PrefixManager.PREFIX).append(Component.text("-------------------------------", NamedTextColor.DARK_GRAY));

        Bukkit.broadcast(message.build());
    }

    private void processPlayersEnd(TextComponent endGameMessage) {
        var times = Title.Times.times(
                Duration.ofSeconds(1),
                Duration.ofSeconds(4),
                Duration.ofSeconds(1)
        );
        var titleObject = Title.title(endGameMessage, Component.text(""), times);

        for (var player : Bukkit.getOnlinePlayers()) {
            player.showTitle(titleObject);
            InventoryUtils.clearInventory(player);
            player.teleport(world.getSpawnLocation());
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20f);

            this.scoreboardManager.removePlayer(player);
            PlayerDataManager.resetPlayerData(player);
        }
    }
}
