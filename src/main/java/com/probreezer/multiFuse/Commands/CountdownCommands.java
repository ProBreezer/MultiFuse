package com.probreezer.multiFuse.Commands;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static com.probreezer.multiFuse.Utils.Text.ADMINERRORPREFIX;
import static com.probreezer.multiFuse.Utils.Text.ADMINPREFIX;

public class CountdownCommands implements CommandExecutor {
    private final MultiFuse plugin;

    public CountdownCommands(MultiFuse plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var commandName = command.getName().toLowerCase();
        if (args.length != 2) {
            sender.sendMessage(ADMINERRORPREFIX + "Usage: /" + commandName + " <action> <countdown>");
            return false;
        }

        var countdown = plugin.game.countdownManager;
        var countdownAction = args[0].toLowerCase();
        var countdownName = args[1].toLowerCase();

        if (!countdown.IsCountdown(countdownName)) {
            sender.sendMessage(ADMINPREFIX + "Countdown '§3" + countdownName + "§7' not found.");
            return true;
        }

        if (countdownAction.equals("pause")) countdown.getCountdown(countdownName).pause();
        if (countdownAction.equals("resume")) countdown.getCountdown(countdownName).resume();

        sender.sendMessage(ADMINPREFIX + "Countdown §3" + countdownName + "§7 has been §5" + countdownAction + "d§7.");
        return true;
    }
}
