package com.camdaloon.simpleflags;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class FlagService {
    public static final int WIDTH = 24;
    public static final int HEIGHT = 18;
    private final NamespacedKey typeKey;
    private final NamespacedKey idKey;
    private final NamespacedKey creatorKey;
    private final NamespacedKey nameKey;
    private final NamespacedKey pixelsKey;

    public FlagService(SimpleFlags plugin) {
        typeKey = plugin.key("flag_type");
        idKey = plugin.key("flag_id");
        creatorKey = plugin.key("flag_creator");
        nameKey = plugin.key("flag_name");
        pixelsKey = plugin.key("flag_pixels");
    }

    public ItemStack createBlankFlag(Player creator, String name) {
        ItemStack item = new ItemStack(Material.WHITE_BANNER);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(typeKey, PersistentDataType.BYTE, (byte) 1);
        pdc.set(idKey, PersistentDataType.STRING, createId(creator.getName(), name));
        pdc.set(creatorKey, PersistentDataType.STRING, creator.getName());
        pdc.set(nameKey, PersistentDataType.STRING, name);
        pdc.set(pixelsKey, PersistentDataType.STRING, encode(new int[WIDTH * HEIGHT]));
        meta.setDisplayName(ChatColor.WHITE + name);
        meta.setLore(java.util.List.of(ChatColor.GRAY + "Created by: " + creator.getName(), ChatColor.DARK_GRAY + "Right-click to paint"));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createUnclaimedBlank() {
        ItemStack item = new ItemStack(Material.WHITE_BANNER);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(typeKey, PersistentDataType.BYTE, (byte) 2);
        meta.setDisplayName(ChatColor.WHITE + "Blank Flag");
        meta.setLore(java.util.List.of(ChatColor.GRAY + "Right-click to name and paint"));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isFlag(ItemStack item) {
        if (item == null || item.getType() != Material.WHITE_BANNER || !item.hasItemMeta()) return false;
        Byte value = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    public boolean isUnclaimedBlank(ItemStack item) {
        if (item == null || item.getType() != Material.WHITE_BANNER || !item.hasItemMeta()) return false;
        Byte value = item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.BYTE);
        return value != null && value == 2;
    }

    public String getName(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(nameKey, PersistentDataType.STRING, "Flag");
    }

    public String getCreator(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(creatorKey, PersistentDataType.STRING, "Unknown");
    }

    public String getId(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(idKey, PersistentDataType.STRING, "");
    }

    public int[] getPixels(ItemStack item) {
        String encoded = item.getItemMeta().getPersistentDataContainer().get(pixelsKey, PersistentDataType.STRING);
        if (encoded == null) return new int[WIDTH * HEIGHT];
        return decode(encoded);
    }

    public ItemStack updatePixels(ItemStack item, int[] pixels) {
        ItemStack result = item.clone();
        ItemMeta meta = result.getItemMeta();
        meta.getPersistentDataContainer().set(pixelsKey, PersistentDataType.STRING, encode(pixels));
        result.setItemMeta(meta);
        return result;
    }

    public ItemStack renameAndClaim(ItemStack blank, Player player, String name) {
        ItemStack result = createBlankFlag(player, name);
        return result;
    }

    public ItemStack copyFlag(ItemStack original) {
        ItemStack copy = original.clone();
        ItemMeta meta = copy.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(idKey, PersistentDataType.STRING, createId(getCreator(original), getName(original)));
        meta.setLore(java.util.List.of(ChatColor.GRAY + "Created by: " + getCreator(original), ChatColor.DARK_GRAY + "Right-click to paint"));
        copy.setItemMeta(meta);
        return copy;
    }

    public int colorFromDyes(ItemStack[] dyes) {
        int count = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        for (ItemStack stack : dyes) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            DyeColorInfo info = DyeColorInfo.from(stack.getType());
            if (info == null) continue;
            count++;
            r += info.r();
            g += info.g();
            b += info.b();
        }
        if (count == 0) return 0;
        return 0xFF000000 | ((r / count) << 16) | ((g / count) << 8) | (b / count);
    }

    private String createId(String creator, String name) {
        return sanitize(creator) + "_" + sanitize(name) + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String sanitize(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private String encode(int[] pixels) {
        byte[] data = new byte[pixels.length * 4];
        for (int i = 0; i < pixels.length; i++) {
            int value = pixels[i];
            int p = i * 4;
            data[p] = (byte) (value >> 24);
            data[p + 1] = (byte) (value >> 16);
            data[p + 2] = (byte) (value >> 8);
            data[p + 3] = (byte) value;
        }
        return Base64.getEncoder().encodeToString(data);
    }

    private int[] decode(String encoded) {
        try {
            byte[] data = Base64.getDecoder().decode(encoded.getBytes(StandardCharsets.UTF_8));
            int[] pixels = new int[WIDTH * HEIGHT];
            for (int i = 0; i < pixels.length && i * 4 + 3 < data.length; i++) {
                int p = i * 4;
                pixels[i] = ((data[p] & 255) << 24) | ((data[p + 1] & 255) << 16) | ((data[p + 2] & 255) << 8) | (data[p + 3] & 255);
            }
            return pixels;
        } catch (Exception ignored) {
            return new int[WIDTH * HEIGHT];
        }
    }

    private record DyeColorInfo(int r, int g, int b) {
        static DyeColorInfo from(Material material) {
            return switch (material) {
                case WHITE_DYE -> new DyeColorInfo(255, 255, 255);
                case LIGHT_GRAY_DYE -> new DyeColorInfo(192, 192, 192);
                case GRAY_DYE -> new DyeColorInfo(96, 96, 96);
                case BLACK_DYE -> new DyeColorInfo(24, 24, 24);
                case RED_DYE -> new DyeColorInfo(190, 30, 30);
                case ORANGE_DYE -> new DyeColorInfo(240, 120, 20);
                case YELLOW_DYE -> new DyeColorInfo(245, 220, 30);
                case LIME_DYE -> new DyeColorInfo(110, 220, 40);
                case GREEN_DYE -> new DyeColorInfo(45, 150, 55);
                case CYAN_DYE -> new DyeColorInfo(30, 180, 180);
                case LIGHT_BLUE_DYE -> new DyeColorInfo(70, 180, 235);
                case BLUE_DYE -> new DyeColorInfo(40, 70, 200);
                case PURPLE_DYE -> new DyeColorInfo(125, 50, 185);
                case MAGENTA_DYE -> new DyeColorInfo(215, 55, 190);
                case PINK_DYE -> new DyeColorInfo(245, 125, 170);
                case BROWN_DYE -> new DyeColorInfo(120, 75, 35);
                default -> null;
            };
        }
    }
}
