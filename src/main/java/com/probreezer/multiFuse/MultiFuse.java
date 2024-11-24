package com.probreezer.multiFuse;

import com.probreezer.multiFuse.DataManagers.PlayerDataManager;
import com.probreezer.multiFuse.Game.Game;
import com.probreezer.multiFuse.Listeners.EventLoader;
import com.probreezer.multiFuse.Utils.ConfigUtils;
import com.probreezer.untitledNetworkCore.Managers.HologramManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MultiFuse extends JavaPlugin {
    private static final String PLUGIN_NAME = "MultiFuse";
    private static MultiFuse instance;
    public Game game;
    public HologramManager hologramManager;

    public static MultiFuse getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        getLogger().info(PLUGIN_NAME + " Loaded");
    }

    @Override
    public void onEnable() {
        //Data Manager
        PlayerDataManager.initialise(this);

        //Configuration
        saveDefaultConfig();

        //Custom Configuration
        ConfigUtils.createCustomConfigs(this);

        //Hologram Manager
        this.hologramManager = new HologramManager(this);

        //Set Game
        this.game = new Game(this);

        //Event
        EventLoader.registerEvents(this);

        //Plugin Enabled
        getLogger().info(PLUGIN_NAME + " Enabled");
    }

    @Override
    public void onDisable() {
        //Configuration
        saveConfig();

        //Plugin Disabled
        getLogger().info(PLUGIN_NAME + " Disabled");
    }
}
