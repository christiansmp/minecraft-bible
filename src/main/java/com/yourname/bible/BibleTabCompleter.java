package com.yourname.bible;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BibleTabCompleter implements TabCompleter {
    private final BibleData data;
    
    public BibleTabCompleter(BibleData data) {
        this.data = data;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument: "open" or book names
            completions.add("open");
            completions.addAll(data.getBooks());
            
            // Filter based on what user typed
            String input = args[0].toLowerCase();
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            // Check if first arg was a number (for books like "1 Samuel")
            if (args[0].equals("1") || args[0].equals("2") || args[0].equals("3")) {
                // Show second part of book names
                List<String> numberedBooks = new ArrayList<>();
                for (String book : data.getBooks()) {
                    if (book.startsWith(args[0] + " ")) {
                        numberedBooks.add(book.split(" ")[1]);
                    }
                }
                return numberedBooks.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            // Show chapter numbers for the selected book
            String book = data.findBook(args[0]);
            if (book != null) {
                int chapters = data.getChapterCount(book);
                for (int i = 1; i <= Math.min(chapters, 20); i++) {
                    completions.add(String.valueOf(i));
                }
            }
        }
        
        if (args.length == 3) {
            // For numbered books, chapter is at position 3
            if (args[0].equals("1") || args[0].equals("2") || args[0].equals("3")) {
                String book = data.findBook(args[0] + " " + args[1]);
                if (book != null) {
                    int chapters = data.getChapterCount(book);
                    for (int i = 1; i <= Math.min(chapters, 20); i++) {
                        completions.add(String.valueOf(i));
                    }
                }
            } else {
                // Suggest verse numbers (generic 1-20)
                for (int i = 1; i <= 20; i++) {
                    completions.add(String.valueOf(i));
                }
            }
        }
        
        if (args.length == 4) {
            // Verse numbers for numbered books
            for (int i = 1; i <= 20; i++) {
                completions.add(String.valueOf(i));
            }
        }
        
        return completions;
    }
}