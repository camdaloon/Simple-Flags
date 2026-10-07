package com.camdaloon.simpleflags;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.Color;

public final class ColorPicker implements InventoryHolder {
    private final SimpleFlags plugin;
    private final FlagService service;
    private final FlagEditor editor;
    private final Inventory inventory;

    public ColorPicker(SimpleFlags plugin, FlagService service, FlagEditor editor) {
        this.plugin = plugin;
        this.service = service;
        this.editor = editor;
        this.inventory = Bukkit.createInventory(this, 54, "Color Picker");
        build();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void build() {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                float hue = col / 9.0f;
                float saturation = 1.0f - row * 0.2f;
                int rgb = Color.HSBtoRGB(hue, saturation, 1.0f);
                ItemStack item = new ItemStack(materialFor(rgb));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.WHITE + String.format("#%06X", rgb & 0xFFFFFF));
                item.setItemMeta(meta);
                inventory.setItem(row * 9 + col, item);
            }
        }
        for (int i = 45; i < 54; i++) inventory.setItem(i, button(Material.GRAY_STAINED_GLASS_PANE, "Shade"));
        inventory.setItem(49, button(Material.BARRIER, "Back"));
    }

    private Material materialFor(int rgb) {
        int r = (rgb >> 16) & 255;
        int g = (rgb >> 8) & 255;
        int b = rgb & 255;
        if (r > 220 && g > 220 && b > 220) return Material.WHITE_STAINED_GLASS;
        if (r > g * 1.4 && r > b * 1.4) return Material.RED_STAINED_GLASS;
        if (g > r * 1.4 && g > b * 1.4) return Material.LIME_STAINED_GLASS;
        if (b > r * 1.4 && b > g * 1.4) return Material.BLUE_STAINED_GLASS;
        if (r > 180 && g > 120 && b < 80) return Material.ORANGE_STAINED_GLASS;
        if (r > 140 && b > 120 && g < 100) return Material.PURPLE_STAINED_GLASS;
        if (g > 120 && b > 120) return Material.CYAN_STAINED_GLASS;
        return Material.PINK_STAINED_GLASS;
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + name);
        item.setItemMeta(meta);
        return item;
    }

    public void handle(InventoryClickEvent event) {
        if (event.getClickedInventory() != inventory) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot == 49) {
            player.openInventory(editor.getInventory());
            return;
        }
        if (slot >= 0 && slot < 45) {
            int row = slot / 9;
            int col = slot % 9;
            int rgb = Color.HSBtoRGB(col / 9.0f, 1.0f - row * 0.2f, 1.0f) & 0xFFFFFF;
            editor.setColor(rgb);
            player.openInventory(editor.getInventory());
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
