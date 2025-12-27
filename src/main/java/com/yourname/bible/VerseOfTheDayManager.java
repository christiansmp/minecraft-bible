package com.yourname.bible;

import org.bukkit.plugin.Plugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages the Verse of the Day feature by fetching from BibleGateway's RSS feed
 * and looking up the text in the local KJV Bible data.
 */
public class VerseOfTheDayManager {

    // BibleGateway VOTD RSS feed - version 9 is KJV
    private static final String BIBLEGATEWAY_VOTD_URL =
        "https://www.biblegateway.com/usage/votd/rss/votd.rdf?9";

    // Pattern to extract the verse reference from <title> tag
    private static final Pattern TITLE_PATTERN = Pattern.compile(
        "<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE);

    // Pattern to parse verse reference like "Psalm 103:1-2" or "John 3:16"
    private static final Pattern REFERENCE_PATTERN = Pattern.compile(
        "^([123]?\\s*[A-Za-z]+(?:\\s+[A-Za-z]+)*)\\s+(\\d+):(\\d+)(?:-(\\d+))?$");

    private final Plugin plugin;
    private final BibleData bibleData;
    private final Logger logger;
    private final HttpClient httpClient;

    // Cached verse data
    private LocalDate cachedDate;
    private String cachedReference;
    private String cachedVerseText;

    public VerseOfTheDayManager(Plugin plugin, BibleData bibleData) {
        this.plugin = plugin;
        this.bibleData = bibleData;
        this.logger = plugin.getLogger();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    /**
     * Gets the cached verse of the day, or fetches it if not cached for today.
     */
    public CompletableFuture<VerseData> getVerseOfTheDay() {
        LocalDate today = LocalDate.now();

        // Return cached data if it's from today
        if (cachedDate != null && cachedDate.equals(today) && cachedVerseText != null) {
            return CompletableFuture.completedFuture(
                new VerseData(cachedReference, cachedVerseText)
            );
        }

        // Fetch new verse asynchronously
        return fetchVerseFromBibleGateway()
            .thenApply(verseData -> {
                if (verseData != null) {
                    cachedDate = today;
                    cachedReference = verseData.reference();
                    cachedVerseText = verseData.text();
                }
                return verseData;
            });
    }

    /**
     * Fetches the verse of the day from BibleGateway's RSS feed.
     */
    private CompletableFuture<VerseData> fetchVerseFromBibleGateway() {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BIBLEGATEWAY_VOTD_URL))
            .header("User-Agent", "Mozilla/5.0 (compatible; MinecraftPlugin/1.0)")
            .GET()
            .timeout(Duration.ofSeconds(15))
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() != 200) {
                    logger.warning("Failed to fetch BibleGateway VOTD: HTTP " + response.statusCode());
                    return getFallbackVerse();
                }

                return parseRssFeed(response.body());
            })
            .exceptionally(ex -> {
                logger.warning("Error fetching verse of the day: " + ex.getMessage());
                return getFallbackVerse();
            });
    }

    /**
     * Parses the RSS feed to extract the verse reference and look up the text.
     */
    private VerseData parseRssFeed(String rss) {
        // Find the first <item> block's <title>
        int itemStart = rss.indexOf("<item>");
        if (itemStart == -1) {
            logger.warning("No <item> found in RSS feed");
            return getFallbackVerse();
        }

        String itemContent = rss.substring(itemStart);
        Matcher titleMatcher = TITLE_PATTERN.matcher(itemContent);

        if (!titleMatcher.find()) {
            logger.warning("No <title> found in RSS item");
            return getFallbackVerse();
        }

        String reference = titleMatcher.group(1).trim();
        logger.info("BibleGateway VOTD reference: " + reference);

        // Parse the reference and look up in local Bible data
        Matcher refMatcher = REFERENCE_PATTERN.matcher(reference);
        if (!refMatcher.matches()) {
            logger.warning("Could not parse reference: " + reference);
            return getFallbackVerse();
        }

        String bookName = refMatcher.group(1).trim();
        int chapter = Integer.parseInt(refMatcher.group(2));
        int startVerse = Integer.parseInt(refMatcher.group(3));
        Integer endVerse = refMatcher.group(4) != null ? Integer.parseInt(refMatcher.group(4)) : null;

        // Resolve book name using BibleData's findBook method
        String resolvedBook = bibleData.findBook(bookName);
        if (resolvedBook == null) {
            logger.warning("Book not found: " + bookName);
            return getFallbackVerse();
        }

        // Look up the verse text
        String verseText;
        if (endVerse != null && endVerse > startVerse) {
            verseText = bibleData.getVerseRange(resolvedBook, chapter, startVerse, endVerse);
        } else {
            verseText = bibleData.getVerse(resolvedBook, chapter, startVerse);
        }

        if (verseText == null) {
            logger.warning("Verse not found: " + resolvedBook + " " + chapter + ":" + startVerse);
            return getFallbackVerse();
        }

        // Format the display reference
        String displayRef = resolvedBook + " " + chapter + ":" + startVerse;
        if (endVerse != null && endVerse > startVerse) {
            displayRef += "-" + endVerse;
        }

        return new VerseData(displayRef, verseText);
    }

    /**
     * Returns a fallback verse when the fetch fails.
     */
    private VerseData getFallbackVerse() {
        String text = bibleData.getVerse("John", 3, 16);
        if (text != null) {
            return new VerseData("John 3:16", text);
        }
        return new VerseData("Genesis 1:1",
            "In the beginning God created the heaven and the earth.");
    }

    /**
     * Forces a refresh of the cached verse.
     */
    public void refreshCache() {
        cachedDate = null;
        cachedReference = null;
        cachedVerseText = null;
    }

    /**
     * Record to hold the verse of the day data.
     */
    public record VerseData(String reference, String text) {}
}
