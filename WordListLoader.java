package model;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WordListLoader {
    private final String filePath;

    public WordListLoader(String filePath) {
        this.filePath = filePath;
    }

    public List<String> loadWordList() {
        // Initialize a list of string type x
        List<String> wordList = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("Error: File not found at " + filePath);
            return wordList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String word;
            // Read each line and add each word
            while ((word = reader.readLine()) != null) {
                word = word.trim().toLowerCase(); // Trim and set the word to lowercase to match case of lists of saved input
                if (word.length() == 5) {
                    wordList.add(word);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading word list: " + e.getMessage());
        }

        if (wordList.isEmpty()) {
            System.err.println("Error: Word list is empty or could not be parsed.");
        }

        return wordList;
    }
}
