package com.yourname.bible;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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

        // /bible <book> <chapter:verse> or /bible <book> <chapter:start-end>
        // Example: /bible John 3:16 or /bible 1 John 4:15-16 or /bible Song of Solomon 2:1-5
        if (args.length >= 2) {
            // Parse the book name and reference
            ParsedReference parsed = parseReference(args);

            if (parsed == null || parsed.book == null) {
                player.sendMessage(Component.text("Book not found!", NamedTextColor.RED));
                showUsage(player);
                return true;
            }

            String book = parsed.book;
            int chapter = parsed.chapter;
            int startVerse = parsed.startVerse;
            int endVerse = parsed.endVerse;

            // Validate chapter
            if (chapter < 1 || chapter > data.getChapterCount(book)) {
                player.sendMessage(Component.text("Invalid chapter for " + book + ". It has " +
                        data.getChapterCount(book) + " chapters.", NamedTextColor.RED));
                return true;
            }

            // Validate verses
            int maxVerse = data.getVerseCount(book, chapter);
            if (startVerse < 1 || startVerse > maxVerse) {
                player.sendMessage(Component.text("Invalid verse number. " + book + " " + chapter +
                        " has " + maxVerse + " verses.", NamedTextColor.RED));
                return true;
            }

            if (endVerse > maxVerse) {
                endVerse = maxVerse;
            }

            String playerName = player.getName();
            final int finalEndVerse = endVerse;

            // Build reference string
            String reference;
            if (startVerse == endVerse) {
                reference = book + " " + chapter + ":" + startVerse;
            } else {
                reference = book + " " + chapter + ":" + startVerse + "-" + finalEndVerse;
            }

            // Get verses from in-memory data (no async needed - instant!)
            String verseText;
            if (startVerse == endVerse) {
                // Single verse - add superscript number
                String rawVerse = data.getVerse(book, chapter, startVerse);
                if (rawVerse != null) {
                    verseText = data.toBoldSuperscript(startVerse) + rawVerse;
                } else {
                    verseText = null;
                }
            } else {
                // Range of verses
                verseText = data.getVerseRange(book, chapter, startVerse, finalEndVerse);
            }

            if (verseText != null) {
                // Build the message components with bold superscript verse numbers
                Component header = Component.text("📖 ", NamedTextColor.GREEN, TextDecoration.BOLD)
                        .append(Component.text(reference + " KJV", NamedTextColor.GREEN, TextDecoration.BOLD));

                // Parse verse text to make superscript numbers bold
                Component verseComponent = formatVerseWithBoldNumbers(verseText);

                Component footer = Component.text("    - Shared by " + playerName,
                        NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC);

                // Broadcast to all players
                Bukkit.broadcast(header);
                Bukkit.broadcast(verseComponent);
                Bukkit.broadcast(footer);
            } else {
                player.sendMessage(Component.text("Verse not found.", NamedTextColor.RED));
            }

            return true;
        }

        // Show usage
        showUsage(player);
        return true;
    }

    /**
     * Parse the command arguments to extract book, chapter, and verse(s)
     * Handles multi-word book names like "Song of Solomon" and "1 John"
     */
    private ParsedReference parseReference(String[] args) {
        if (args.length < 2) return null;

        // The last argument should contain chapter:verse
        String lastArg = args[args.length - 1];

        // Check if last arg contains a colon (chapter:verse format)
        if (!lastArg.contains(":")) {
            return null;
        }

        // Everything before the last arg is the book name
        StringBuilder bookBuilder = new StringBuilder();
        for (int i = 0; i < args.length - 1; i++) {
            if (i > 0) bookBuilder.append(" ");
            bookBuilder.append(args[i]);
        }
        String bookInput = bookBuilder.toString();

        // Find the book
        String book = data.findBook(bookInput);
        if (book == null) return null;

        // Parse chapter:verse from last argument
        String[] chapterVerse = lastArg.split(":");
        if (chapterVerse.length != 2) return null;

        int chapter;
        try {
            chapter = Integer.parseInt(chapterVerse[0]);
        } catch (NumberFormatException e) {
            return null;
        }

        // Parse verse or verse range
        String versePart = chapterVerse[1];
        int startVerse, endVerse;

        if (versePart.contains("-")) {
            // Range: 15-16
            String[] range = versePart.split("-");
            if (range.length != 2) return null;
            try {
                startVerse = Integer.parseInt(range[0]);
                endVerse = Integer.parseInt(range[1]);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            // Single verse
            try {
                startVerse = Integer.parseInt(versePart);
                endVerse = startVerse;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return new ParsedReference(book, chapter, startVerse, endVerse);
    }

    /**
     * Format verse text with bold superscript numbers
     * Superscript characters are made bold in the output
     */
    private Component formatVerseWithBoldNumbers(String text) {
        TextComponent.Builder builder = Component.text();

        StringBuilder currentText = new StringBuilder();
        StringBuilder superscriptBuffer = new StringBuilder();
        boolean inSuperscript = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (isSuperscriptDigit(c)) {
                // If we have pending normal text, add it
                if (currentText.length() > 0) {
                    builder.append(Component.text(currentText.toString(), NamedTextColor.GRAY));
                    currentText.setLength(0);
                }
                superscriptBuffer.append(c);
                inSuperscript = true;
            } else {
                // If we have pending superscript, add it with bold gold
                if (superscriptBuffer.length() > 0) {
                    builder.append(Component.text(superscriptBuffer.toString(),
                            NamedTextColor.GOLD, TextDecoration.BOLD));
                    superscriptBuffer.setLength(0);
                }
                currentText.append(c);
                inSuperscript = false;
            }
        }

        // Add any remaining text
        if (superscriptBuffer.length() > 0) {
            builder.append(Component.text(superscriptBuffer.toString(),
                    NamedTextColor.GOLD, TextDecoration.BOLD));
        }
        if (currentText.length() > 0) {
            builder.append(Component.text(currentText.toString(), NamedTextColor.GRAY));
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

    private void showUsage(Player player) {
        player.sendMessage(Component.text("Usage:", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("  /bible open", NamedTextColor.GRAY)
                .append(Component.text(" - Open Bible GUI", NamedTextColor.WHITE)));
        player.sendMessage(Component.text("  /bible <book> <chapter:verse>", NamedTextColor.GRAY)
                .append(Component.text(" - Share a verse", NamedTextColor.WHITE)));
    }

    /**
     * Helper class to hold parsed reference data
     */
    private static class ParsedReference {
        final String book;
        final int chapter;
        final int startVerse;
        final int endVerse;

        ParsedReference(String book, int chapter, int startVerse, int endVerse) {
            this.book = book;
            this.chapter = chapter;
            this.startVerse = startVerse;
            this.endVerse = endVerse;
        }
    }
}
