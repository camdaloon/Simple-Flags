package com.camdaloon.simpleflags;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public final class FlagEditor implements InventoryHolder {
    private final SimpleFlags plugin;
    private final FlagService service;
    private final Inventory inventory;
    private final ItemStack flag;
    private final Player player;
    private final UploadListener uploads;
    private int penSize = 1;
    private int color = 0x000000;
    private int[] pixels;

    public FlagEditor(SimpleFlags plugin, FlagService service, ItemStack flag, Player player) {
        this.plugin = plugin;
        this.service = service;
        this.flag = flag;
        this.player = player;
        this.uploads = new UploadListener(plugin, service);
        this.pixels = service.decode(service.getDesign(flag));
        this.inventory = Bukkit.createInventory(this, 54, "Flag Painter");
        build();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void build() {
        renderCanvas();
        inventory.setItem(45, button(Material.FEATHER, "Pen 1"));
        inventory.setItem(46, button(Material.BLAZE_ROD, "Pen 2"));
        inventory.setItem(47, button(Material.STICK, "Pen 4"));
        inventory.setItem(48, button(Material.BAMBOO, "Pen 8"));
        inventory.setItem(49, button(Material.NETHER_STAR, "Color Picker"));
        inventory.setItem(50, uploadButton());
        inventory.setItem(51, button(Material.LIME_DYE, "Save"));
        inventory.setItem(52, button(Material.RED_DYE, "Cancel"));
        inventory.setItem(53, button(Material.WHITE_DYE, "Clear"));
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack uploadButton() {
        ItemStack item = button(Material.ENDER_EYE, "Upload Image");
        ItemMeta meta = item.getItemMeta();
        if (!player.hasPermission("uploadflag")) {
            meta.setLore(java.util.List.of(ChatColor.RED + "Insufficient permissions"));
        } else {
            meta.setLore(java.util.List.of(ChatColor.GRAY + "Import an image from a URL"));
        }
        item.setItemMeta(meta);
        return item;
    }

    private void renderCanvas() {
        for (int i = 0; i < FlagService.CELLS; i++) {
            int rgb = pixels[i];
            Material material = closestMaterial(rgb);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + "Pixel " + (i + 1));
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }
    }

    private Material closestMaterial(int rgb) {
        int r = (rgb >> 16) & 255;
        int g = (rgb >> 8) & 255;
        int b = rgb & 255;
        if (r > 220 && g > 220 && b > 220) return Material.WHITE_WOOL;
        if (r < 45 && g < 45 && b < 45) return Material.BLACK_WOOL;
        if (r > 180 && g < 90 && b < 90) return Material.RED_WOOL;
        if (r > 180 && g > 120 && b < 80) return Material.ORANGE_WOOL;
        if (r > 180 && g > 180 && b < 90) return Material.YELLOW_WOOL;
        if (g > 150 && r < 100 && b < 120) return Material.GREEN_WOOL;
        if (b > 150 && r < 120 && g < 160) return Material.BLUE_WOOL;
        if (r > 120 && b > 120 && g < 100) return Material.PURPLE_WOOL;
        if (r > 180 && b > 140 && g > 120) return Material.PINK_WOOL;
        if (r < 100 && g > 130 && b > 130) return Material.CYAN_WOOL;
        if (r > 120 && g > 120 && b > 120) return Material.LIGHT_GRAY_WOOL;
        return Material.GRAY_WOOL;
    }

    public void handle(InventoryClickEvent event) {
        if (event.getClickedInventory() != inventory) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < FlagService.CELLS) {
            paint(slot);
            renderCanvas();
            player.updateInventory();
            return;
        }
        if (slot == 45) penSize = 1;
        if (slot == 46) penSize = 2;
        if (slot == 47) penSize = 4;
        if (slot == 48) penSize = 8;
        if (slot == 49) {
            new ColorPicker(plugin, service, this).open(player);
            return;
        }
        if (slot == 50) {
            if (!player.hasPermission("uploadflag")) {
                player.sendMessage(ChatColor.RED + "Insufficient permissions");
                return;
            }
            uploads.begin(player, this);
            return;
        }
        if (slot == 51) {
            saveToHeldItem(player);
            player.closeInventory();
            return;
        }
        if (slot == 52) {
            player.closeInventory();
            return;
        }
        if (slot == 53) {
            for (int i = 0; i < pixels.length; i++) pixels[i] = 0xFFFFFF;
            renderCanvas();
        }
    }

    private void paint(int slot) {
        int x = slot % FlagService.WIDTH;
        int y = slot / FlagService.WIDTH;
        int radius = Math.max(0, penSize / 2);
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int px = x + dx;
                int py = y + dy;
                if (px >= 0 && px < FlagService.WIDTH && py >= 0 && py < FlagService.HEIGHT) pixels[py * FlagService.WIDTH + px] = color;
            }
        }
    }

    private void saveToHeldItem(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (service.isFlag(held)) service.setDesign(held, service.encode(pixels));
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setDesign(String design) {
        this.pixels = service.decode(design);
    }

    public void refresh() {
        renderCanvas();
        player.updateInventory();
    }

    public void handleDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
