package com.probreezer.multiFuse.Listeners;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import static org.bukkit.Bukkit.getServer;

public class WorldListener implements Listener {

    private final MultiFuse plugin;

    public WorldListener(MultiFuse plugin) {
        this.plugin = plugin;
        maintainDaytime();
    }

    public void maintainDaytime() {
        getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (var world : Bukkit.getWorlds()) {
                world.setTime(6000);
                world.setStorm(false);
                world.setThundering(false);
            }
        }, 0L, 600L);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        if (event.toThunderState()) {
            event.setCancelled(true);
        }
    }
}
