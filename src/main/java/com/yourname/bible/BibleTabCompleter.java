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

        // Build what the user has typed so far for the book name
        StringBuilder typedSoFar = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) typedSoFar.append(" ");
            typedSoFar.append(args[i]);
        }
        String typed = typedSoFar.toString().toLowerCase();

        // Check if the last argument contains a colon (chapter:verse)
        // If so, the user is done with the book name - no more completions
        if (args.length > 0 && args[args.length - 1].contains(":")) {
            return completions; // Empty list - no suggestions
        }

        // First argument: "open" or start of book names
        if (args.length == 1) {
            String input = args[0].toLowerCase();

            // Add "open" command
            if ("open".startsWith(input)) {
                completions.add("open");
            }

            // Add all matching book names
            for (String book : data.getBooks()) {
                if (book.toLowerCase().startsWith(input)) {
                    // For multi-word books, only show the first word initially
                    String[] words = book.split(" ");
                    if (words.length > 1) {
                        // If typing starts a numbered book (1, 2, 3), show the number
                        if (words[0].equals("1") || words[0].equals("2") || words[0].equals("3")) {
                            if (words[0].startsWith(input) || input.isEmpty()) {
                                completions.add(words[0]);
                            }
                        } else {
                            completions.add(book);
                        }
                    } else {
                        completions.add(book);
                    }
                }
            }

            // Remove duplicates and return
            return completions.stream().distinct().collect(Collectors.toList());
        }

        // Multi-word book name completion
        if (args.length >= 2) {
            String firstArg = args[0];

            // Handle numbered books (1 Samuel, 2 Kings, etc.)
            if (firstArg.equals("1") || firstArg.equals("2") || firstArg.equals("3")) {
                // Find books that start with this number
                for (String book : data.getBooks()) {
                    if (book.startsWith(firstArg + " ")) {
                        // Get the remaining part of the book name
                        String remaining = book.substring(firstArg.length() + 1);
                        String[] remainingWords = remaining.split(" ");

                        // Check how many words the user has typed after the number
                        int wordsTyped = args.length - 1;

                        if (wordsTyped < remainingWords.length) {
                            // Build what we should match against
                            StringBuilder matchBuilder = new StringBuilder();
                            for (int i = 1; i < args.length; i++) {
                                if (i > 1) matchBuilder.append(" ");
                                matchBuilder.append(args[i]);
                            }
                            String matchTyped = matchBuilder.toString().toLowerCase();

                            // Build the expected portion of the book name
                            StringBuilder expectedBuilder = new StringBuilder();
                            for (int i = 0; i < wordsTyped; i++) {
                                if (i > 0) expectedBuilder.append(" ");
                                expectedBuilder.append(remainingWords[i]);
                            }
                            String expected = expectedBuilder.toString().toLowerCase();

                            // If what user typed matches the beginning, suggest next word
                            if (expected.startsWith(matchTyped) || matchTyped.isEmpty()) {
                                if (wordsTyped < remainingWords.length) {
                                    completions.add(remainingWords[wordsTyped]);
                                }
                            }
                        }
                    }
                }
            } else {
                // Non-numbered multi-word books (e.g., "Song of Solomon")
                for (String book : data.getBooks()) {
                    String[] bookWords = book.split(" ");
                    if (bookWords.length > 1 && bookWords[0].equalsIgnoreCase(firstArg)) {
                        // User has typed the first word of a multi-word book
                        int wordsTyped = args.length;

                        if (wordsTyped <= bookWords.length) {
                            // Build what user typed
                            StringBuilder matchBuilder = new StringBuilder();
                            for (int i = 0; i < args.length; i++) {
                                if (i > 0) matchBuilder.append(" ");
                                matchBuilder.append(args[i]);
                            }
                            String matchTyped = matchBuilder.toString().toLowerCase();

                            // Check if it matches the book so far
                            StringBuilder bookBuilder = new StringBuilder();
                            for (int i = 0; i < wordsTyped; i++) {
                                if (i > 0) bookBuilder.append(" ");
                                bookBuilder.append(bookWords[i]);
                            }
                            String bookPrefix = bookBuilder.toString().toLowerCase();

                            if (bookPrefix.startsWith(matchTyped)) {
                                if (wordsTyped < bookWords.length) {
                                    completions.add(bookWords[wordsTyped]);
                                }
                            }
                        }
                    }
                }
            }
        }

        return completions.stream().distinct().collect(Collectors.toList());
    }
}
