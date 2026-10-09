package com.camdaloon.simpleflags;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class FlagListener implements Listener {
    private final SimpleFlags plugin;
    private final FlagService service;

    public FlagListener(SimpleFlags plugin, FlagService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (!service.isFlag(item)) return;
        event.setCancelled(true);
        new FlagEditor(plugin, service, item, event.getPlayer()).open(event.getPlayer());
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof FlagEditor editor) {
            editor.handle(event);
            return;
        }
        if (event.getView().getTopInventory().getHolder() instanceof ColorPicker picker) picker.handle(event);
    }

    @EventHandler
    public void inventoryDrag(InventoryDragEvent event) {
        Object holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof FlagEditor || holder instanceof ColorPicker) event.setCancelled(true);
    }

    @EventHandler
    public void renameChat(AsyncPlayerChatEvent event) {
        String current = FlagEditor.pendingNames.remove(event.getPlayer().getUniqueId());
        if (current == null) return;
        event.setCancelled(true);
        String requested = event.getMessage().trim();
        if (requested.equalsIgnoreCase("cancel")) {
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Flag naming cancelled.");
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            FlagEditor editor = FlagEditor.openEditors.get(event.getPlayer().getUniqueId());
            if (editor != null) {
                editor.setFlagName(requested);
                editor.refresh();
                event.getPlayer().sendMessage(ChatColor.GREEN + "Flag name set to " + requested + ". Click Save to keep it.");
            }
        });
    }
}
