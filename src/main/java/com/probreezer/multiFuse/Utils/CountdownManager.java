package com.probreezer.multiFuse.Utils;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.boss.BarColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CountdownManager {
    private final MultiFuse plugin;
    private final Map<String, BossBarUtils> countdowns;

    public CountdownManager(MultiFuse plugin) {
        this.plugin = plugin;
        this.countdowns = new HashMap<>();
    }

    public static void updateLobbyCountdown(MultiFuse plugin) {
        if (plugin.game.state) return;
        var config = plugin.getConfig();
        var numberOfPlayers = plugin.getServer().getOnlinePlayers().size();
        var countdownManager = plugin.game.countdownManager;
        var countdownConfig = config.getConfigurationSection("Phases.Start");
        countdownManager.createCountdown("start", countdownConfig.getInt("Time"), countdownConfig.getString("Title"), BarColor.valueOf(countdownConfig.getString("Colour")), plugin.game::startGame);

        if (numberOfPlayers > 1) {
            countdownManager.startCountdown("start");
        } else {
            countdownManager.cancelCountdown("start");
        }
    }

    public void createCountdown(String id, int seconds, String title, BarColor color, Runnable onCompleteAction) {
        var countdown = countdowns.get(id);
        if (countdown == null) {
            countdown = new BossBarUtils(plugin, id, seconds, title, color, onCompleteAction);
            countdowns.put(id, countdown);
        }
    }

    public BossBarUtils getCountdown(String id) {
        return countdowns.get(id);
    }

    public void startCountdown(String id) {
        var countdown = countdowns.get(id);
        if (countdown != null && !countdown.isPaused) {
            countdown.start();
        }
    }

    public void cancelCountdown(String id) {
        var countdown = countdowns.get(id);
        if (countdown != null) {
            countdown.cancel();
            countdowns.remove(id);
        }
    }

    public void cancelAllCountdowns() {
        var countdownList = new ArrayList<>(countdowns.values());
        for (var countdown : countdownList) {
            countdown.cancel();
        }
        countdowns.clear();
    }

    public boolean IsCountdown(String id) {
        return countdowns.containsKey(id);
    }
}

