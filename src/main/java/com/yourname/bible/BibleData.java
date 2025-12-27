package com.yourname.bible;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class BibleData {
    private final Map<String, Integer> books = new LinkedHashMap<>();
    private final Map<String, String> bookAbbreviations = new HashMap<>();
    private final List<String> oldTestamentBooks = new ArrayList<>();
    private final List<String> newTestamentBooks = new ArrayList<>();

    public BibleData() {
        initializeBooks();
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
        addOTBook("Psalms", 150);
        addOTBook("Proverbs", 31);
        addOTBook("Ecclesiastes", 12);
        addOTBook("Song of Solomon", 8);
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
        bookAbbreviations.put("lev", "Leviticus");
        bookAbbreviations.put("num", "Numbers");
        bookAbbreviations.put("deut", "Deuteronomy");
        bookAbbreviations.put("josh", "Joshua");
        bookAbbreviations.put("matt", "Matthew");
        bookAbbreviations.put("rom", "Romans");
        bookAbbreviations.put("rev", "Revelation");
        bookAbbreviations.put("ps", "Psalms");
        bookAbbreviations.put("prov", "Proverbs");
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
    
    public String findBook(String input) {
        String lower = input.toLowerCase();
        
        // Check abbreviations
        if (bookAbbreviations.containsKey(lower)) {
            return bookAbbreviations.get(lower);
        }
        
        // Check exact match
        for (String book : books.keySet()) {
            if (book.equalsIgnoreCase(input)) {
                return book;
            }
        }
        
        // Check starts with
        for (String book : books.keySet()) {
            if (book.toLowerCase().startsWith(lower)) {
                return book;
            }
        }
        
        return null;
    }
    
    public String fetchVerse(String book, int chapter, int verse) {
        try {
            String reference = book.replace(" ", "+") + "+" + chapter + ":" + verse;
            URL url = new URL("https://bible-api.com/" + reference + "?translation=kjv");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JsonObject json = new Gson().fromJson(response.toString(), JsonObject.class);
            if (json.has("text")) {
                String text = json.get("text").getAsString();
                // Clean up the text - remove verse numbers and extra whitespace
                text = text.replaceAll("\\d+", "").trim();
                return text;
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public String fetchChapter(String book, int chapter) {
        try {
            String reference = book.replace(" ", "+") + "+" + chapter;
            URL url = new URL("https://bible-api.com/" + reference + "?translation=kjv");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JsonObject json = new Gson().fromJson(response.toString(), JsonObject.class);
            if (json.has("text")) {
                return json.get("text").getAsString();
            }
            
            return "Chapter not available.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error loading chapter.";
        }
    }
}