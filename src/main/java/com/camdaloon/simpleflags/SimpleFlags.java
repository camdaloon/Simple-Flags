package com.camdaloon.simpleflags;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleFlags extends JavaPlugin {
    public static NamespacedKey flagKey;
    public static NamespacedKey designKey;
    public static NamespacedKey creatorKey;
    public static NamespacedKey nameKey;
    public static NamespacedKey idKey;

    @Override
    public void onEnable() {
        flagKey = new NamespacedKey(this, "flag");
        designKey = new NamespacedKey(this, "design");
        creatorKey = new NamespacedKey(this, "creator");
        nameKey = new NamespacedKey(this, "name");
        idKey = new NamespacedKey(this, "id");
        FlagService service = new FlagService(this);
        getServer().getPluginManager().registerEvents(new FlagListener(this, service), this);
        getServer().getPluginManager().registerEvents(new FlagCraftListener(service), this);
        getServer().getPluginManager().registerEvents(new UploadListener(this, service), this);
        getCommand("simpleflags").setExecutor(new SimpleFlagsCommand(this, service));
        getCommand("simpleflags").setTabCompleter(new SimpleFlagsCommand(this, service));
        registerRecipe();
    }

    private void registerRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "blank_flag"), new ItemStack(Material.WHITE_BANNER));
        recipe.shape("SWW", "SWW");
        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('W', Material.WHITE_WOOL);
        getServer().addRecipe(recipe);
    }
}
