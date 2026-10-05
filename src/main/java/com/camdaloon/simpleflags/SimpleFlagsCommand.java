package com.camdaloon.simpleflags;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SimpleFlagsCommand implements CommandExecutor {
    private final SimpleFlags plugin;

    public SimpleFlagsCommand(SimpleFlags plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Simple Flags reloaded.");
            return true;
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("color") && sender instanceof Player) {
            sender.sendMessage(ChatColor.YELLOW + "Color selected: " + args[1]);
            return true;
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("upload") && sender instanceof Player) {
            sender.sendMessage(ChatColor.YELLOW + "Image URL received: " + args[1]);
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "/simpleflags reload");
        return true;
    }
}
