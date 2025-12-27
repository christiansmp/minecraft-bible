package com.yourname.bible;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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

    // Player state tracking
    private final Map<Player, String> currentBook = new HashMap<>();
    private final Map<Player, Integer> currentChapter = new HashMap<>();
    private final Map<Player, Integer> bookPage = new HashMap<>();
    private final Map<Player, Integer> chapterPage = new HashMap<>();
    private final Map<Player, Boolean> viewingNewTestament = new HashMap<>();

    private static final int ITEMS_PER_PAGE = 45;

    public BibleGUI(Plugin plugin, BibleData data) {
        this.plugin = plugin;
        this.data = data;
    }

    /**
     * Opens book selection starting with New Testament
     */
    public void openBookSelection(Player player) {
        viewingNewTestament.put(player, true);
        openBookSelectionPage(player, 0);
    }

    /**
     * Opens book selection for Old Testament
     */
    public void openOldTestamentBooks(Player player) {
        viewingNewTestament.put(player, false);
        openBookSelectionPage(player, 0);
    }

    private void openBookSelectionPage(Player player, int page) {
        boolean isNT = viewingNewTestament.getOrDefault(player, true);
        List<String> books = isNT ? data.getNewTestamentBooks() : data.getOldTestamentBooks();

        int maxPage = Math.max(0, (books.size() - 1) / ITEMS_PER_PAGE);
        if (page > maxPage) page = maxPage;
        if (page < 0) page = 0;

        bookPage.put(player, page);

        String title = isNT ? "Bible - New Testament" : "Bible - Old Testament";
        Inventory inv = Bukkit.createInventory(null, 54,
                Component.text(title, NamedTextColor.BLACK));

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, books.size());

        for (int i = startIndex; i < endIndex; i++) {
            String book = books.get(i);
            ItemStack item = new ItemStack(Material.BOOKSHELF);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(book, NamedTextColor.GOLD, TextDecoration.BOLD));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(data.getChapterCount(book) + " chapters", NamedTextColor.GRAY));
            meta.lore(lore);

            item.setItemMeta(meta);
            inv.setItem(i - startIndex, item);
        }

        // Bottom row navigation (slots 45-53)

        // Previous page button (bottom-left, slot 45)
        if (page > 0) {
            inv.setItem(45, createButton(Material.ARROW, "Previous Page", NamedTextColor.YELLOW));
        }

        // Testament toggle button (center, slot 49)
        String toggleLabel = isNT ? "View Old Testament" : "View New Testament";
        inv.setItem(49, createButton(Material.COMPASS, toggleLabel, NamedTextColor.LIGHT_PURPLE));

        // Next page button (bottom-right, slot 53)
        if (page < maxPage) {
            inv.setItem(53, createButton(Material.ARROW, "Next Page", NamedTextColor.GREEN));
        }

        player.openInventory(inv);
    }

    /**
     * Opens chapter selection showing signed books for each chapter
     */
    public void openChapterSelection(Player player, String book) {
        openChapterSelectionPage(player, book, 0);
    }

    private void openChapterSelectionPage(Player player, String book, int page) {
        currentBook.put(player, book);
        chapterPage.put(player, page);

        int chapterCount = data.getChapterCount(book);
        int maxPage = Math.max(0, (chapterCount - 1) / ITEMS_PER_PAGE);

        if (page > maxPage) page = maxPage;
        if (page < 0) page = 0;

        String title = book + " - Chapters";
        Inventory inv = Bukkit.createInventory(null, 54,
                Component.text(title, NamedTextColor.BLACK));

        int startChapter = page * ITEMS_PER_PAGE + 1;
        int endChapter = Math.min(startChapter + ITEMS_PER_PAGE - 1, chapterCount);

        // Add signed books for each chapter
        for (int chapter = startChapter; chapter <= endChapter; chapter++) {
            ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) item.getItemMeta();
            meta.displayName(Component.text("Chapter " + chapter, NamedTextColor.GOLD, TextDecoration.BOLD));
            meta.setTitle(book + " " + chapter);
            meta.setAuthor("KJV");

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Click to read", NamedTextColor.GRAY));
            meta.lore(lore);

            item.setItemMeta(meta);
            inv.setItem(chapter - startChapter, item);
        }

        // Bottom row navigation

        // Previous page button (bottom-left, slot 45)
        if (page > 0) {
            inv.setItem(45, createButton(Material.ARROW, "Previous Page", NamedTextColor.YELLOW));
        }

        // Back to Books button (center, slot 49)
        inv.setItem(49, createButton(Material.COMPASS, "Back to Books", NamedTextColor.LIGHT_PURPLE));

        // Next page button (bottom-right, slot 53)
        if (page < maxPage) {
            inv.setItem(53, createButton(Material.ARROW, "Next Page", NamedTextColor.GREEN));
        }

        player.openInventory(inv);
    }

    /**
     * Opens the chapter content directly in a book
     * Uses in-memory data for instant loading with bold superscript verse numbers
     */
    public void openChapterReader(Player player, String book, int chapter) {
        currentBook.put(player, book);
        currentChapter.put(player, chapter);

        // Get chapter text from in-memory data (instant!)
        String text = data.getChapter(book, chapter);

        // Create a written book with the chapter text
        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();

        bookMeta.setTitle(book + " " + chapter);
        bookMeta.setAuthor("KJV");

        // Format text with bold superscript verse numbers
        List<Component> pages = formatChapterPages(text);

        if (pages.isEmpty()) {
            pages.add(Component.text("Chapter text unavailable.", NamedTextColor.RED));
        }

        bookMeta.pages(pages);
        bookItem.setItemMeta(bookMeta);

        // Open the book directly for the player
        player.openBook(bookItem);
    }

    /**
     * Formats chapter text into book pages with bold superscript verse numbers
     */
    private List<Component> formatChapterPages(String text) {
        List<Component> pages = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return pages;
        }

        // The text already has superscript verse numbers from BibleData.getChapter()
        // We need to split it into pages and make the superscript numbers bold

        // Clean up extra whitespace
        String cleanText = text.replaceAll("\\s+", " ").trim();

        // Split into pages (256 chars per page for Minecraft books, but we use less for readability)
        int charsPerPage = 220;
        int startPos = 0;

        while (startPos < cleanText.length()) {
            int endPos = Math.min(startPos + charsPerPage, cleanText.length());

            // Try to break at a space if not at the end
            if (endPos < cleanText.length()) {
                int lastSpace = cleanText.lastIndexOf(' ', endPos);
                if (lastSpace > startPos) {
                    endPos = lastSpace;
                }
            }

            String pageText = cleanText.substring(startPos, endPos).trim();
            Component pageComponent = formatTextWithBoldSuperscript(pageText);
            pages.add(pageComponent);

            startPos = endPos + 1; // Skip the space
        }

        return pages;
    }

    /**
     * Format text with bold superscript verse numbers
     * Superscript digits get bold decoration, regular text stays normal
     */
    private Component formatTextWithBoldSuperscript(String text) {
        TextComponent.Builder builder = Component.text();

        StringBuilder currentText = new StringBuilder();
        StringBuilder superscriptBuffer = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (isSuperscriptDigit(c)) {
                // If we have pending normal text, add it
                if (currentText.length() > 0) {
                    builder.append(Component.text(currentText.toString(), NamedTextColor.BLACK));
                    currentText.setLength(0);
                }
                superscriptBuffer.append(c);
            } else {
                // If we have pending superscript, add it with bold
                if (superscriptBuffer.length() > 0) {
                    builder.append(Component.text(superscriptBuffer.toString(),
                            NamedTextColor.BLACK, TextDecoration.BOLD));
                    superscriptBuffer.setLength(0);
                }
                currentText.append(c);
            }
        }

        // Add any remaining text
        if (superscriptBuffer.length() > 0) {
            builder.append(Component.text(superscriptBuffer.toString(),
                    NamedTextColor.BLACK, TextDecoration.BOLD));
        }
        if (currentText.length() > 0) {
            builder.append(Component.text(currentText.toString(), NamedTextColor.BLACK));
        }

        return builder.build();
    }

    /**
     * Check if a character is a Unicode superscript digit
     */
    private boolean isSuperscriptDigit(char c) {
        return c == '\u2070' || c == '\u00B9' || c == '\u00B2' || c == '\u00B3' ||
                c == '\u2074' || c == '\u2075' || c == '\u2076' || c == '\u2077' ||
                c == '\u2078' || c == '\u2079';
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
        if (!title.startsWith("Bible") && !title.contains("Chapters")) {
            return;
        }

        event.setCancelled(true); // Prevent item movement

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        Component displayName = clicked.getItemMeta().displayName();
        if (displayName == null) return;

        String itemName = PlainTextComponentSerializer.plainText().serialize(displayName);

        // Handle based on inventory title
        if (title.startsWith("Bible - ")) {
            handleBookSelection(player, clicked, itemName);
        } else if (title.contains("Chapters")) {
            handleChapterSelection(player, clicked, itemName, title);
        }
    }

    private void handleBookSelection(Player player, ItemStack clicked, String itemName) {
        // Handle testament toggle
        if (itemName.equals("View Old Testament")) {
            openOldTestamentBooks(player);
            return;
        }
        if (itemName.equals("View New Testament")) {
            openBookSelection(player);
            return;
        }

        // Handle pagination
        if (itemName.equals("Next Page")) {
            int currentPage = bookPage.getOrDefault(player, 0);
            openBookSelectionPage(player, currentPage + 1);
            return;
        }
        if (itemName.equals("Previous Page")) {
            int currentPage = bookPage.getOrDefault(player, 0);
            openBookSelectionPage(player, currentPage - 1);
            return;
        }

        // Handle book selection (bookshelf items)
        if (clicked.getType() == Material.BOOKSHELF) {
            // Check if it's a valid book name
            List<String> allBooks = data.getBooks();
            if (allBooks.contains(itemName)) {
                openChapterSelection(player, itemName);
            }
        }
    }

    private void handleChapterSelection(Player player, ItemStack clicked, String itemName, String title) {
        // Handle back to books
        if (itemName.equals("Back to Books")) {
            String book = currentBook.get(player);
            if (book != null && data.isNewTestament(book)) {
                openBookSelection(player);
            } else {
                openOldTestamentBooks(player);
            }
            return;
        }

        // Handle pagination
        if (itemName.equals("Next Page")) {
            String book = currentBook.get(player);
            int currentPage = chapterPage.getOrDefault(player, 0);
            if (book != null) {
                openChapterSelectionPage(player, book, currentPage + 1);
            }
            return;
        }
        if (itemName.equals("Previous Page")) {
            String book = currentBook.get(player);
            int currentPage = chapterPage.getOrDefault(player, 0);
            if (book != null) {
                openChapterSelectionPage(player, book, currentPage - 1);
            }
            return;
        }

        // Handle chapter selection (written book items)
        if (clicked.getType() == Material.WRITTEN_BOOK && itemName.startsWith("Chapter ")) {
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
}
