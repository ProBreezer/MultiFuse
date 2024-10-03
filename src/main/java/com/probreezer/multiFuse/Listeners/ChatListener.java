package com.probreezer.multiFuse.Listeners;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final MultiFuse plugin;

    public ChatListener(MultiFuse plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        var player = event.getPlayer();
        var displayName = player.getDisplayName();
        event.setFormat(displayName + ": " + ChatColor.WHITE + "%2$s");
    }
}

