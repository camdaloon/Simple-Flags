package com.camdaloon.simpleflags;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public final class SimpleFlagsCommand implements CommandExecutor, TabCompleter {
    private final SimpleFlags plugin;

    public SimpleFlagsCommand(SimpleFlags plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("simpleflags.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission.");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Simple Flags configuration reloaded.");
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "/simpleflags reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("reload");
        return List.of();
    }
}
