import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.ArrayDeque; 
import java.util.Deque; 

public class WordleOptimalGuessGUIRevised extends Application {

	private Deque <List <String>> historyStack = new ArrayDeque<>(); // undo stack to keep tract of word list (MINE)
	private Button undoButton; 
	
	 private TextField[] letterFields = new TextField[5];
	    private ComboBox<String>[] colorSelectors = new ComboBox[5];
	    private ListView<String> wordListView;
	    private List<String> wordList;

	    private static final String WORD_LIST_FILE = "C:/Users/diazm1a/Downloads/valid-wordle-words.txt";

	    @Override
	    public void start(Stage primaryStage) {
	        // Load word list
	        WordListLoader loader = new WordListLoader(WORD_LIST_FILE);
	        wordList = loader.loadWordList();

	        if (wordList == null || wordList.isEmpty()) {
	            System.err.println("Failed to load word list or word list is empty.");
	            Alert alert = new Alert(Alert.AlertType.ERROR, "Word list could not be loaded. Please check the file.", ButtonType.OK);
	            alert.showAndWait();
	            return;
	        }

	        primaryStage.setTitle("Optimal Wordle Guesser");

	        // Top section: Input instructions
	        Label inputLabel = new Label("Input Letters and Colors:");
	        inputLabel.setFont(new Font(16));
	        inputLabel.setPadding(new Insets(10));

	        // Middle section: Horizontal letter boxes with color selectors
	        HBox inputRow = new HBox(10);
	        inputRow.setAlignment(Pos.CENTER);
	        inputRow.setPadding(new Insets(10));

	        for (int i = 0; i < 5; i++) {
	            VBox letterBox = new VBox(5);
	            letterBox.setAlignment(Pos.CENTER);

	            TextField letterField = new TextField();
	            letterField.setPromptText("A");
	            letterField.setPrefWidth(50);
	            letterFields[i] = letterField;

	            ComboBox<String> colorSelector = new ComboBox<>();
	            colorSelector.getItems().addAll("Gray", "Yellow", "Green");
	            colorSelector.setPromptText("Color");
	            colorSelectors[i] = colorSelector;

	            letterBox.getChildren().addAll(letterField, colorSelector);
	            inputRow.getChildren().add(letterBox);
	        }

	        //added undo button to GUI 
	        undoButton = new Button("Undo"); // MINE
	        undoButton.setDisable(true); // starts as disabled since there is nothing to undo yet (MINE)
	        undoButton.setOnAction(e -> undoLastAction()); //attaching undoLastAction method (MINE)
	        
	        
	        
	        Button saveButton = new Button("Save Input");
	        saveButton.setOnAction(e -> saveInput());

	        HBox buttonContainer = new HBox(10, saveButton, undoButton); // created button container for both save and undo (MINE)
	        buttonContainer.setAlignment(Pos.CENTER); 
	        buttonContainer.setPadding(new Insets (10));
	        
	        
	        VBox inputContainer = new VBox(10, inputLabel, inputRow, buttonContainer); // buttonContainer replaced saveButton 
	        inputContainer.setAlignment(Pos.CENTER);

	        wordListView = new ListView<>();
	        wordListView.setPrefWidth(200);
	        wordListView.setPlaceholder(new Label("Sorted words will appear here"));
	        VBox wordListContainer = new VBox(10, new Label("Sorted Word List:"), wordListView);
	        wordListContainer.setAlignment(Pos.TOP_LEFT);
	        wordListContainer.setPadding(new Insets(10));

	        HBox mainLayout = new HBox(20, inputContainer, wordListContainer);
	        mainLayout.setPadding(new Insets(20));

	        
	        
	        
	        Scene scene = new Scene(mainLayout, 800, 400); 
	        primaryStage.setScene(scene);
	        primaryStage.show();
	    }

	    
	    
	    
	    
	    
	    private void saveInput() {
	    	
	        List<Filter.GrayPositionedLetter> grayLetters = new ArrayList<>();
	        List<Filter.PositionedLetter> yellowLetters = new ArrayList<>();
	        List<Filter.PositionedLetter> greenLetters = new ArrayList<>();
	    	
	        for (int i = 0; i < 5; i++) {
	            String letter = letterFields[i].getText().toLowerCase().trim();
	            String color = colorSelectors[i].getValue();

	            if (letter.isEmpty() || color == null) {
	                Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill all boxes and select colors.", ButtonType.OK);
	                alert.show();
	                return;
	            }
	        
	            switch (color) {
                case "Gray":
                    grayLetters.add(new Filter.GrayPositionedLetter(letter.charAt(0), i));
                    break;
                case "Yellow":
                    yellowLetters.add(new Filter.PositionedLetter(letter.charAt(0), i));
                    break;
                case "Green":
                    greenLetters.add(new Filter.PositionedLetter(letter.charAt(0), i));
                    break;
            }
        }
	        
	        //FROM HERE    
	    	synchronized (wordList) { //save current state of wordlist before Filtering (MINE)
	    		historyStack.push(new ArrayList<>(wordList)); //transfer a copy of current wordlist onto history stack (MINE)
	    	}
	    	
	    	Task<Void> filterTask = new Task<>() {
	    		@Override
	    		protected Void call() throws Exception {
	    			synchronized(wordList) {
	    				Filter.filterWordList(wordList, grayLetters, yellowLetters, greenLetters);
	    				wordList.sort((word1, word2) -> {
	    					int score1 = calculateWordScore(word1, yellowLetters, greenLetters);
	    					int score2 = calculateWordScore(word2, yellowLetters, greenLetters);
	    					return Integer.compare(score2, score1);
	    				});
	    			}
	    			return null;
	    		}
	    		@Override
	    		protected void succeeded() {
	    			Platform.runLater(() -> {
	    				wordListView.getItems().setAll(wordList);
	    				
	    				updateUndoButtonState(); //Update the undo button after succesful filter (MINE ADDITION JUST THIS LINE)
	    				
	    			});
	    		}
	    		
	    		@Override 
	    		protected void failed() {
	    			Platform.runLater(() -> {
	    				Alert alert = new Alert(Alert.AlertType.ERROR, "Error filtering words: " + getException().getMessage(), ButtonType.OK);
	    				alert.show();
	    			});
	    		}
	    	};
	    	
	    	Thread filterThread = new Thread (filterTask);
	    	filterThread.setDaemon(true);
	    	filterThread.start();
	    	
	    	for(int i1 = 0; i1 < 5; i1++) {
	    		letterFields[i1].clear();
	    		colorSelectors[i1].setValue(null);
	    	}
	    	
	    	// TO HERE

	        Task<Void> filterTask1 = new Task<>() { // filterTask was changed to -> filterTask1
	        	
	            @Override
	            protected Void call() throws Exception {
	                synchronized (wordList) {
	                    // Filter words dynamically
	                    Filter.filterWordList(wordList, grayLetters, yellowLetters, greenLetters);

	                    // Sort for optimal guesses
	                    wordList.sort((word1, word2) -> {
	                        int score1 = calculateWordScore(word1, yellowLetters, greenLetters);
	                        int score2 = calculateWordScore(word2, yellowLetters, greenLetters);
	                        return Integer.compare(score2, score1); // Higher scores first
	                    });
	                }
	                return null;
	            }

	            @Override
	            protected void succeeded() {
	                Platform.runLater(() -> wordListView.getItems().setAll(wordList));
	            }

	            @Override
	            protected void failed() {
	                Platform.runLater(() -> {
	                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error filtering words: " + getException().getMessage(), ButtonType.OK);
	                    alert.show();
	                });
	            }
	        };

	        Thread filterThread1 = new Thread(filterTask1);
	        filterThread1.setDaemon(true);
	        filterThread1.start();

	        // Clear inputs for the next iteration
	        for (int i = 0; i < 5; i++) {
	            letterFields[i].clear();
	            colorSelectors[i].setValue(null);
	        }
	    }

