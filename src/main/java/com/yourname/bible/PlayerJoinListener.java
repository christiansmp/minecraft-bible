package com.yourname.bible;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Listener that sends the Verse of the Day to players when they join the server.
 */
public class PlayerJoinListener implements Listener {

    private final Plugin plugin;
    private final VerseOfTheDayManager verseManager;

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

            // Schedule the message to run on the main thread (required for Bukkit)
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }

                // Build the formatted message using Adventure API (native to Paper)
                Component header = Component.text()
                    .append(Component.text("=== ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Verse of the Day", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text(" ===", NamedTextColor.DARK_GRAY))
                    .build();

                Component reference = Component.text()
                    .append(Component.text("\uD83D\uDCD6 ", NamedTextColor.WHITE))
                    .append(Component.text(verseData.reference(), NamedTextColor.GOLD))
                    .append(Component.text(" KJV", NamedTextColor.GRAY))
                    .build();

                Component verseText = Component.text(verseData.text(), NamedTextColor.WHITE);

                player.sendMessage(header);
                player.sendMessage(reference);
                player.sendMessage(verseText);
            });
        });
    }
}
