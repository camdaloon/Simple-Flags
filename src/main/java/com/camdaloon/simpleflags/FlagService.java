package com.camdaloon.simpleflags;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class FlagService {
    public static final int WIDTH = 64;
    public static final int HEIGHT = 96;

    public FlagService(SimpleFlags plugin) {
    }

    public ItemStack createFlag(Player player, String name) {
        ItemStack item = new ItemStack(Material.WHITE_BANNER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f" + name);
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(SimpleFlags.flagKey, PersistentDataType.BYTE, (byte) 1);
        data.set(SimpleFlags.creatorKey, PersistentDataType.STRING, player.getName());
        data.set(SimpleFlags.nameKey, PersistentDataType.STRING, name);
        data.set(SimpleFlags.designKey, PersistentDataType.STRING, blankDesign());
        item.setItemMeta(meta);
        return item;
    }

    public boolean isFlag(ItemStack item) {
        if (item == null || item.getType() != Material.WHITE_BANNER || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(SimpleFlags.flagKey, PersistentDataType.BYTE);
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

    public String blankDesign() {
        StringBuilder result = new StringBuilder(WIDTH * HEIGHT * 9);
        for (int i = 0; i < WIDTH * HEIGHT; i++) {
            if (i > 0) result.append(',');
            result.append("FFFFFFFF");
        }
        return result.toString();
    }
}
