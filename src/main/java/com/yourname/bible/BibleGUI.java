package com.yourname.bible;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibleGUI {
    private final Plugin plugin;
    private final BibleData data;
    private final Map<Player, String> currentBook = new HashMap<>();
    private final Map<Player, Integer> currentChapter = new HashMap<>();
    
    public BibleGUI(Plugin plugin, BibleData data) {
        this.plugin = plugin;
        this.data = data;
    }
    
    public void openBookSelection(Player player) {
        openBookSelection(player, 0);
    }
    
    public void openBookSelection(Player player, int page) {
        List<String> books = data.getBooks();
        int booksPerPage = 45;
        int maxPage = (books.size() - 1) / booksPerPage;
        
        if (page > maxPage) page = maxPage;
        if (page < 0) page = 0;
        
        Inventory inv = Bukkit.createInventory(null, 54, 
            Component.text("Bible - Select a Book (Page " + (page + 1) + ")", NamedTextColor.GOLD));
        
        int startIndex = page * booksPerPage;
        int endIndex = Math.min(startIndex + booksPerPage, books.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String book = books.get(i);
            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(book, NamedTextColor.AQUA, TextDecoration.BOLD));
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(data.getChapterCount(book) + " chapters", NamedTextColor.GRAY));
            lore.add(Component.text("Click to view chapters", NamedTextColor.YELLOW));
            meta.lore(lore);
            
            item.setItemMeta(meta);
            inv.setItem(i - startIndex, item);
        }
        
        // Navigation buttons
        if (page > 0) {
            ItemStack prev = createButton(Material.ARROW, "Previous Page", NamedTextColor.YELLOW);
            inv.setItem(48, prev);
        }
        
        if (page < maxPage) {
            ItemStack next = createButton(Material.ARROW, "Next Page", NamedTextColor.GREEN);
            inv.setItem(50, next);
        }
        
        ItemStack close = createButton(Material.BARRIER, "Close", NamedTextColor.RED);
        inv.setItem(49, close);
        
        player.openInventory(inv);
    }
    
    public void openChapterSelection(Player player, String book) {
        int chapterCount = data.getChapterCount(book);
        int size = Math.min(54, ((chapterCount + 8) / 9) * 9);
        if (size < 9) size = 9;
        
        Inventory inv = Bukkit.createInventory(null, size, 
            Component.text(book + " - Select Chapter", NamedTextColor.GOLD));
        
        currentBook.put(player, book);
        
        int maxSlots = size - 9; // Reserve bottom row for navigation
        for (int i = 1; i <= Math.min(chapterCount, maxSlots); i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("Chapter " + i, NamedTextColor.AQUA, TextDecoration.BOLD));
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Click to read", NamedTextColor.YELLOW));
            meta.lore(lore);
            
            item.setItemMeta(meta);
            inv.setItem(i - 1, item);
        }
        
        // Navigation buttons at bottom
        ItemStack home = createButton(Material.COMPASS, "Back to Books", NamedTextColor.YELLOW);
        inv.setItem(size - 9, home);
        
        ItemStack close = createButton(Material.BARRIER, "Close", NamedTextColor.RED);
        inv.setItem(size - 5, close);
        
        player.openInventory(inv);
    }
    
    public void openChapterReader(Player player, String book, int chapter) {
        currentBook.put(player, book);
        currentChapter.put(player, chapter);
        
        Inventory inv = Bukkit.createInventory(null, 27, 
            Component.text(book + " " + chapter, NamedTextColor.GOLD));
        
        // Show loading message
        ItemStack loading = new ItemStack(Material.BOOK);
        ItemMeta loadingMeta = loading.getItemMeta();
        loadingMeta.displayName(Component.text("Loading chapter...", NamedTextColor.YELLOW));
        loading.setItemMeta(loadingMeta);
        inv.setItem(13, loading);
        
        player.openInventory(inv);
        
        // Fetch chapter text asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String text = data.fetchChapter(book, chapter);
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Create a written book with the chapter text
                ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();
                
                bookMeta.setTitle(book + " " + chapter);
                bookMeta.setAuthor("KJV");
                
                // Split text into pages (256 chars per page)
                List<Component> pages = new ArrayList<>();
                String cleanText = text.replaceAll("\\n+", " ").trim();
                
                int charsPerPage = 256;
                for (int i = 0; i < cleanText.length(); i += charsPerPage) {
                    int end = Math.min(i + charsPerPage, cleanText.length());
                    String pageText = cleanText.substring(i, end);
                    pages.add(Component.text(pageText, NamedTextColor.BLACK));
                }
                
                if (pages.isEmpty()) {
                    pages.add(Component.text("Chapter text unavailable.", NamedTextColor.RED));
                }
                
                bookMeta.pages(pages);
                bookItem.setItemMeta(bookMeta);
                
                inv.setItem(13, bookItem);
                
                // Navigation buttons
                int maxChapter = data.getChapterCount(book);
                
                if (chapter > 1) {
                    ItemStack prev = createButton(Material.ARROW, "Previous Chapter", NamedTextColor.YELLOW);
                    inv.setItem(18, prev);
                }
                
                if (chapter < maxChapter) {
                    ItemStack next = createButton(Material.ARROW, "Next Chapter", NamedTextColor.GREEN);
                    inv.setItem(26, next);
                }
                
                ItemStack home = createButton(Material.COMPASS, "Back to Chapters", NamedTextColor.AQUA);
                inv.setItem(21, home);
                
                ItemStack homeBooks = createButton(Material.BOOKSHELF, "Back to Books", NamedTextColor.YELLOW);
                inv.setItem(22, homeBooks);
                
                ItemStack close = createButton(Material.BARRIER, "Close", NamedTextColor.RED);
                inv.setItem(23, close);
                
                player.updateInventory();
            });
        });
    }
    
    private ItemStack createButton(Material material, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color, TextDecoration.BOLD));
        item.setItemMeta(meta);
        return item;
    }
    
    public Plugin getPlugin() {
        return plugin;
    }
    
    public String getCurrentBook(Player player) {
        return currentBook.get(player);
    }
    
    public Integer getCurrentChapter(Player player) {
        return currentChapter.get(player);
    }
}