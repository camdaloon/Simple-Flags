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
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FlagEditor implements InventoryHolder {
    public static final Map<UUID, String> pendingNames = new ConcurrentHashMap<>();
    public static final Map<UUID, FlagEditor> openEditors = new ConcurrentHashMap<>();
    private final SimpleFlags plugin;
    private final FlagService service;
    private final Inventory inventory;
    private final ItemStack flag;
    private final Player player;
    private final UploadListener uploads;
    private int penSize = 1;
    private int color = 0x000000;
    private int tool = 0;
    private String flagName;
    private int[] pixels;

    public FlagEditor(SimpleFlags plugin, FlagService service, ItemStack flag, Player player) {
        this.plugin = plugin;
        this.service = service;
        this.flag = flag.clone();
        this.player = player;
        this.uploads = new UploadListener(plugin, service);
        this.pixels = service.decode(service.getDesign(flag));
        this.flagName = service.getName(flag);
        this.inventory = Bukkit.createInventory(this, 54, "Flag Painter");
        build();
        openEditors.put(player.getUniqueId(), this);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void build() {
        renderCanvas();
        inventory.setItem(45, button(Material.FEATHER, "Pen Size: " + penSize));
        inventory.setItem(46, button(Material.PAPER, "Tool: " + toolName()));
        inventory.setItem(47, button(Material.HOPPER, "Fill Tool"));
        inventory.setItem(48, button(Material.NAME_TAG, "Flag Name: " + flagName));
        inventory.setItem(49, button(Material.NETHER_STAR, "Color Picker"));
        inventory.setItem(50, uploadButton());
        inventory.setItem(51, button(Material.RED_DYE, "Cancel"));
        inventory.setItem(52, button(Material.LIME_DYE, "Save"));
        inventory.setItem(53, button(Material.WHITE_DYE, "Clear Canvas"));
    }

    private String toolName() {
        return switch (tool) { case 1 -> "Rectangle"; case 2 -> "Circle"; case 3 -> "Triangle"; default -> "Pen"; };
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
        meta.setLore(java.util.List.of(player.hasPermission("uploadflag") ? ChatColor.GRAY + "Import an image from a URL" : ChatColor.RED + "Insufficient permissions"));
        item.setItemMeta(meta);
        return item;
    }

    private void renderCanvas() {
        for (int i = 0; i < FlagService.CELLS; i++) {
            ItemStack item = new ItemStack(closestMaterial(pixels[i]));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + "Canvas pixel " + (i + 1));
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }
        inventory.setItem(45, button(Material.FEATHER, "Pen Size: " + penSize));
        inventory.setItem(46, button(Material.PAPER, "Tool: " + toolName()));
        inventory.setItem(48, button(Material.NAME_TAG, "Flag Name: " + flagName));
    }

    private Material closestMaterial(int rgb) {
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
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
        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        event.setCancelled(true);
        if (event.getClickedInventory() != inventory) return;
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < FlagService.CELLS) {
            paint(slot);
            renderCanvas();
            clicker.updateInventory();
            return;
        }
        switch (slot) {
            case 45 -> { penSize = penSize == 1 ? 2 : penSize == 2 ? 4 : penSize == 4 ? 8 : 1; renderCanvas(); }
            case 46 -> { tool = (tool + 1) % 4; renderCanvas(); }
            case 47 -> { tool = 4; renderCanvas(); }
            case 48 -> { pendingNames.put(player.getUniqueId(), flagName); player.closeInventory(); player.sendMessage(ChatColor.YELLOW + "Type the new flag name in chat, or type cancel."); }
            case 49 -> new ColorPicker(plugin, service, this).open(player);
            case 50 -> { if (!player.hasPermission("uploadflag")) player.sendMessage(ChatColor.RED + "Insufficient permissions"); else uploads.begin(player, this); }
            case 51 -> { openEditors.remove(player.getUniqueId()); player.closeInventory(); }
            case 52 -> { saveToHeldItem(player); openEditors.remove(player.getUniqueId()); player.closeInventory(); player.sendMessage(ChatColor.GREEN + "Flag saved."); }
            case 53 -> { for (int i = 0; i < pixels.length; i++) pixels[i] = 0xFFFFFF; renderCanvas(); }
        }
    }

    private void paint(int slot) {
        int x = slot % FlagService.WIDTH, y = slot / FlagService.WIDTH;
        if (tool == 4) { int target = pixels[slot]; for (int i = 0; i < pixels.length; i++) if (pixels[i] == target) pixels[i] = color; return; }
        int radius = Math.max(0, penSize / 2);
        for (int dy = -radius; dy <= radius; dy++) for (int dx = -radius; dx <= radius; dx++) {
            int px = x + dx, py = y + dy;
            if (px < 0 || px >= FlagService.WIDTH || py < 0 || py >= FlagService.HEIGHT) continue;
            boolean draw = true;
            if (tool == 2) draw = dx * dx + dy * dy <= radius * radius + 1;
            if (tool == 3) draw = dy >= 0 && Math.abs(dx) <= dy;
            if (draw) pixels[py * FlagService.WIDTH + px] = color;
        }
        if (tool == 1) {
            int x0 = Math.max(0, x - penSize), x1 = Math.min(FlagService.WIDTH - 1, x + penSize);
            int y0 = Math.max(0, y - penSize), y1 = Math.min(FlagService.HEIGHT - 1, y + penSize);
            for (int px = x0; px <= x1; px++) { pixels[y0 * FlagService.WIDTH + px] = color; pixels[y1 * FlagService.WIDTH + px] = color; }
            for (int py = y0; py <= y1; py++) { pixels[py * FlagService.WIDTH + x0] = color; pixels[py * FlagService.WIDTH + x1] = color; }
        }
    }

    private void saveToHeldItem(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!service.isFlag(held)) return;
        service.setDesign(held, service.encode(pixels));
        ItemMeta meta = held.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + flagName);
        meta.getPersistentDataContainer().set(SimpleFlags.nameKey, PersistentDataType.STRING, flagName);
        meta.getPersistentDataContainer().set(SimpleFlags.idKey, PersistentDataType.STRING, service.buildId(service.getCreator(held), flagName));
        held.setItemMeta(meta);
    }

    public void setColor(int color) { this.color = color & 0xFFFFFF; }
    public void setDesign(String design) { this.pixels = service.decode(design); }
    public void setFlagName(String name) { if (name != null && !name.isBlank()) this.flagName = name.length() > 48 ? name.substring(0, 48) : name; }
    public void refresh() { renderCanvas(); player.updateInventory(); }

    @Override
    public Inventory getInventory() { return inventory; }
}
