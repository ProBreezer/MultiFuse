package com.probreezer.multiFuse;

import com.probreezer.multiFuse.Game.Game;
import com.probreezer.multiFuse.Game.PlayerDataManager;
import com.probreezer.multiFuse.Listeners.EventLoader;
import com.probreezer.multiFuse.Listeners.WorldListener;
import com.probreezer.multiFuse.Lobby.Spawn;
import com.probreezer.multiFuse.Utils.*;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class MultiFuse extends JavaPlugin {

    public HologramUtils hologramManager;
    public CountdownManager countdownManager;
    public Game game;
    public ScoreboardUtils scoreboard;

    @Override
    public void onLoad() {
        //Copy World
        getLogger().info("Copying World");
        var worldUtils = new WorldUtils(this);
        try {
            worldUtils.replaceWorld();
        } catch (IOException e) {
            getLogger().severe("Failed to replace the world: " + e.getMessage());
            e.printStackTrace();
        }

        getLogger().info("MultiFuse Loaded");
    }

    @Override
    public void onEnable() {
        //Data Manager
        PlayerDataManager.initialise(this);

        //Configuration
        saveDefaultConfig();
        reloadConfig();

        //Custom Configuration
        ConfigUtils.createCustomConfigs(this);

        //Hologram Manager
        this.hologramManager = new HologramUtils(this);

        //Set Default Spawn
        Spawn.setSpawn(this);

        //Keep Daytime
        getServer().getScheduler().runTaskTimer(this, () -> {
            WorldListener.maintainDaytime();
        }, 0L, 600L);

        //Set Game
        this.game = new Game(this);
        this.game.world.setPVP(false);
        this.game.world.setDifficulty(Difficulty.EASY);
        this.game.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        this.game.world.setGameRule(GameRule.DISABLE_RAIDS, true);
        this.game.kitManager.loadKits();

        //Event
        this.scoreboard = new ScoreboardUtils(this);
        EventLoader.registerEvents(this);

        //BossBars
        this.countdownManager = new CountdownManager(this);

        //Plugin Enabled
        getLogger().info("MultiFuse Enabled");
    }

    @Override
    public void onDisable() {
        //Configuration
        saveConfig();

        //Plugin Disabled
        getLogger().info("MultiFuse Disabled");
    }
}
