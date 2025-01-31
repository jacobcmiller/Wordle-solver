package model;
import java.util.List;

// Filter class to remove and filter words based on rules
public class Filter {
    public static void filterWordList(List<String> words, List<GrayPositionedLetter> grayLetters,
                                      List<PositionedLetter> yellowLetters, List<PositionedLetter> greenLetters) {
         // Removes if returned true for set conditions
        words.removeIf(word -> {
            // Check for green letters (must be at the exact position)
            for (PositionedLetter green : greenLetters) {
                
                if (word.charAt(green.position) != green.letter) {
                    return true; // Remove words without green letters in the correct position
                } 
                
            }

            // Check for gray letters (remove if no yellow record exists for the same letter)
            for (GrayPositionedLetter gray : grayLetters) {
                // Skip gray letter removal if there is a yellow record for the same letter
                boolean hasYellowConflict = yellowLetters.stream()
                        .anyMatch(yellow -> yellow.letter == gray.letter);
                // Logic fix added lines 26 & 27 
                // Skip gray letter removal if the letter is green at any position
                boolean hasGreenConflict = greenLetters.stream()
                .anyMatch(green -> green.letter == gray.letter);
                // Added hasGreenConflict
                if (!hasYellowConflict && !hasGreenConflict && word.contains(String.valueOf(gray.letter))) {
                    return true; // Remove words containing gray letters if no yellow conflict exists
                }
            }

            // Check for yellow letters
            for (PositionedLetter yellow : yellowLetters) {
                // Remove if the yellow letter is at the excluded position
                if (word.charAt(yellow.position) == yellow.letter) {
                    return true; // Remove words with yellow letters in the excluded position
                }

                // Remove if the yellow letter is not present at all
                if (!word.contains(String.valueOf(yellow.letter))) {
                    return true; // Remove words that do not contain the yellow letter
                }
            }

            return false; // Word is valid
        });
    }
}
