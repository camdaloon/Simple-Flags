package com.camdaloon.simpleflags;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
        new FlagEditor(plugin, service).open(event.getPlayer(), item);
    }
}
