package com.probreezer.multiFuse.Utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Text {
    public static final String PREFIX = "§8[§6MultiFuse§8]§7 ";
    public static final String ERRORPREFIX = "§8[§6MultiFuse§8]§c ";
    public static final String ADMINPREFIX = "§8[§6MultiFuse §4Admin§8]§7 ";
    public static final String ADMINERRORPREFIX = "§8[§6MultiFuse §4Admin§8]§c ";

    private static String getRole(Player player) {
        var roleConfig = ConfigUtils.getConfig("roles");
        for (var p : roleConfig.getKeys(false)) {
            if (p.equalsIgnoreCase(player.getName())) {
                return roleConfig.getString(p);
            }
        }
        return null;
    }

    public static String getRolePrefix(Player player) {
        var rolePrefix = getRole(player) != null ? ChatColor.DARK_PURPLE + "[" + getRole(player) + "] " : "";
        return rolePrefix;
    }
}