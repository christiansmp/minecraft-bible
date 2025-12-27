package com.yourname.bible;

import org.bukkit.plugin.java.JavaPlugin;

public class Bible extends JavaPlugin {
    private BibleData bibleData;
    private BibleGUI gui;
    private VerseOfTheDayManager verseOfTheDayManager;

    @Override
    public void onEnable() {
        // Initialize Bible data with book metadata
        bibleData = new BibleData();

        // Load KJV Bible JSON into memory for zero-lag access
        getLogger().info("Loading KJV Bible data...");
        bibleData.loadBibleJson(this);

        if (!bibleData.isLoaded()) {
            getLogger().severe("Failed to load Bible data! Plugin may not function correctly.");
        }

        // Initialize GUI
        gui = new BibleGUI(this, bibleData);

        // Initialize Verse of the Day manager
        verseOfTheDayManager = new VerseOfTheDayManager(this, bibleData);

        // Set up command executor and tab completer
        BibleCommand commandExecutor = new BibleCommand(this, bibleData, gui);
        getCommand("bible").setExecutor(commandExecutor);
        getCommand("bible").setTabCompleter(new BibleTabCompleter(bibleData));

        // Register GUI event listener
        getServer().getPluginManager().registerEvents(gui, this);

        // Register player join listener for Verse of the Day
        getServer().getPluginManager().registerEvents(
            new PlayerJoinListener(this, verseOfTheDayManager), this);

        // Pre-fetch today's verse asynchronously on startup
        verseOfTheDayManager.getVerseOfTheDay().thenAccept(verse -> {
            if (verse != null) {
                getLogger().info("Verse of the Day loaded: " + verse.reference());
            }
        });

        getLogger().info("Bible plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Bible plugin disabled!");
    }
}
