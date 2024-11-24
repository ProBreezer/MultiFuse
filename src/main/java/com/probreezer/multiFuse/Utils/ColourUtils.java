package com.probreezer.multiFuse.Utils;

public class ColourUtils {
    public static String getColourCode(String colorName) {
        return switch (colorName.replace(" ", "_").toLowerCase()) {
            case "red" -> "&c";
            case "blue" -> "&9";
            case "green" -> "&a";
            case "yellow" -> "&e";
            case "purple", "magenta" -> "&5";
            case "aqua", "cyan" -> "&b";
            case "white" -> "&f";
            case "black" -> "&0";
            case "gray" -> "&7";
            case "dark_red" -> "&4";
            case "dark_blue" -> "&1";
            case "dark_green" -> "&2";
            case "dark_aqua" -> "&3";
            case "dark_purple" -> "&5";
            case "dark_gray" -> "&8";
            case "gold", "orange" -> "&6";
            default -> "&f";
        };
    }
}
