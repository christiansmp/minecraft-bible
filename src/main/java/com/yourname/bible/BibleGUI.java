package com.yourname.bible;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibleGUI implements Listener {
    private final Plugin plugin;
    private final BibleData data;
    private final Map<Player, String> currentBook = new HashMap<>();
    private final Map<Player, Integer> currentChapter = new HashMap<>();
    private final Map<Player, Integer> bookPage = new HashMap<>();
    
    public BibleGUI(Plugin plugin, BibleData data) {
        this.plugin = plugin;
        this.data = data;
    }
    
    public void openBookSelection(Player player) {
        openBookSelectionPage(player, 0);
    }
    
    private void openBookSelectionPage(Player player, int page) {
        List<String> books = data.getBooks();
        int booksPerPage = 45;
        int maxPage = (books.size() - 1) / booksPerPage;
        
        if (page > maxPage) page = maxPage;
        if (page < 0) page = 0;
        
        bookPage.put(player, page);
        
        Inventory inv = Bukkit.createInventory(null, 54, 
            Component.text("Bible - Books (Page " + (page + 1) + ")", NamedTextColor.GOLD));
        
        int startIndex = page * booksPerPage;
        int endIndex = Math.min(startIndex + booksPerPage, books.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String book = books.get(i);
            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(book, NamedTextColor.AQUA, TextDecoration.BOLD));
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(data.getChapterCount(book) + " chapters", NamedTextColor.GRAY));
            meta.lore(lore);
            
            item.setItemMeta(meta);
            inv.setItem(i - startIndex, item);
        }
        
        // Navigation buttons
        if (page > 0) {
            inv.setItem(48, createButton(Material.ARROW, "◄ Previous Page", NamedTextColor.YELLOW));
        }
        
        if (page < maxPage) {
            inv.setItem(50, createButton(Material.ARROW, "Next Page ►", NamedTextColor.GREEN));
        }
        
        inv.setItem(49, createButton(Material.BARRIER, "✖ Close", NamedTextColor.RED));
        
        player.openInventory(inv);
    }
    
    public void openChapterSelection(Player player, String book) {
        currentBook.put(player, book);
        
        int chapterCount = data.getChapterCount(book);
        int size = 54;
        
        Inventory inv = Bukkit.createInventory(null, size, 
            Component.text(book + " - Chapters", NamedTextColor.GOLD));
        
        // Add chapters (max 45 to leave room for navigation)
        for (int i = 1; i <= Math.min(chapterCount, 45); i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("Chapter " + i, NamedTextColor.AQUA, TextDecoration.BOLD));
            item.setItemMeta(meta);
            inv.setItem(i - 1, item);
        }
        
        // Navigation buttons at bottom
        inv.setItem(45, createButton(Material.COMPASS, "🏠 Back to Books", NamedTextColor.YELLOW));
        inv.setItem(49, createButton(Material.BARRIER, "✖ Close", NamedTextColor.RED));
        
        player.openInventory(inv);
    }
    
    public void openChapterReader(Player player, String book, int chapter) {
        currentBook.put(player, book);
        currentChapter.put(player, chapter);
        
        Inventory inv = Bukkit.createInventory(null, 27, 
            Component.text(book + " " + chapter, NamedTextColor.GOLD));
        
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
                String cleanText = text.replaceAll("\\n+", " ").replaceAll("\\d+", "").trim();
                
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
                    inv.setItem(18, createButton(Material.ARROW, "◄ Previous Chapter", NamedTextColor.YELLOW));
                }
                
                if (chapter < maxChapter) {
                    inv.setItem(26, createButton(Material.ARROW, "Next Chapter ►", NamedTextColor.GREEN));
                }
                
                inv.setItem(21, createButton(Material.WRITABLE_BOOK, "📖 Back to Chapters", NamedTextColor.AQUA));
                inv.setItem(22, createButton(Material.BOOKSHELF, "🏠 Back to Books", NamedTextColor.YELLOW));
                inv.setItem(23, createButton(Material.BARRIER, "✖ Close", NamedTextColor.RED));
                
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
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Component titleComponent = event.getView().title();
        String title = PlainTextComponentSerializer.plainText().serialize(titleComponent);
        
        // Only handle our plugin's inventories
        if (!title.startsWith("Bible") && !title.contains("Chapters") && !isBookChapter(title)) {
            return;
        }
        
        event.setCancelled(true); // Prevent item movement
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        // Check if player clicked on the written book to read it
        if (clicked.getType() == Material.WRITTEN_BOOK && isBookChapter(title)) {
            player.openBook(clicked);
            return;
        }
        
        Component displayName = clicked.getItemMeta().displayName();
        if (displayName == null) return;
        
        String itemName = PlainTextComponentSerializer.plainText().serialize(displayName);
        
        // Handle based on title
        if (title.contains("Bible - Books")) {
            handleBookSelection(player, itemName, title);
        } else if (title.contains("Chapters")) {
            handleChapterSelection(player, itemName);
        } else if (isBookChapter(title)) {
            handleChapterReader(player, itemName);
        }
    }
    
    private void handleBookSelection(Player player, String itemName, String title) {
        if (itemName.equals("✖ Close")) {
            player.closeInventory();
        } else if (itemName.equals("Next Page ►")) {
            int currentPage = bookPage.getOrDefault(player, 0);
            openBookSelectionPage(player, currentPage + 1);
        } else if (itemName.equals("◄ Previous Page")) {
            int currentPage = bookPage.getOrDefault(player, 0);
            openBookSelectionPage(player, currentPage - 1);
        } else {
            // Check if it's a book name
            if (data.getBooks().contains(itemName)) {
                openChapterSelection(player, itemName);
            }
        }
    }
    
    private void handleChapterSelection(Player player, String itemName) {
        if (itemName.equals("✖ Close")) {
            player.closeInventory();
        } else if (itemName.equals("🏠 Back to Books")) {
            openBookSelection(player);
        } else if (itemName.startsWith("Chapter ")) {
            String book = currentBook.get(player);
            if (book != null) {
                try {
                    int chapter = Integer.parseInt(itemName.replace("Chapter ", ""));
                    openChapterReader(player, book, chapter);
                } catch (NumberFormatException e) {
                    // Ignore invalid format
                }
            }
        }
    }
    
    private void handleChapterReader(Player player, String itemName) {
        String book = currentBook.get(player);
        Integer chapter = currentChapter.get(player);
        
        if (book == null || chapter == null) return;
        
        if (itemName.equals("✖ Close")) {
            player.closeInventory();
        } else if (itemName.equals("🏠 Back to Books")) {
            openBookSelection(player);
        } else if (itemName.equals("📖 Back to Chapters")) {
            openChapterSelection(player, book);
        } else if (itemName.equals("◄ Previous Chapter")) {
            openChapterReader(player, book, chapter - 1);
        } else if (itemName.equals("Next Chapter ►")) {
            openChapterReader(player, book, chapter + 1);
        }
    }
    
    private boolean isBookChapter(String title) {
        // Check if title is in format "BookName ChapterNumber"
        for (String book : data.getBooks()) {
            if (title.startsWith(book + " ")) {
                return true;
            }
        }
        return false;
    }
}