	    private int calculateWordScore(String word, List<Filter.PositionedLetter> yellowLetters, List<Filter.PositionedLetter> greenLetters) {
	        int score = 0;

	        // Reward for green letters in the correct position
	        for (Filter.PositionedLetter green : greenLetters) {
	            if (word.charAt(green.position) == green.letter) {
	                score += 5;
	            }
	        }

	        // Reward for yellow letters present in the word but not at the excluded position
	        for (Filter.PositionedLetter yellow : yellowLetters) {
	            if (word.indexOf(yellow.letter) != -1 && word.charAt(yellow.position) != yellow.letter) {
	                score += 2;
	            }
	        }

	        return score;
	    }
	    
	    
	   
	    private void undoLastAction () { //MINE
	    	if (!historyStack.isEmpty()) { // undo last action by popping the previous state from history stack 
	    		synchronized(wordList) {
	    			wordList.clear();
	    			wordList.addAll(historyStack.pop()); //Restores last saved wordlist
	    		}
	    		wordListView.getItems().setAll(wordList); //updates list view 
	    	}
	    	updateUndoButtonState();
	    }
	    
	    private void updateUndoButtonState() { // MINE
	    	undoButton.setDisable(historyStack.isEmpty()); // to enable or disable button if stack is empty
	    }
	     
	    
	    public static void main(String[] args) { 
	        launch(args);
	    }
	}
	
