package com.yourname.bible;

import org.bukkit.plugin.java.JavaPlugin;

public class Bible extends JavaPlugin {
    private BibleData bibleData;
    private BibleGUI gui;
    
    @Override
    public void onEnable() {
        bibleData = new BibleData();
        gui = new BibleGUI(this, bibleData);
        
        BibleCommand commandExecutor = new BibleCommand(this, bibleData, gui);
        getCommand("bible").setExecutor(commandExecutor);
        getCommand("bible").setTabCompleter(new BibleTabCompleter(bibleData));
        
        getServer().getPluginManager().registerEvents(gui, this);
        
        getLogger().info("Bible plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Bible plugin disabled!");
    }
}