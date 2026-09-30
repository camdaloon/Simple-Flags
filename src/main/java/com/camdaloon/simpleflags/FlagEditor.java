package com.camdaloon.simpleflags;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FlagEditor {
    private static final int CANVAS_COLUMNS = 6;
    private static final int CANVAS_ROWS = 6;
    private static final int DYE_START = 49;
    private static final int PREVIEW_SLOT = 53;
    private final SimpleFlags plugin;
    private final FlagService flags;
    private final Map<UUID, Session> sessions = new HashMap<>();

    public FlagEditor(SimpleFlags plugin, FlagService flags) {
        this.plugin = plugin;
        this.flags = flags;
    }

    public void open(Player player, ItemStack flag) {
        sessions.put(player.getUniqueId(), new Session(flag.clone(), flags.getPixels(flag), 1, 0));
        player.openInventory(buildInventory(sessions.get(player.getUniqueId())));
    }

    public boolean isEditor(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof EditorHolder;
    }

    public void click(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Session session = sessions.get(player.getUniqueId());
        if (session == null) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) return;

        if (slot >= DYE_START && slot < PREVIEW_SLOT) {
            if (event.isShiftClick() || event.isDoubleClick()) event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> refresh(player, session));
            return;
        }

        event.setCancelled(true);

        if (slot < CANVAS_COLUMNS * CANVAS_ROWS) {
            paint(player, session, slot);
            refresh(player, session);
            return;
        }

        switch (slot) {
            case 36 -> session.brush = 1;
            case 37 -> session.brush = 2;
            case 38 -> session.brush = 3;
            case 39 -> session.brush = 5;
            case 40 -> session.tool = 0;
            case 41 -> session.tool = 1;
            case 42 -> clear(session);
            case 43 -> save(player, session);
            case 44 -> cancel(player);
            default -> {
            }
        }
        if (sessions.containsKey(player.getUniqueId())) refresh(player, session);
    }

    public void close(Player player) {
        Session session = sessions.remove(player.getUniqueId());
        if (session == null) return;
        player.getInventory().addItem(flags.updatePixels(session.flag, session.pixels));
    }

    private void paint(Player player, Session session, int slot) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        ItemStack[] dyes = new ItemStack[4];
        for (int i = 0; i < 4; i++) dyes[i] = inv.getItem(DYE_START + i);
        int color = flags.colorFromDyes(dyes);
        if (color == 0) return;
        int tileX = slot % CANVAS_COLUMNS;
        int tileY = slot / CANVAS_COLUMNS;
        int pixelX = tileX * 4;
        int pixelY = tileY * 3;
        int radius = Math.max(0, session.brush / 2);
        for (int y = pixelY - radius; y < pixelY + 3 + radius; y++) {
            for (int x = pixelX - radius; x < pixelX + 4 + radius; x++) {
                if (x < 0 || y < 0 || x >= FlagService.WIDTH || y >= FlagService.HEIGHT) continue;
                session.pixels[y * FlagService.WIDTH + x] = session.tool == 1 ? 0 : color;
            }
        }
        consumeDyes(inv, dyes);
    }

    private void consumeDyes(Inventory inv, ItemStack[] dyes) {
        for (int i = 0; i < dyes.length; i++) {
            ItemStack dye = dyes[i];
            if (dye == null || dye.getType() == Material.AIR) continue;
            dye.setAmount(dye.getAmount() - 1);
            inv.setItem(DYE_START + i, dye.getAmount() <= 0 ? null : dye);
        }
    }

    private void clear(Session session) {
        java.util.Arrays.fill(session.pixels, 0);
    }

    private void save(Player player, Session session) {
        ItemStack result = flags.updatePixels(session.flag, session.pixels);
        player.getInventory().addItem(result);
        sessions.remove(player.getUniqueId());
        player.closeInventory();
    }

    private void cancel(Player player) {
        Session session = sessions.remove(player.getUniqueId());
        if (session != null) player.getInventory().addItem(session.flag);
        player.closeInventory();
    }

    private void refresh(Player player, Session session) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        for (int i = 0; i < CANVAS_COLUMNS * CANVAS_ROWS; i++) {
            int tileX = i % CANVAS_COLUMNS;
            int tileY = i / CANVAS_COLUMNS;
            int color = session.pixels[(tileY * 3) * FlagService.WIDTH + tileX * 4];
            inventory.setItem(i, pixelItem(color));
        }
        inventory.setItem(36, button(Material.FEATHER, "1x1 Brush"));
        inventory.setItem(37, button(Material.STICK, "2x2 Brush"));
        inventory.setItem(38, button(Material.BLAZE_ROD, "3x3 Brush"));
        inventory.setItem(39, button(Material.BONE, "5x5 Brush"));
        inventory.setItem(40, button(Material.PAPER, "Freehand"));
        inventory.setItem(41, button(Material.SPONGE, "Eraser"));
        inventory.setItem(42, button(Material.BARRIER, "Clear"));
        inventory.setItem(43, button(Material.LIME_DYE, "Save"));
        inventory.setItem(44, button(Material.RED_DYE, "Cancel"));
        inventory.setItem(PREVIEW_SLOT, previewItem(flags.colorFromDyes(new ItemStack[]{
                inventory.getItem(49), inventory.getItem(50), inventory.getItem(51), inventory.getItem(52)
        })));
    }

    private Inventory buildInventory(Session session) {
        Inventory inventory = Bukkit.createInventory(new EditorHolder(), 54, ChatColor.DARK_BLUE + "Flag Canvas");
        for (int i = 0; i < CANVAS_COLUMNS * CANVAS_ROWS; i++) {
            int tileX = i % CANVAS_COLUMNS;
            int tileY = i / CANVAS_COLUMNS;
            int color = session.pixels[(tileY * 3) * FlagService.WIDTH + tileX * 4];
            inventory.setItem(i, pixelItem(color));
        }
        inventory.setItem(36, button(Material.FEATHER, "1x1 Brush"));
        inventory.setItem(37, button(Material.STICK, "2x2 Brush"));
        inventory.setItem(38, button(Material.BLAZE_ROD, "3x3 Brush"));
        inventory.setItem(39, button(Material.BONE, "5x5 Brush"));
        inventory.setItem(40, button(Material.PAPER, "Freehand"));
        inventory.setItem(41, button(Material.SPONGE, "Eraser"));
        inventory.setItem(42, button(Material.BARRIER, "Clear"));
        inventory.setItem(43, button(Material.LIME_DYE, "Save"));
        inventory.setItem(44, button(Material.RED_DYE, "Cancel"));
        inventory.setItem(PREVIEW_SLOT, previewItem(0));
        return inventory;
    }

    private ItemStack pixelItem(int color) {
        ItemStack item = new ItemStack(color == 0 ? Material.WHITE_STAINED_GLASS_PANE : nearestWool(color));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color == 0 ? ChatColor.GRAY + "Blank Canvas" : ChatColor.WHITE + "Paint");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack previewItem(int color) {
        ItemStack item = new ItemStack(color == 0 ? Material.WHITE_STAINED_GLASS_PANE : nearestWool(color));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color == 0 ? ChatColor.GRAY + "Mix a color" : ChatColor.WHITE + "Current Paint Color");
        item.setItemMeta(meta);
        return item;
    }

    private Material nearestWool(int color) {
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        Material best = Material.WHITE_WOOL;
        int distance = Integer.MAX_VALUE;
        Material[] colors = {Material.WHITE_WOOL, Material.LIGHT_GRAY_WOOL, Material.GRAY_WOOL, Material.BLACK_WOOL, Material.RED_WOOL, Material.ORANGE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL, Material.GREEN_WOOL, Material.CYAN_WOOL, Material.LIGHT_BLUE_WOOL, Material.BLUE_WOOL, Material.PURPLE_WOOL, Material.MAGENTA_WOOL, Material.PINK_WOOL, Material.BROWN_WOOL};
        for (Material material : colors) {
            int[] c = woolColor(material);
            int d = (r - c[0]) * (r - c[0]) + (g - c[1]) * (g - c[1]) + (b - c[2]) * (b - c[2]);
            if (d < distance) {
                distance = d;
                best = material;
            }
        }
        return best;
    }

    private int[] woolColor(Material material) {
        return switch (material) {
            case WHITE_WOOL -> new int[]{255,255,255};
            case LIGHT_GRAY_WOOL -> new int[]{190,190,190};
            case GRAY_WOOL -> new int[]{95,95,95};
            case BLACK_WOOL -> new int[]{25,25,25};
            case RED_WOOL -> new int[]{190,35,35};
            case ORANGE_WOOL -> new int[]{235,125,35};
            case YELLOW_WOOL -> new int[]{245,220,45};
            case LIME_WOOL -> new int[]{120,210,55};
            case GREEN_WOOL -> new int[]{55,145,55};
            case CYAN_WOOL -> new int[]{45,170,170};
            case LIGHT_BLUE_WOOL -> new int[]{80,175,220};
            case BLUE_WOOL -> new int[]{55,70,180};
            case PURPLE_WOOL -> new int[]{125,60,175};
            case MAGENTA_WOOL -> new int[]{205,65,180};
            case PINK_WOOL -> new int[]{235,135,165};
            case BROWN_WOOL -> new int[]{120,80,45};
            default -> new int[]{255,255,255};
        };
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + name);
        item.setItemMeta(meta);
        return item;
    }

    private static final class Session {
        private final ItemStack flag;
        private final int[] pixels;
        private int brush;
        private int tool;
        private Session(ItemStack flag, int[] pixels, int brush, int tool) {
            this.flag = flag;
            this.pixels = pixels;
            this.brush = brush;
            this.tool = tool;
        }
    }

    public static final class EditorHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
