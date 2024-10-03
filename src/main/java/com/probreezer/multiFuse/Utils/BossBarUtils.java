package com.probreezer.multiFuse.Utils;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class BossBarUtils {
    private final MultiFuse plugin;
    private final BossBar bossBar;
    private final int totalSeconds;
    private final String id;
    private final Runnable onCompleteAction;
    public boolean isCancelled;
    private BukkitTask countdownTask;
    private int timeRemaining;

    public BossBarUtils(MultiFuse plugin, String id, int seconds, String title, BarColor color, Runnable onCompleteAction) {
        this.plugin = plugin;
        this.id = id;
        this.bossBar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        this.totalSeconds = seconds;
        this.timeRemaining = seconds;
        this.isCancelled = false;
        this.onCompleteAction = onCompleteAction;
    }

    public void start() {
        if (countdownTask != null && !countdownTask.isCancelled()) {
            countdownTask.cancel();
        }
        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, this::countdown, 0L, 20L);
        bossBar.setVisible(true);
        showBossBarToAllPlayers();
        updateBossBar();
    }

    public void cancel() {
        isCancelled = true;
    }

    private void countdown() {
        if (plugin.countdownManager.getIsCancelled(id) || timeRemaining <= 0) {
            stop();
            if (timeRemaining <= 0) {
                plugin.getLogger().info("Countdown finished for " + id + ". Performing actions.");
                performEndAction();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                }
            }
            return;
        }

        if (timeRemaining <= 5) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
            }
        }

        timeRemaining--;
        updateBossBar();
    }

    private void stop() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        bossBar.setVisible(false);
        hideBossBarFromAllPlayers();
        updateBossBar();
    }

    private void performEndAction() {
        if (onCompleteAction != null) {
            try {
                onCompleteAction.run();
            } catch (Exception e) {
                plugin.getLogger().warning("Error occurred while executing onCompleteAction: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateBossBar() {
        var progress = (float) timeRemaining / totalSeconds;
        bossBar.setProgress(progress);

        String timeDisplay;
        if (timeRemaining > 60) {
            int minutes = timeRemaining / 60;
            int seconds = timeRemaining % 60;
            timeDisplay = String.format("%d minute%s %02d second%s",
                    minutes, (minutes != 1 ? "s" : ""),
                    seconds, (seconds != 1 ? "s" : ""));
        } else {
            timeDisplay = timeRemaining + " second" + (timeRemaining > 1 ? "s" : "");
        }

        bossBar.setTitle(bossBar.getTitle().split(":")[0] + ": " + timeDisplay + " remaining");
    }


    private void showBossBarToAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
    }

    private void hideBossBarFromAllPlayers() {
        bossBar.removeAll();
    }
}
