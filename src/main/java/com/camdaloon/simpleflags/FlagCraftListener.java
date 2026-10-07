package com.camdaloon.simpleflags;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public final class FlagCraftListener implements Listener {
    private final FlagService service;

    public FlagCraftListener(FlagService service) {
        this.service = service;
    }

    @EventHandler
    public void prepare(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();
        ItemStack designed = null;
        int flags = 0;
        int blanks = 0;
        for (ItemStack item : matrix) {
            if (!service.isFlag(item)) continue;
            flags++;
            if (service.isBlank(item)) blanks++;
            else designed = item;
        }
        if (flags == 2 && blanks == 1 && designed != null) {
            inventory.setResult(service.copyFlag(designed));
            return;
        }
        if (isBlankRecipe(matrix)) {
            Player player = event.getView().getPlayer() instanceof Player p ? p : null;
            if (player != null) inventory.setResult(service.createBlankFlag(player, "Unnamed Flag"));
        }
    }

    private boolean isBlankRecipe(ItemStack[] matrix) {
        if (matrix.length != 9) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack item = matrix[i];
            boolean top = i == 0 || i == 1 || i == 2;
            boolean second = i == 3 || i == 4 || i == 5;
            boolean expectedStick = i == 0 || i == 3;
            boolean expectedWool = i == 1 || i == 2 || i == 4 || i == 5;
            if (top || second) {
                if (expectedStick && (item == null || item.getType() != org.bukkit.Material.STICK || item.getAmount() < 1)) return false;
                if (expectedWool && (item == null || item.getType() != org.bukkit.Material.WHITE_WOOL || item.getAmount() < 1)) return false;
            } else if (item != null && item.getType() != org.bukkit.Material.AIR) return false;
        }
        return true;
    }

    @EventHandler
    public void craft(CraftItemEvent event) {
        ItemStack result = event.getCurrentItem();
        if (result != null && service.isFlag(result)) return;
        if (event.getWhoClicked() instanceof Player player) {
            ItemStack[] matrix = event.getInventory().getMatrix();
            ItemStack designed = null;
            int flags = 0;
            int blanks = 0;
            for (ItemStack item : matrix) {
                if (!service.isFlag(item)) continue;
                flags++;
                if (service.isBlank(item)) blanks++;
                else designed = item;
            }
            if (flags == 2 && blanks == 1 && designed != null) event.setCurrentItem(service.copyFlag(designed));
            if (isBlankRecipe(matrix)) event.setCurrentItem(service.createBlankFlag(player, "Unnamed Flag"));
        }
    }
}
