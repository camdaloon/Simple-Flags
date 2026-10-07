package com.camdaloon.simpleflags;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.net.URI;
import java.util.List;

public final class SimpleFlagsCommand implements CommandExecutor, TabCompleter {
    private final SimpleFlags plugin;
    private final FlagService service;

    public SimpleFlagsCommand(SimpleFlags plugin, FlagService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Simple Flags reloaded.");
            return true;
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("name") && sender instanceof Player player) {
            ItemStack held = player.getInventory().getItemInMainHand();
            if (!service.isFlag(held)) {
                player.sendMessage(ChatColor.RED + "Hold a Simple Flag in your main hand.");
                return true;
            }
            String name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            org.bukkit.inventory.meta.ItemMeta meta = held.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + name);
            meta.getPersistentDataContainer().set(SimpleFlags.nameKey, org.bukkit.persistence.PersistentDataType.STRING, name);
            meta.getPersistentDataContainer().set(SimpleFlags.idKey, org.bukkit.persistence.PersistentDataType.STRING, service.buildId(service.getCreator(held), name));
            held.setItemMeta(meta);
            player.sendMessage(ChatColor.GREEN + "Flag renamed to " + name + ".");
            return true;
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("upload") && sender instanceof Player player) {
            if (!player.hasPermission("uploadflag")) {
                player.sendMessage(ChatColor.RED + "Insufficient permissions");
                return true;
            }
            try {
                URI uri = URI.create(args[1]);
                if (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https")) throw new IllegalArgumentException();
                player.sendMessage(ChatColor.GREEN + "Image URL accepted: " + uri);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Invalid image URL");
            }
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "/simpleflags reload");
        sender.sendMessage(ChatColor.YELLOW + "/simpleflags name <name>");
        sender.sendMessage(ChatColor.YELLOW + "/simpleflags upload <direct-image-url>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("reload", "name", "upload");
        return List.of();
    }
}
