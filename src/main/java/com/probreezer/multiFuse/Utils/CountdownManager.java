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
        var config = plugin.getConfig();
        var numberOfPlayers = plugin.getServer().getOnlinePlayers().size();
        var countdownManager = plugin.countdownManager;
        var countdownConfig = config.getConfigurationSection("Phases.Start");
        countdownManager.createCountdown("Start", countdownConfig.getInt("Time"), countdownConfig.getString("Title"), BarColor.valueOf(countdownConfig.getString("Colour")), plugin.game::startGame);

        if (numberOfPlayers > 1) {
            countdownManager.startCountdown("Start");
        } else {
            countdownManager.cancelCountdown("Start");
        }
    }

    public void createCountdown(String id, int seconds, String title, BarColor color, Runnable onCompleteAction) {
        var countdown = new BossBarUtils(plugin, id, seconds, title, color, onCompleteAction);
        countdowns.put(id, countdown);
    }

    public void startCountdown(String id) {
        var countdown = countdowns.get(id);
        if (countdown != null) {
            countdown.start();
        }
    }

    public void cancelCountdown(String id) {
        var countdown = countdowns.get(id);
        if (countdown != null) {
            countdown.cancel();
        }
    }

    public boolean getIsCancelled(String id) {
        var cancelled = false;
        var countdown = countdowns.get(id);
        if (countdown != null) {
            cancelled = countdown.isCancelled;
        }
        return cancelled;
    }

    public void cancelAllCountdowns() {
        var countdownList = new ArrayList<>(countdowns.values());
        for (BossBarUtils countdown : countdownList) {
            countdown.cancel();
        }
        countdowns.clear();
    }


}

