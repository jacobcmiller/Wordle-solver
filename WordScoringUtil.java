package model;
import java.util.List;

// Utility class for calculating word scores based on letter positions
public class WordScoringUtil {
    public static int calculateScore(String word, List<PositionedLetter> yellowLetters, List<PositionedLetter> greenLetters) {
        int score = 0;

        // Reward for green letters in the correct position
        for (PositionedLetter green : greenLetters) {
            if (word.charAt(green.position) == green.letter) {
                score += 5; // Green adds 5 to the score
            }
        }

        // Reward for yellow letters present in the word but not at the excluded position
        for (PositionedLetter yellow : yellowLetters) {
            if (word.indexOf(yellow.letter) != -1 && word.charAt(yellow.position) != yellow.letter) {
                score += 2; // Yellow only adds 2
            }
        }

        return score; // Return the final score after adding up
    }
}
