package com.yourname.bible;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class BibleCommand implements CommandExecutor {
    private final Plugin plugin;
    private final BibleData data;
    private final BibleGUI gui;
    
    public BibleCommand(Plugin plugin, BibleData data, BibleGUI gui) {
        this.plugin = plugin;
        this.data = data;
        this.gui = gui;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
            return true;
        }
        
        if (!player.hasPermission("bible.use")) {
            player.sendMessage(Component.text("You don't have permission!", NamedTextColor.RED));
            return true;
        }
        
        // /bible open
        if (args.length == 1 && args[0].equalsIgnoreCase("open")) {
            gui.openBookSelection(player);
            return true;
        }
        
        // /bible <book> <chapter> <verse>
        if (args.length >= 3) {
            String bookInput = args[0];
            
            // Handle multi-word book names
            StringBuilder bookName = new StringBuilder(bookInput);
            int chapterArgIndex = 1;
            
            // Check if it's a numbered book (1 Samuel, 2 Kings, etc.)
            if (args.length >= 4 && (args[0].equals("1") || args[0].equals("2") || args[0].equals("3"))) {
                bookName.append(" ").append(args[1]);
                chapterArgIndex = 2;
            }
            
            String book = data.findBook(bookName.toString());
            
            if (book == null) {
                player.sendMessage(Component.text("Book not found: " + bookName, NamedTextColor.RED));
                return true;
            }
            
            try {
                int chapter = Integer.parseInt(args[chapterArgIndex]);
                int verse = Integer.parseInt(args[chapterArgIndex + 1]);
                
                if (chapter < 1 || chapter > data.getChapterCount(book)) {
                    player.sendMessage(Component.text("Invalid chapter for " + book, NamedTextColor.RED));
                    return true;
                }
                
                String playerName = player.getName();
                
                // Fetch asynchronously to avoid lag
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    String verseText = data.fetchVerse(book, chapter, verse);
                    String reference = book + " " + chapter + ":" + verse;
                    
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (verseText != null) {
                            // Broadcast to all players
                            Bukkit.broadcast(Component.text("📖 " + reference + " KJV", 
                                NamedTextColor.GOLD, TextDecoration.BOLD));
                            Bukkit.broadcast(Component.text(verseText, NamedTextColor.WHITE));
                            Bukkit.broadcast(Component.text("   - Shared by " + playerName, 
                                NamedTextColor.GRAY, TextDecoration.ITALIC));
                        } else {
                            player.sendMessage(Component.text("Verse not found or error occurred.", 
                                NamedTextColor.RED));
                        }
                    });
                });
                
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid chapter or verse number!", NamedTextColor.RED));
                return true;
            }
        }
        
        // Show usage
        player.sendMessage(Component.text("Usage:", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("  /bible open", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  /bible <book> <chapter> <verse>", NamedTextColor.GRAY));
        return true;
    }
}