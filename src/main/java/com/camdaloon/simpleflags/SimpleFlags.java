package com.camdaloon.simpleflags;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleFlags extends JavaPlugin {
    private FlagService flagService;
    private FlagEditor editor;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        flagService = new FlagService(this);
        editor = new FlagEditor(this, flagService);
        getServer().getPluginManager().registerEvents(new FlagListener(this, flagService, editor), this);
        SimpleFlagsCommand command = new SimpleFlagsCommand(this);
        getCommand("simpleflags").setExecutor(command);
        getCommand("simpleflags").setTabCompleter(command);
        getLogger().info("Simple Flags " + getDescription().getVersion() + " enabled.");
    }

    public FlagService getFlagService() {
        return flagService;
    }

    public FlagEditor getEditor() {
        return editor;
    }

    public NamespacedKey key(String value) {
        return new NamespacedKey(this, value);
    }
}
