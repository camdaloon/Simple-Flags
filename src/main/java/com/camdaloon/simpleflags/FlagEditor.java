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

public final class FlagEditor implements InventoryHolder {
    private final SimpleFlags plugin;
    private final FlagService service;
    private final Inventory inventory;
    private final ItemStack flag;
    private int penSize = 1;

    public FlagEditor(SimpleFlags plugin, FlagService service) {
        this.plugin = plugin;
        this.service = service;
        this.flag = null;
        this.inventory = Bukkit.createInventory(this, 54, "Flag Painter");
    }

    private FlagEditor(SimpleFlags plugin, FlagService service, ItemStack flag) {
        this.plugin = plugin;
        this.service = service;
        this.flag = flag.clone();
        this.inventory = Bukkit.createInventory(this, 54, "Flag Painter");
        build();
    }

    public void open(Player player, ItemStack flag) {
        player.openInventory(new FlagEditor(plugin, service, flag).inventory);
    }

    private void build() {
        inventory.setItem(45, button(Material.FEATHER, "Pen 1"));
        inventory.setItem(46, button(Material.BLAZE_ROD, "Pen 2"));
        inventory.setItem(47, button(Material.STICK, "Pen 4"));
        inventory.setItem(48, button(Material.BAMBOO, "Pen 8"));
        inventory.setItem(49, button(Material.NETHER_STAR, "Color Picker"));
        inventory.setItem(50, button(Material.ENDER_EYE, "Upload Image URL"));
        inventory.setItem(51, button(Material.LIME_DYE, "Save"));
        inventory.setItem(52, button(Material.RED_DYE, "Cancel"));
        inventory.setItem(53, button(Material.WHITE_DYE, "Clear"));
        renderCanvas();
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + name);
        item.setItemMeta(meta);
        return item;
    }

    private void renderCanvas() {
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, button(Material.PAPER, "Canvas"));
        }
    }

    public void handle(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != inventory) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot == 45) penSize = 1;
        if (slot == 46) penSize = 2;
        if (slot == 47) penSize = 4;
        if (slot == 48) penSize = 8;
        if (slot == 49) player.sendMessage(ChatColor.YELLOW + "Use /simpleflags color <hex> to select a color.");
        if (slot == 50) player.sendMessage(ChatColor.YELLOW + "Use /simpleflags upload <direct-image-url> to import an image.");
        if (slot == 51) player.closeInventory();
        if (slot == 52) player.closeInventory();
        if (slot == 53) {
            service.setDesign(flag, service.blankDesign());
            renderCanvas();
            player.updateInventory();
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
