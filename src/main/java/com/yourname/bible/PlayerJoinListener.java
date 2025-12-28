package com.yourname.bible;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener that sends the Verse of the Day to players when they join the server.
 */
public class PlayerJoinListener implements Listener {

    private final Plugin plugin;
    private final VerseOfTheDayManager verseManager;

    // Pattern to match superscript verse numbers (Unicode superscript digits)
    private static final Pattern SUPERSCRIPT_PATTERN = Pattern.compile(
        "([\u2070\u00B9\u00B2\u00B3\u2074\u2075\u2076\u2077\u2078\u2079]+)");

    public PlayerJoinListener(Plugin plugin, VerseOfTheDayManager verseManager) {
        this.plugin = plugin;
        this.verseManager = verseManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Fetch verse asynchronously and send to player
        verseManager.getVerseOfTheDay().thenAccept(verseData -> {
            if (verseData == null) {
                return;
            }

            // Schedule the message with 10 second delay
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }

                // Build the formatted message using Adventure API (native to Paper)
                Component reference = Component.text()
                    .append(Component.text("\uD83D\uDCD6 ", NamedTextColor.GREEN, TextDecoration.BOLD))
                    .append(Component.text(verseData.reference(), NamedTextColor.GREEN, TextDecoration.BOLD))
                    .append(Component.text(" KJV", NamedTextColor.GREEN, TextDecoration.BOLD))
                    .build();

                Component verseText = formatVerseWithBoldNumbers(verseData.text());

                Component footer = Component.text("    - Verse of the Day", NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC);

                player.sendMessage(reference);
                player.sendMessage(verseText);
                player.sendMessage(footer);
            }, 300L); // 15 second delay (20 ticks = 1 second)
        });
    }

    /**
     * Formats verse text with bold superscript verse numbers.
     */
    private Component formatVerseWithBoldNumbers(String text) {
        TextComponent.Builder builder = Component.text();
        Matcher matcher = SUPERSCRIPT_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            // Add text before the superscript
            if (matcher.start() > lastEnd) {
                builder.append(Component.text(text.substring(lastEnd, matcher.start()), NamedTextColor.GRAY));
            }
            // Add the superscript number in bold gold
            builder.append(Component.text(matcher.group(1), NamedTextColor.GOLD, TextDecoration.BOLD));
            lastEnd = matcher.end();
        }

        // Add remaining text
        if (lastEnd < text.length()) {
            builder.append(Component.text(text.substring(lastEnd), NamedTextColor.GRAY));
        }

        return builder.build();
    }
}
