package com.probreezer.multiFuse;

import com.probreezer.multiFuse.Commands.CommandLoader;
import com.probreezer.multiFuse.Game.Game;
import com.probreezer.multiFuse.Game.PlayerDataManager;
import com.probreezer.multiFuse.Listeners.EventLoader;
import com.probreezer.multiFuse.Utils.ConfigUtils;
import com.probreezer.multiFuse.Utils.HologramUtils;
import com.probreezer.multiFuse.Utils.SpawnUtils;
import com.probreezer.multiFuse.Utils.WorldUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class MultiFuse extends JavaPlugin {

    public HologramUtils hologramManager;
    public Game game;

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

        //Load Commands
        CommandLoader.registerCommands(this);

        //Hologram Manager
        this.hologramManager = new HologramUtils(this);

        //Set Default Spawn
        SpawnUtils.setSpawn(this);

        //Set Game
        this.game = new Game(this);

        //Event
        EventLoader.registerEvents(this);

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
