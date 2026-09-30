package com.camdaloon.simpleflags;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FlagListener implements Listener {
    private final SimpleFlags plugin;
    private final FlagService flags;
    private final FlagEditor editor;
    private final Map<UUID, ItemStack> naming = new HashMap<>();

    public FlagListener(SimpleFlags plugin, FlagService flags, FlagEditor editor) {
        this.plugin = plugin;
        this.flags = flags;
        this.editor = editor;
        registerRecipes();
    }

    private void registerRecipes() {
        ShapedRecipe recipe = new ShapedRecipe(plugin.key("blank_flag"), flags.createUnclaimedBlank());
        recipe.shape("SWW", "SWW");
        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('W', Material.WHITE_WOOL);
        plugin.getServer().addRecipe(recipe);

        ShapelessRecipe copy = new ShapelessRecipe(plugin.key("flag_copy"), flags.createUnclaimedBlank());
        copy.addIngredient(Material.WHITE_BANNER);
        copy.addIngredient(Material.WHITE_BANNER);
        plugin.getServer().addRecipe(copy);
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        ItemStack flag = null;
        ItemStack blank = null;
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (flags.isFlag(item)) flag = item;
            if (flags.isUnclaimedBlank(item)) blank = item;
        }
        if (flag != null && blank != null) {
            event.getInventory().setResult(flags.copyFlag(flag));
            return;
        }
        if (event.getRecipe() instanceof ShapedRecipe shaped && shaped.getKey().equals(plugin.key("blank_flag"))) {
            Player player = event.getView().getPlayer() instanceof Player p ? p : null;
            if (player != null) event.getInventory().setResult(flags.createBlankFlag(player, "Unnamed Flag"));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (flags.isUnclaimedBlank(item)) {
            event.setCancelled(true);
            naming.put(player.getUniqueId(), item.clone());
            removeOneFromHand(player);
            player.sendMessage(ChatColor.YELLOW + "Type the flag name in chat. Type cancel to stop.");
            return;
        }
        if (!flags.isFlag(item)) return;
        event.setCancelled(true);
        if (event.getClickedBlock() != null && isFence(event.getClickedBlock())) {
            player.sendMessage(ChatColor.GRAY + "Fence rendering is reserved for the next Alpha build.");
            return;
        }
        editor.open(player, item);
        removeOneFromHand(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!naming.containsKey(uuid)) return;
        event.setCancelled(true);
        String message = event.getMessage().trim();
        ItemStack blank = naming.remove(uuid);
        if (message.equalsIgnoreCase("cancel") || message.isBlank()) {
            event.getPlayer().getInventory().addItem(blank);
            event.getPlayer().sendMessage(ChatColor.GRAY + "Flag naming cancelled.");
            return;
        }
        String name = message.length() > 32 ? message.substring(0, 32) : message;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            ItemStack flag = flags.renameAndClaim(blank, event.getPlayer(), name);
            event.getPlayer().getInventory().addItem(flag);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Created flag: " + name);
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (editor.isEditor(event.getView().getTopInventory())) editor.click(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player && editor.isEditor(event.getInventory())) editor.close(player);
    }

    private void removeOneFromHand(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        hand.setAmount(hand.getAmount() - 1);
        player.getInventory().setItemInMainHand(hand.getAmount() <= 0 ? null : hand);
    }

    private boolean isFence(Block block) {
        return block.getType().name().endsWith("_FENCE") || block.getType() == Material.NETHER_BRICK_FENCE;
    }
}
