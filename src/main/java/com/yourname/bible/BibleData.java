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
    
    public BibleData() {
        initializeBooks();
    }
    
    private void initializeBooks() {
        // Old Testament
        books.put("Genesis", 50);
        books.put("Exodus", 40);
        books.put("Leviticus", 27);
        books.put("Numbers", 36);
        books.put("Deuteronomy", 34);
        books.put("Joshua", 24);
        books.put("Judges", 21);
        books.put("Ruth", 4);
        books.put("1 Samuel", 31);
        books.put("2 Samuel", 24);
        books.put("1 Kings", 22);
        books.put("2 Kings", 25);
        books.put("1 Chronicles", 29);
        books.put("2 Chronicles", 36);
        books.put("Ezra", 10);
        books.put("Nehemiah", 13);
        books.put("Esther", 10);
        books.put("Job", 42);
        books.put("Psalms", 150);
        books.put("Proverbs", 31);
        books.put("Ecclesiastes", 12);
        books.put("Song of Solomon", 8);
        books.put("Isaiah", 66);
        books.put("Jeremiah", 52);
        books.put("Lamentations", 5);
        books.put("Ezekiel", 48);
        books.put("Daniel", 12);
        books.put("Hosea", 14);
        books.put("Joel", 3);
        books.put("Amos", 9);
        books.put("Obadiah", 1);
        books.put("Jonah", 4);
        books.put("Micah", 7);
        books.put("Nahum", 3);
        books.put("Habakkuk", 3);
        books.put("Zephaniah", 3);
        books.put("Haggai", 2);
        books.put("Zechariah", 14);
        books.put("Malachi", 4);
        
        // New Testament
        books.put("Matthew", 28);
        books.put("Mark", 16);
        books.put("Luke", 24);
        books.put("John", 21);
        books.put("Acts", 28);
        books.put("Romans", 16);
        books.put("1 Corinthians", 16);
        books.put("2 Corinthians", 13);
        books.put("Galatians", 6);
        books.put("Ephesians", 6);
        books.put("Philippians", 4);
        books.put("Colossians", 4);
        books.put("1 Thessalonians", 5);
        books.put("2 Thessalonians", 3);
        books.put("1 Timothy", 6);
        books.put("2 Timothy", 4);
        books.put("Titus", 3);
        books.put("Philemon", 1);
        books.put("Hebrews", 13);
        books.put("James", 5);
        books.put("1 Peter", 5);
        books.put("2 Peter", 3);
        books.put("1 John", 5);
        books.put("2 John", 1);
        books.put("3 John", 1);
        books.put("Jude", 1);
        books.put("Revelation", 22);
        
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
    
    public List<String> getBooks() {
        return new ArrayList<>(books.keySet());
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