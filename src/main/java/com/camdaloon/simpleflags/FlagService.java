package com.camdaloon.simpleflags;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public final class FlagService {
    public static final int WIDTH = 9;
    public static final int HEIGHT = 5;
    public static final int CELLS = WIDTH * HEIGHT;

    public FlagService(SimpleFlags plugin) {
    }

    public ItemStack createBlankFlag(Player player, String name) {
        ItemStack item = new ItemStack(Material.WHITE_BANNER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + name);
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(SimpleFlags.flagKey, PersistentDataType.BYTE, (byte) 1);
        data.set(SimpleFlags.creatorKey, PersistentDataType.STRING, player.getName());
        data.set(SimpleFlags.nameKey, PersistentDataType.STRING, name);
        data.set(SimpleFlags.idKey, PersistentDataType.STRING, buildId(player.getName(), name));
        data.set(SimpleFlags.designKey, PersistentDataType.STRING, blankDesign());
        item.setItemMeta(meta);
        return item;
    }

    public String buildId(String creator, String name) {
        String cleanCreator = creator.replaceAll("[^A-Za-z0-9_-]", "_");
        String cleanName = name.replaceAll("[^A-Za-z0-9_-]", "_");
        return cleanCreator + "_" + cleanName + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public boolean isFlag(ItemStack item) {
        if (item == null || item.getType() != Material.WHITE_BANNER || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(SimpleFlags.flagKey, PersistentDataType.BYTE);
    }

    public boolean isBlank(ItemStack item) {
        if (!isFlag(item)) return false;
        return blankDesign().equals(getDesign(item));
    }

    public String getDesign(ItemStack item) {
        if (!isFlag(item)) return blankDesign();
        String value = item.getItemMeta().getPersistentDataContainer().get(SimpleFlags.designKey, PersistentDataType.STRING);
        return value == null ? blankDesign() : value;
    }

    public void setDesign(ItemStack item, String design) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(SimpleFlags.designKey, PersistentDataType.STRING, design);
        item.setItemMeta(meta);
    }

    public String getCreator(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(SimpleFlags.creatorKey, PersistentDataType.STRING, "");
    }

    public String getName(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(SimpleFlags.nameKey, PersistentDataType.STRING, "Flag");
    }

    public String getId(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(SimpleFlags.idKey, PersistentDataType.STRING, "");
    }

    public String blankDesign() {
        StringBuilder result = new StringBuilder(CELLS * 7);
        for (int i = 0; i < CELLS; i++) {
            if (i > 0) result.append(',');
            result.append("FFFFFF");
        }
        return result.toString();
    }

    public int[] decode(String design) {
        String[] parts = design.split(",", -1);
        int[] result = new int[CELLS];
        for (int i = 0; i < CELLS; i++) {
            try {
                result[i] = i < parts.length ? Integer.parseInt(parts[i], 16) : 0xFFFFFF;
            } catch (NumberFormatException e) {
                result[i] = 0xFFFFFF;
            }
        }
        return result;
    }

    public String encode(int[] pixels) {
        StringBuilder result = new StringBuilder(CELLS * 7);
        for (int i = 0; i < CELLS; i++) {
            if (i > 0) result.append(',');
            result.append(String.format("%06X", pixels[i] & 0xFFFFFF));
        }
        return result.toString();
    }

    public ItemStack copyFlag(ItemStack source) {
        ItemStack copy = source.clone();
        ItemMeta meta = copy.getItemMeta();
        String creator = getCreator(source);
        String name = getName(source);
        meta.getPersistentDataContainer().set(SimpleFlags.idKey, PersistentDataType.STRING, buildId(creator, name));
        copy.setItemMeta(meta);
        return copy;
    }
}
