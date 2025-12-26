package com.yourname.bible;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {
    private final BibleGUI gui;
    private final BibleData data;
    
    public GUIListener(BibleGUI gui, BibleData data) {
        this.gui = gui;
        this.data = data;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Component titleComponent = event.getView().title();
        String title = PlainTextComponentSerializer.plainText().serialize(titleComponent);
        
        if (!title.startsWith("Bible")) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        Component displayName = clicked.getItemMeta().displayName();
        if (displayName == null) return;
        
        String itemName = PlainTextComponentSerializer.plainText().serialize(displayName);
        
        // Handle different screens
        if (title.contains("Select a Book")) {
            handleBookSelection(player, title, itemName);
        } else if (title.contains("Select Chapter")) {
            handleChapterSelection(player, itemName);
        } else {
            handleChapterReader(player, itemName);
        }
    }
    
    private void handleBookSelection(Player player, String title, String itemName) {
        if (itemName.equals("Close")) {
            player.closeInventory();
        } else if (itemName.equals("Next Page")) {
            int currentPage = extractPage(title);
            gui.openBookSelection(player, currentPage);
        } else if (itemName.equals("Previous Page")) {
            int currentPage = extractPage(title);
            gui.openBookSelection(player, currentPage - 2);
        } else if (data.getBooks().contains(itemName)) {
            gui.openChapterSelection(player, itemName);
        }
    }
    
    private void handleChapterSelection(Player player, String itemName) {
        if (itemName.equals("Close")) {
            player.closeInventory();
        } else if (itemName.equals("Back to Books")) {
            gui.openBookSelection(player);
        } else if (itemName.startsWith("Chapter ")) {
            String book = gui.getCurrentBook(player);
            int chapter = Integer.parseInt(itemName.replace("Chapter ", ""));
            gui.openChapterReader(player, book, chapter);
        }
    }
    
    private void handleChapterReader(Player player, String itemName) {
        String book = gui.getCurrentBook(player);
        Integer chapter = gui.getCurrentChapter(player);
        
        if (chapter == null) return;
        
        switch (itemName) {
            case "Close":
                player.closeInventory();
                break;
            case "Back to Books":
                gui.openBookSelection(player);
                break;
            case "Back to Chapters":
                gui.openChapterSelection(player, book);
                break;
            case "Previous Chapter":
                gui.openChapterReader(player, book, chapter - 1);
                break;
            case "Next Chapter":
                gui.openChapterReader(player, book, chapter + 1);
                break;
        }
    }
    
    private int extractPage(String title) {
        try {
            int start = title.indexOf("Page ") + 5;
            int end = title.indexOf(")", start);
            return Integer.parseInt(title.substring(start, end));
        } catch (Exception e) {
            return 1;
        }
    }
}