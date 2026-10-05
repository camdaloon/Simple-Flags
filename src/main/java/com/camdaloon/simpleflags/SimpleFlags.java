package com.camdaloon.simpleflags;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleFlags extends JavaPlugin {
    public static NamespacedKey flagKey;
    public static NamespacedKey designKey;
    public static NamespacedKey creatorKey;
    public static NamespacedKey nameKey;

    @Override
    public void onEnable() {
        flagKey = new NamespacedKey(this, "flag");
        designKey = new NamespacedKey(this, "design");
        creatorKey = new NamespacedKey(this, "creator");
        nameKey = new NamespacedKey(this, "name");
        FlagService service = new FlagService(this);
        getServer().getPluginManager().registerEvents(new FlagListener(this, service), this);
        getCommand("simpleflags").setExecutor(new SimpleFlagsCommand(this));
    }
}
