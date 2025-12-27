package com.yourname.bible;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.Plugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class BibleData {
    private final Map<String, Integer> books = new LinkedHashMap<>();
    private final Map<String, String> bookAbbreviations = new HashMap<>();
    private final List<String> oldTestamentBooks = new ArrayList<>();
    private final List<String> newTestamentBooks = new ArrayList<>();

    // In-memory Bible data: Book -> Chapter -> Verse -> Text
    private Map<String, Map<String, Map<String, String>>> bibleData;
    private Logger logger;

    public BibleData() {
        initializeBooks();
    }

    /**
     * Load the KJV Bible JSON into memory
     */
    public void loadBibleJson(Plugin plugin) {
        this.logger = plugin.getLogger();
        try {
            InputStream inputStream = plugin.getResource("KJV_bible.json");
            if (inputStream == null) {
                logger.severe("Could not find KJV_bible.json in resources!");
                return;
            }

            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, Map<String, Map<String, String>>>>(){}.getType();
            bibleData = new Gson().fromJson(reader, type);
            reader.close();

            logger.info("Loaded KJV Bible data: " + bibleData.size() + " books");
        } catch (Exception e) {
            logger.severe("Failed to load KJV_bible.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if Bible data is loaded
     */
    public boolean isLoaded() {
        return bibleData != null && !bibleData.isEmpty();
    }

    private void initializeBooks() {
        // Old Testament
        addOTBook("Genesis", 50);
        addOTBook("Exodus", 40);
        addOTBook("Leviticus", 27);
        addOTBook("Numbers", 36);
        addOTBook("Deuteronomy", 34);
        addOTBook("Joshua", 24);
        addOTBook("Judges", 21);
        addOTBook("Ruth", 4);
        addOTBook("1 Samuel", 31);
        addOTBook("2 Samuel", 24);
        addOTBook("1 Kings", 22);
        addOTBook("2 Kings", 25);
        addOTBook("1 Chronicles", 29);
        addOTBook("2 Chronicles", 36);
        addOTBook("Ezra", 10);
        addOTBook("Nehemiah", 13);
        addOTBook("Esther", 10);
        addOTBook("Job", 42);
        addOTBook("Psalm", 150);
        addOTBook("Proverbs", 31);
        addOTBook("Ecclesiastes", 12);
        addOTBook("Song Of Solomon", 8);
        addOTBook("Isaiah", 66);
        addOTBook("Jeremiah", 52);
        addOTBook("Lamentations", 5);
        addOTBook("Ezekiel", 48);
        addOTBook("Daniel", 12);
        addOTBook("Hosea", 14);
        addOTBook("Joel", 3);
        addOTBook("Amos", 9);
        addOTBook("Obadiah", 1);
        addOTBook("Jonah", 4);
        addOTBook("Micah", 7);
        addOTBook("Nahum", 3);
        addOTBook("Habakkuk", 3);
        addOTBook("Zephaniah", 3);
        addOTBook("Haggai", 2);
        addOTBook("Zechariah", 14);
        addOTBook("Malachi", 4);

        // New Testament
        addNTBook("Matthew", 28);
        addNTBook("Mark", 16);
        addNTBook("Luke", 24);
        addNTBook("John", 21);
        addNTBook("Acts", 28);
        addNTBook("Romans", 16);
        addNTBook("1 Corinthians", 16);
        addNTBook("2 Corinthians", 13);
        addNTBook("Galatians", 6);
        addNTBook("Ephesians", 6);
        addNTBook("Philippians", 4);
        addNTBook("Colossians", 4);
        addNTBook("1 Thessalonians", 5);
        addNTBook("2 Thessalonians", 3);
        addNTBook("1 Timothy", 6);
        addNTBook("2 Timothy", 4);
        addNTBook("Titus", 3);
        addNTBook("Philemon", 1);
        addNTBook("Hebrews", 13);
        addNTBook("James", 5);
        addNTBook("1 Peter", 5);
        addNTBook("2 Peter", 3);
        addNTBook("1 John", 5);
        addNTBook("2 John", 1);
        addNTBook("3 John", 1);
        addNTBook("Jude", 1);
        addNTBook("Revelation", 22);

        // Common abbreviations
        bookAbbreviations.put("gen", "Genesis");
        bookAbbreviations.put("exod", "Exodus");
        bookAbbreviations.put("ex", "Exodus");
        bookAbbreviations.put("lev", "Leviticus");
        bookAbbreviations.put("num", "Numbers");
        bookAbbreviations.put("deut", "Deuteronomy");
        bookAbbreviations.put("josh", "Joshua");
        bookAbbreviations.put("judg", "Judges");
        bookAbbreviations.put("1sam", "1 Samuel");
        bookAbbreviations.put("2sam", "2 Samuel");
        bookAbbreviations.put("1kgs", "1 Kings");
        bookAbbreviations.put("2kgs", "2 Kings");
        bookAbbreviations.put("1chr", "1 Chronicles");
        bookAbbreviations.put("2chr", "2 Chronicles");
        bookAbbreviations.put("neh", "Nehemiah");
        bookAbbreviations.put("esth", "Esther");
        bookAbbreviations.put("ps", "Psalm");
        bookAbbreviations.put("psa", "Psalm");
        bookAbbreviations.put("psalms", "Psalm");
        bookAbbreviations.put("prov", "Proverbs");
        bookAbbreviations.put("eccl", "Ecclesiastes");
        bookAbbreviations.put("song", "Song Of Solomon");
        bookAbbreviations.put("sos", "Song Of Solomon");
        bookAbbreviations.put("isa", "Isaiah");
        bookAbbreviations.put("jer", "Jeremiah");
        bookAbbreviations.put("lam", "Lamentations");
        bookAbbreviations.put("ezek", "Ezekiel");
        bookAbbreviations.put("dan", "Daniel");
        bookAbbreviations.put("hos", "Hosea");
        bookAbbreviations.put("mic", "Micah");
        bookAbbreviations.put("nah", "Nahum");
        bookAbbreviations.put("hab", "Habakkuk");
        bookAbbreviations.put("zeph", "Zephaniah");
        bookAbbreviations.put("hag", "Haggai");
        bookAbbreviations.put("zech", "Zechariah");
        bookAbbreviations.put("mal", "Malachi");
        bookAbbreviations.put("matt", "Matthew");
        bookAbbreviations.put("mk", "Mark");
        bookAbbreviations.put("lk", "Luke");
        bookAbbreviations.put("jn", "John");
        bookAbbreviations.put("rom", "Romans");
        bookAbbreviations.put("1cor", "1 Corinthians");
        bookAbbreviations.put("2cor", "2 Corinthians");
        bookAbbreviations.put("gal", "Galatians");
        bookAbbreviations.put("eph", "Ephesians");
        bookAbbreviations.put("phil", "Philippians");
        bookAbbreviations.put("col", "Colossians");
        bookAbbreviations.put("1thess", "1 Thessalonians");
        bookAbbreviations.put("2thess", "2 Thessalonians");
        bookAbbreviations.put("1tim", "1 Timothy");
        bookAbbreviations.put("2tim", "2 Timothy");
        bookAbbreviations.put("tit", "Titus");
        bookAbbreviations.put("phlm", "Philemon");
        bookAbbreviations.put("heb", "Hebrews");
        bookAbbreviations.put("jas", "James");
        bookAbbreviations.put("1pet", "1 Peter");
        bookAbbreviations.put("2pet", "2 Peter");
        bookAbbreviations.put("1jn", "1 John");
        bookAbbreviations.put("2jn", "2 John");
        bookAbbreviations.put("3jn", "3 John");
        bookAbbreviations.put("rev", "Revelation");
    }

    private void addOTBook(String name, int chapters) {
        books.put(name, chapters);
        oldTestamentBooks.add(name);
    }

    private void addNTBook(String name, int chapters) {
        books.put(name, chapters);
        newTestamentBooks.add(name);
    }

    public List<String> getBooks() {
        return new ArrayList<>(books.keySet());
    }

    public List<String> getOldTestamentBooks() {
        return new ArrayList<>(oldTestamentBooks);
    }

    public List<String> getNewTestamentBooks() {
        return new ArrayList<>(newTestamentBooks);
    }

    public boolean isNewTestament(String book) {
        return newTestamentBooks.contains(book);
    }

    public boolean isOldTestament(String book) {
        return oldTestamentBooks.contains(book);
    }

    public int getChapterCount(String book) {
        return books.getOrDefault(book, 0);
    }

    /**
     * Find a book by name, abbreviation, or prefix match
     */
    public String findBook(String input) {
        if (input == null || input.isEmpty()) return null;

        String lower = input.toLowerCase();

        // Check abbreviations
        if (bookAbbreviations.containsKey(lower)) {
            return bookAbbreviations.get(lower);
        }

        // Check exact match (case-insensitive)
        for (String book : books.keySet()) {
            if (book.equalsIgnoreCase(input)) {
                return book;
            }
        }

        // Check starts with (case-insensitive)
        for (String book : books.keySet()) {
            if (book.toLowerCase().startsWith(lower)) {
                return book;
            }
        }

        return null;
    }

    /**
     * Get verse count for a specific chapter
     */
    public int getVerseCount(String book, int chapter) {
        if (bibleData == null) return 0;

        Map<String, Map<String, String>> bookData = bibleData.get(book);
        if (bookData == null) return 0;

        Map<String, String> chapterData = bookData.get(String.valueOf(chapter));
        if (chapterData == null) return 0;

        return chapterData.size();
    }

    /**
     * Fetch a single verse from in-memory data
     */
    public String getVerse(String book, int chapter, int verse) {
        if (bibleData == null) return null;

        Map<String, Map<String, String>> bookData = bibleData.get(book);
        if (bookData == null) return null;

        Map<String, String> chapterData = bookData.get(String.valueOf(chapter));
        if (chapterData == null) return null;

        return chapterData.get(String.valueOf(verse));
    }

    /**
     * Fetch a range of verses from in-memory data
     * Returns formatted text with bold superscript verse numbers
     */
    public String getVerseRange(String book, int chapter, int startVerse, int endVerse) {
        if (bibleData == null) return null;

        Map<String, Map<String, String>> bookData = bibleData.get(book);
        if (bookData == null) return null;

        Map<String, String> chapterData = bookData.get(String.valueOf(chapter));
        if (chapterData == null) return null;

        StringBuilder result = new StringBuilder();
        for (int v = startVerse; v <= endVerse; v++) {
            String verseText = chapterData.get(String.valueOf(v));
            if (verseText != null) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                // Add bold superscript verse number
                result.append(toBoldSuperscript(v)).append(verseText);
            }
        }

        return result.length() > 0 ? result.toString() : null;
    }

    /**
     * Fetch entire chapter with bold superscript verse numbers
     */
    public String getChapter(String book, int chapter) {
        if (bibleData == null) return "Bible data not loaded.";

        Map<String, Map<String, String>> bookData = bibleData.get(book);
        if (bookData == null) return "Book not found.";

        Map<String, String> chapterData = bookData.get(String.valueOf(chapter));
        if (chapterData == null) return "Chapter not found.";

        StringBuilder result = new StringBuilder();
        int verseCount = chapterData.size();

        for (int v = 1; v <= verseCount; v++) {
            String verseText = chapterData.get(String.valueOf(v));
            if (verseText != null) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                // Add bold superscript verse number
                result.append(toBoldSuperscript(v)).append(verseText);
            }
        }

        return result.toString();
    }

    /**
     * Converts a verse number to bold superscript format for Minecraft
     * Uses Unicode superscript characters
     */
    public String toBoldSuperscript(int number) {
        StringBuilder sb = new StringBuilder();
        String numStr = String.valueOf(number);
        for (char c : numStr.toCharArray()) {
            switch (c) {
                case '0' -> sb.append('\u2070');
                case '1' -> sb.append('\u00B9');
                case '2' -> sb.append('\u00B2');
                case '3' -> sb.append('\u00B3');
                case '4' -> sb.append('\u2074');
                case '5' -> sb.append('\u2075');
                case '6' -> sb.append('\u2076');
                case '7' -> sb.append('\u2077');
                case '8' -> sb.append('\u2078');
                case '9' -> sb.append('\u2079');
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Convert verse number string to superscript
     */
    public String toSuperscript(String number) {
        StringBuilder sb = new StringBuilder();
        for (char c : number.toCharArray()) {
            switch (c) {
                case '0' -> sb.append('\u2070');
                case '1' -> sb.append('\u00B9');
                case '2' -> sb.append('\u00B2');
                case '3' -> sb.append('\u00B3');
                case '4' -> sb.append('\u2074');
                case '5' -> sb.append('\u2075');
                case '6' -> sb.append('\u2076');
                case '7' -> sb.append('\u2077');
                case '8' -> sb.append('\u2078');
                case '9' -> sb.append('\u2079');
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
