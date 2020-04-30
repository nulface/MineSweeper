package minesweeper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MineSweeper extends VBox {

	private Stage stage;
	private Stage customDifficulty;
	private Stage showHighScores;
	private Stage getPlayerNameWindow;

	private BorderPane mainView;
	private BorderPane topMenu;

	private HighScorePane highScorePane;

	private enum scoreDifficulties {
		BEGINNER, INTERMEDIATE, EXPERT, CUSTOM
	};

	private Face face;
	private Counter clock;
	private Pane fieldPane;
	private Mine mines[][];
	private Counter flagCounter;

	private static final int BEVELLAYERS = 4;
	private static final String INNERBEVEL = "inner-outline";
	private static final String OUTERBEVEL = "outer-outline";
	private static final String CSSFILE = "minesweeper.css";
	private static final int PAD = 5;
	private static final Insets PADDING = new Insets(PAD, PAD, PAD, PAD);
	private static final String BEGINNERFILE = "scores/beginner.swp";
	private static final String INTERMEDIATEFILE = "scores/intermediate.swp";
	private static final String EXPERTFILE = "scores/expert.swp";
	private static final String CUSTOMFILE = "scores/custom.swp";

	private int numRevealed;
	private boolean gameOver, firstClick;

	private Difficulty state;
	private final ExpertGame EXPERT = new ExpertGame();
	private final BeginnerGame BEGINNER = new BeginnerGame();
	private final IntermediateGame INTERMEDIATE = new IntermediateGame();

	private Timeline timeline;

	private final static Image DEAD = new Image("file:res/dead.png");
	private final static Image SMILE = new Image("file:res/smile.png");
	private final static Image OFACE = new Image("file:res/ohface.png");
	private final static Image SUNGLASSES = new Image("file:res/sunglasses.png");
	private final static Image SMILEPRESSED = new Image("file:res/smilepressed.png");
	private final static Image D0 = new Image("file:res/0.png");
	private final static Image D1 = new Image("file:res/1.png");
	private final static Image D2 = new Image("file:res/2.png");
	private final static Image D3 = new Image("file:res/3.png");
	private final static Image D4 = new Image("file:res/4.png");
	private final static Image D5 = new Image("file:res/5.png");
	private final static Image D6 = new Image("file:res/6.png");
	private final static Image D7 = new Image("file:res/7.png");
	private final static Image D8 = new Image("file:res/8.png");
	private final static Image D9 = new Image("file:res/9.png");
	private final static Image M1 = new Image("file:res/1tile.png");
	private final static Image M2 = new Image("file:res/2tile.png");
	private final static Image M3 = new Image("file:res/3tile.png");
	private final static Image M4 = new Image("file:res/4tile.png");
	private final static Image M5 = new Image("file:res/5tile.png");
	private final static Image M6 = new Image("file:res/6tile.png");
	private final static Image M7 = new Image("file:res/7tile.png");
	private final static Image M8 = new Image("file:res/8tile.png");
	private final static Image FLAG = new Image("file:res/flag.png");
	private final static Image MINE = new Image("file:res/mine.png");
	private final static Image XMINE = new Image("file:res/xmine.png");
	private final static Image REDMINE = new Image("file:res/redmine.png");
	private final static Image EMPTYSPACE = new Image("file:res/empty.png");
	private final static Image EMPTYPRESSED = new Image("file:res/emptyPressed.png");

	private final static Image[] DIGITS = { D0, D1, D2, D3, D4, D5, D6, D7, D8, D9 };
	private final static Image[] MINES = { EMPTYPRESSED, M1, M2, M3, M4, M5, M6, M7, M8 };

	public MineSweeper(@SuppressWarnings("exports") Stage stage) {
		this.stage = stage;
		setup();
		newGame(BEGINNER);
	}

	private static Image getDigitImage(char n) {
		return getDigitImage(Integer.parseInt("" + n));
	}

	private static Image getDigitImage(int n) {
		return DIGITS[n];
	}

	private static Image getMineImage(int n) {
		return MINES[n];
	}

	private void setup() {

		setupHighScoreScreen();

		stage.getIcons().add(SUNGLASSES);
		stage.setResizable(false);

		stage.setOnHiding(e -> {
			System.exit(0);
		});

		stage.setOnShowing(e -> {
			stage.getScene().getStylesheets().add(getClass().getResource(CSSFILE).toExternalForm());
		});

		stage.setOnShown(e -> {
			stage.setX(((Screen.getScreens().get(0).getBounds().getMaxX()) / 2) - ((stage.getWidth()) / 2));
			stage.setY(((Screen.getScreens().get(0).getBounds().getMaxY()) / 2) - ((stage.getHeight()) / 2));
		});

		customDifficulty = new Stage();
		customDifficulty.setResizable(false);

		MineSweeperDifficultyMenu difficultyMenu = new MineSweeperDifficultyMenu();

		customDifficulty.setScene(new Scene(bevelPane(difficultyMenu, OUTERBEVEL, BEVELLAYERS)));
		customDifficulty.getScene().getStylesheets().add(getClass().getResource(CSSFILE).toExternalForm());
		customDifficulty.initStyle(StageStyle.UTILITY);

		customDifficulty.setOnShowing(e -> {
			difficultyMenu.resetTextFields();
		});

		timeline = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
			clock.tick();
		}));
		timeline.setCycleCount(Animation.INDEFINITE);

		mainView = new BorderPane();
		topMenu = new BorderPane();

		flagCounter = new Counter();
		clock = new Counter();
		face = new Face();

		topMenu.setLeft(flagCounter);
		topMenu.setCenter(face);
		topMenu.setRight(clock);
		topMenu.setPadding(PADDING);

		BorderPane pane = new BorderPane();
		pane.setCenter(bevelPane(topMenu, INNERBEVEL, BEVELLAYERS));
		pane.setPadding(new Insets(0, 0, PAD, 0));
		mainView.setTop(pane);
		mainView.setPadding(PADDING);

		fieldPane = new Pane();
		mainView.setCenter(bevelPane(fieldPane, INNERBEVEL, BEVELLAYERS));

		this.getChildren().addAll(new MineSweeperMenuBar(), bevelPane(mainView, OUTERBEVEL, BEVELLAYERS));
	}

	private void setupHighScoreScreen() {
		showHighScores = new Stage();
		highScorePane = new HighScorePane();
		showHighScores.setScene(new Scene(highScorePane));
		showHighScores.getScene().getStylesheets().add(getClass().getResource(CSSFILE).toExternalForm());
		showHighScores.setResizable(false);
		showHighScores.initStyle(StageStyle.UTILITY);
	}

	private class HighScorePane extends Pane {
		private ArrayList<HighScore> gameScores = new ArrayList<HighScore>(0);
		private static final int NUMBEROFSCORES = 3;
		private String fileName;
		VBox labels;

		HighScorePane() {

			File file = new File("scores");
			if (file.mkdir()) {
				System.out.println("scores folder created");
			} else {
				System.out.println("scores folder already exists");
			}

			labels = new VBox();
			BorderPane holdButton = new BorderPane();
			Button clearScores = new Button("Clear Scores");
			holdButton.setCenter(clearScores);
			VBox p = new VBox();

			p.getChildren().addAll(labels, holdButton);
			clearScores.setOnAction(e -> {
				highScorePane.deleteFile();
				highScorePane.refreshPane();
				showHighScores.show();
				showHighScores.sizeToScene();
			});

			this.getChildren().add(bevelBorder(p));
			labels.setPadding(PADDING);

		}

		private void getScores(scoreDifficulties diff) {

			fileNameFromDifficulty(diff);

			refreshPane();
		}

		private void refreshPane() {
			labels.getChildren().clear();

			getScoresFromFile();

			Collections.sort(gameScores);

			int max = (NUMBEROFSCORES < gameScores.size()) ? NUMBEROFSCORES : gameScores.size();

			if (gameScores.size() > 0) {
				for (int i = 0; i < max; i++) {
					labels.getChildren().add(new Label(gameScores.get(i).toString()));
				}
			} else {
				labels.getChildren().add(new Label("There are no" + "" + " high scores yet!"));
			}
		}

		private void fileNameFromDifficulty(scoreDifficulties diff) {
			switch (diff) {
			case BEGINNER:
				fileName = BEGINNERFILE;
				break;
			case INTERMEDIATE:
				fileName = INTERMEDIATEFILE;
				break;
			case EXPERT:
				fileName = EXPERTFILE;
				break;
			case CUSTOM:
				fileName = CUSTOMFILE;
				break;
			default:
				fileName = BEGINNERFILE;
			}
		}

		private void writeScoresToFile(scoreDifficulties diff, HighScore hs) {
			fileNameFromDifficulty(diff);

			getScoresFromFile();

			gameScores.add(hs);
			Collections.sort(gameScores);

			int max = (NUMBEROFSCORES < gameScores.size()) ? NUMBEROFSCORES : gameScores.size();

			try (DataOutputStream output = new DataOutputStream(new FileOutputStream(fileName));) {

				for (int i = 0; i < max; i++) {
					output.writeUTF(gameScores.get(i).getName());
					output.writeUTF(gameScores.get(i).getDifficulty());
					output.writeInt(gameScores.get(i).getScore());
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		private void getScoresFromFile() {

			File file = new File(fileName);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			gameScores.clear();
			try (DataInputStream input = new DataInputStream(new FileInputStream(file));) {
				while (input.available() > 0) {
					gameScores.add(new HighScore(input.readUTF(), input.readUTF(), input.readInt()));
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}

		private void deleteFile() {
			// fileNameFromDifficulty(diff);

			File file = new File(fileName);
			if (file.exists()) {
				file.delete();
			}
		}

	}

	private void setupPlayerNameStage() {
		getPlayerNameWindow = new Stage();
		getPlayerNameWindow.setScene(new Scene(new EnterHighScore(clock.getValue())));
		getPlayerNameWindow.getScene().getStylesheets().add(getClass().getResource(CSSFILE).toExternalForm());
		getPlayerNameWindow.initStyle(StageStyle.UTILITY);
		getPlayerNameWindow.setResizable(false);
		getPlayerNameWindow.show();
	}

	private class EnterHighScore extends BorderPane {
		TextField name;

		EnterHighScore(int score) {

			name = new TextField();

			VBox items = new VBox();
			Label scoreLabel = new Label("Your score is: " + score);
			scoreLabel.setAlignment(Pos.CENTER);
			Label enterName = new Label("Enter Name: ");
			Button confirmButton = new Button("Confirm name");
			items.getChildren().addAll(scoreLabel, enterName, name, confirmButton);

			items.setAlignment(Pos.CENTER);

			this.setCenter(bevelBorder(items));

			confirmButton.setOnAction(e -> {
				String input = name.getText();
				try {
					InputHandler.notEmptyOrThrow(input);
					InputHandler.noWhiteSpaceOrThrow(input);
					InputHandler.isAlphabeticalOrThrow(input);

					HighScore hs = new HighScore(name.getText(), state.getDifficulty().toString().toLowerCase(), score);
					highScorePane.writeScoresToFile(state.getDifficulty(), hs);
					openHighScoreWindow(state.getDifficulty());
					getPlayerNameWindow.close();

				} catch (SelectionException ex) {
					System.out.println(ex.getMessage());
				} catch (WordException ex) {
					System.out.println(ex.getMessage());
				}
			});

		}
	}

	private void openHighScoreWindow(scoreDifficulties d) {
		highScorePane.getScores(d);
		showHighScores.show();
		showHighScores.sizeToScene();
	}

	private class MineSweeperMenuBar extends MenuBar {

		MineSweeperMenuBar() {
			Menu menu = new Menu();
			menu.setText("Game");
			menu.getStyleClass().add(OUTERBEVEL);

			MenuItem d0 = new MenuItem();
			d0.setText("Beginner Difficulty");
			d0.setOnAction(e -> {
				customDifficulty.hide();
				newGame(BEGINNER);
			});
			d0.getStyleClass().add(INNERBEVEL);

			MenuItem d1 = new MenuItem();
			d1.setText("Intermediate Difficulty");
			d1.setOnAction(e -> {
				customDifficulty.hide();
				newGame(INTERMEDIATE);
			});
			d1.getStyleClass().add(INNERBEVEL);

			MenuItem d2 = new MenuItem();
			d2.setText("Expert Difficulty");
			d2.setOnAction(e -> {
				customDifficulty.hide();
				newGame(EXPERT);
			});
			d2.getStyleClass().add(INNERBEVEL);

			MenuItem c1 = new MenuItem();
			c1.setText("Difficulty Menu");
			c1.setOnAction(e -> {
				customDifficulty.show();
			});
			c1.getStyleClass().add(INNERBEVEL);

			menu.getItems().addAll(d0, d1, d2, c1);

			Menu scores = new Menu();
			scores.setText("High Scores");

			MenuItem beginnerscore = new MenuItem();
			beginnerscore.setText("Show Beginner game Scores");
			beginnerscore.setOnAction(e -> {
				openHighScoreWindow(scoreDifficulties.BEGINNER);
			});

			MenuItem intermediatescore = new MenuItem();
			intermediatescore.setText("Show Intermediate game Scores");
			intermediatescore.setOnAction(e -> {
				openHighScoreWindow(scoreDifficulties.INTERMEDIATE);
			});

			MenuItem expertscore = new MenuItem();
			expertscore.setText("Show Expert game Scores");
			expertscore.setOnAction(e -> {
				openHighScoreWindow(scoreDifficulties.EXPERT);
			});

			MenuItem customscore = new MenuItem();
			customscore.setText("Show Custom game Scores");
			customscore.setOnAction(e -> {
				openHighScoreWindow(scoreDifficulties.CUSTOM);
			});

			scores.getItems().addAll(beginnerscore, intermediatescore, expertscore, customscore);

			this.getMenus().addAll(menu, scores);

		}

	}

	private void gameWon() {
		gameOver();
		face.currentFace.setImage(SUNGLASSES);

		highScorePane.getScores(state.getDifficulty());

		if (highScorePane.gameScores.size() > 0) {

			for (int i = 0; i < highScorePane.gameScores.size(); i++) {
				if (clock.getValue() < highScorePane.gameScores.get(i).getScore()) {
					setupPlayerNameStage();
				}
			}
			if (highScorePane.gameScores.size() < 3) {
				setupPlayerNameStage();
			}

		} else {
			setupPlayerNameStage();
		}

	}

	private void gameOver() {
		gameOver = true;
		timeline.stop();
		revealAll();
	}

	private void gameLost() {
		gameOver();
		face.currentFace.setImage(DEAD);
	}

	private Pane bevelBorder(Pane p) {

		BorderPane inner = new BorderPane();
		BorderPane outer = new BorderPane();

		inner.setCenter(p);
		outer.setPadding(PADDING);
		outer.setCenter(bevelPane(inner, INNERBEVEL, BEVELLAYERS));

		return bevelPane(outer, OUTERBEVEL, BEVELLAYERS);
	}

	private Pane bevelPane(Pane p, String styleSheet, int layers) {
		BorderPane pane = new BorderPane();
		pane.setCenter(p);
		pane.getStyleClass().add(styleSheet);

		if (layers > 0) {
			return bevelPane(pane, styleSheet, layers - 1);
		} else {
			return pane;
		}
	}

	private class HighScore implements Comparable<HighScore> {
		private String name, difficulty;
		private int time;

		HighScore(String name, String difficulty, int score) {
			this.name = name;
			this.difficulty = difficulty;
			this.time = score;
		}

		private String getName() {
			return name;
		}

		private String getDifficulty() {
			return difficulty;
		}

		private int getScore() {
			return time;
		}

		public String toString() {
			return name + " beat " + difficulty + " in " + time + " seconds.";
		}

		@Override
		public int compareTo(HighScore o) {
			return (this.time < o.time) ? -1 : 1;
		}
	}

	private class MineSweeperDifficultyMenu extends VBox {
		private TextField widthField = new TextField();
		private TextField heightField = new TextField();
		private TextField mineTextField = new TextField();

		private static final String ENTERHEIGHT = "Enter Height";
		private static final String ENTERWIDTH = "Enter Width";
		private static final String ENTERMINES = "Enter Mines";

		MineSweeperDifficultyMenu() {

			Label errFeedback = new Label("");
			errFeedback.getStyleClass().add("error-label");
			errFeedback.setPadding(new Insets(PAD, 0, PAD, 0));

			// VBox items = new VBox();
			this.setPadding(PADDING);

			Label difficultyTitle = new Label("Select Difficulty");
			difficultyTitle.setPadding(new Insets(0, 0, 2, 0));

			ToggleGroup difficultyToggle = new ToggleGroup();

			RadioButton beginnerButton = new RadioButton();
			beginnerButton.setToggleGroup(difficultyToggle);
			beginnerButton.selectedProperty().addListener(e -> {
				errFeedback.setText("Beginner selected, click ok");
				resetTextFields();
			});
			BorderPane beginnerBorder = new BorderPane();
			Label beginnerLabel = new Label("Beginner");
			beginnerBorder.setOnMouseClicked(e -> {
				beginnerButton.setSelected(true);
				resetTextFields();
			});
			beginnerLabel.getStyleClass().add("radio-label");
			beginnerBorder.setCenter(beginnerLabel);
			beginnerBorder.setLeft(beginnerButton);

			RadioButton intermediateButton = new RadioButton();
			intermediateButton.setToggleGroup(difficultyToggle);
			intermediateButton.selectedProperty().addListener(e -> {
				errFeedback.setText("Intermediate selected, click ok");
				resetTextFields();
			});
			BorderPane intermediateBorder = new BorderPane();
			Label intermediateLabel = new Label("Intermediate");
			intermediateBorder.setOnMouseClicked(e -> {
				intermediateButton.setSelected(true);
				resetTextFields();
			});
			intermediateLabel.getStyleClass().add("radio-label");
			intermediateBorder.setCenter(intermediateLabel);
			intermediateBorder.setLeft(intermediateButton);

			RadioButton expertButton = new RadioButton();
			expertButton.setToggleGroup(difficultyToggle);
			expertButton.selectedProperty().addListener(e -> {
				errFeedback.setText("Expert selected, click ok");
				resetTextFields();
			});
			BorderPane expertBorder = new BorderPane();
			Label expertLabel = new Label("Expert");
			expertBorder.setOnMouseClicked(e -> {
				expertButton.setSelected(true);
				resetTextFields();
			});
			expertLabel.getStyleClass().add("radio-label");
			expertBorder.setCenter(expertLabel);
			expertBorder.setLeft(expertButton);

			RadioButton customButton = new RadioButton();
			customButton.setToggleGroup(difficultyToggle);
			customButton.selectedProperty().addListener(e -> {
				errFeedback.setText("Select game parameters:");
			});
			BorderPane customBorder = new BorderPane();
			Label customLabel = new Label("Custom");
			customBorder.setOnMouseClicked(e -> {
				customButton.setSelected(true);
			});
			customLabel.getStyleClass().add("radio-label");
			customBorder.setCenter(customLabel);
			customBorder.setLeft(customButton);

			VBox radioPane = new VBox();
			radioPane.getChildren().addAll(beginnerBorder, intermediateBorder, expertBorder, customBorder);

			VBox textFieldPane = new VBox();
			textFieldPane.setAlignment(Pos.CENTER);

			HBox setWidth = new HBox();
			Label widthLabel = new Label("");

			HBox setHeight = new HBox();
			Label heightLabel = new Label("");

			HBox setMines = new HBox();
			Label mineLabel = new Label("");

			widthField.setAlignment(Pos.CENTER);
			widthField.setOnMouseClicked(e -> {
				resetHeightField();
				resetMineTextField();
				customButton.setSelected(true);
				widthField.setText("");
			});
			setWidth.getChildren().addAll(widthLabel, widthField);

			heightField.setAlignment(Pos.CENTER);
			heightField.setOnMouseClicked(e -> {
				resetWidthField();
				resetMineTextField();
				customButton.setSelected(true);
				heightField.setText("");
			});
			setHeight.getChildren().addAll(heightLabel, heightField);

			mineTextField.setAlignment(Pos.CENTER);
			mineTextField.setOnMouseClicked(e -> {
				resetWidthField();
				resetHeightField();
				customButton.setSelected(true);
				mineTextField.setText("");
			});
			setMines.getChildren().addAll(mineLabel, mineTextField);

			Button confirmSelectionButton = new Button("OK");
			confirmSelectionButton.getStyleClass().add("okay-button");

			BorderPane okayAreaPane = new BorderPane();
			okayAreaPane.setCenter(confirmSelectionButton);
			okayAreaPane.setPadding(new Insets(PAD, 0, PAD, 0));

			BorderPane lowerPane = new BorderPane();
			lowerPane.setCenter(bevelPane(okayAreaPane, OUTERBEVEL, BEVELLAYERS));
			lowerPane.setPadding(new Insets(10, 40, 10, 40));

			EventHandler<MouseEvent> selectionEvent = ev -> {

				RadioButton radioGroupSelection = (RadioButton) difficultyToggle.getSelectedToggle();
				boolean clear = false;

				if (radioGroupSelection == null) {
					errFeedback.setText("Please make a selection");
				} else {

					if (radioGroupSelection.equals(beginnerButton)) {
						newGame(BEGINNER);
						customDifficulty.hide();
						clear = true;
					} else if (radioGroupSelection.equals(intermediateButton)) {
						newGame(INTERMEDIATE);
						customDifficulty.hide();
						clear = true;
					} else if (radioGroupSelection.equals(expertButton)) {
						newGame(EXPERT);
						customDifficulty.hide();
						clear = true;
					} else if (radioGroupSelection.equals(customButton)) {

						try {

							InputHandler.notEmptyOrThrow(widthField.getText());
							InputHandler.notEmptyOrThrow(heightField.getText());
							InputHandler.notEmptyOrThrow(mineTextField.getText());

							InputHandler.isNumbersOrThrow(widthField.getText());
							InputHandler.isNumbersOrThrow(heightField.getText());
							InputHandler.isNumbersOrThrow(mineTextField.getText());

							InputHandler.noWhiteSpaceOrThrow(widthField.getText());
							InputHandler.noWhiteSpaceOrThrow(heightField.getText());
							InputHandler.noWhiteSpaceOrThrow(mineTextField.getText());

							int w = Integer.parseInt(widthField.getText());
							int h = Integer.parseInt(heightField.getText());
							int m = Integer.parseInt(mineTextField.getText());

							InputHandler.inRangeOrThrow("Width ", w, 7, 40);
							InputHandler.inRangeOrThrow("Height ", h, 7, 25);
							InputHandler.inRangeOrThrow("Mine ", m, 1, (w * h) / 2);

							newGame(new CustomGame(w, h, m));
							customDifficulty.hide();
							clear = true;
						} catch (Exception er) {
							errFeedback.setText(er.getMessage());
						}
					}
					if (clear) {
						radioGroupSelection.setSelected(false);
						widthField.setText("");
						heightField.setText("");
						mineTextField.setText("");
						errFeedback.setText("");
					}
				}
			};

			okayAreaPane.setOnMouseClicked(selectionEvent);

			textFieldPane.getChildren().addAll(setWidth, setHeight, setMines, lowerPane);

			confirmSelectionButton.setOnMouseClicked(selectionEvent);

			this.getChildren().addAll(difficultyTitle, bevelPane(radioPane, INNERBEVEL, BEVELLAYERS), errFeedback,
					bevelPane(textFieldPane, INNERBEVEL, BEVELLAYERS));

			this.setAlignment(Pos.CENTER);

		}

		private void resetTextFields() {
			resetWidthField();
			resetHeightField();
			resetMineTextField();
		}

		private void resetMineTextField() {
			if (mineTextField.getText().length() < 1) {
				mineTextField.setText(ENTERMINES);
			}
		}

		private void resetWidthField() {
			if (widthField.getText().length() < 1) {
				widthField.setText(ENTERWIDTH);
			}
		}

		private void resetHeightField() {
			if (heightField.getText().length() < 1) {
				heightField.setText(ENTERHEIGHT);
			}
		}

	}

	private void newGame(Difficulty s) {

		state = s;
		timeline.stop();
		numRevealed = 0;
		gameOver = false;
		firstClick = true;

		flagCounter.reset(state.getNMines());
		clock.reset(0);
		face.reset();

		fieldPane.getChildren().clear();
		fieldPane.getChildren().add(new MineField());

		stage.sizeToScene();

		// move window to the center of the screen
		stage.setX(((Screen.getScreens().get(0).getBounds().getMaxX()) / 2) - ((stage.getWidth()) / 2));
		stage.setY(((Screen.getScreens().get(0).getBounds().getMaxY()) / 2) - ((stage.getHeight()) / 2));

		// TODO delete this:
		// setupPlayerNameStage();

	}

	private class Face extends Button {

		private ImageView currentFace;

		Face() {

			currentFace = new ImageView(SMILE);
			int size = 52;
			currentFace.setFitWidth(size);
			currentFace.setFitHeight(size);

			setMinWidth(size);
			setMaxWidth(size);
			setMinHeight(size);
			setMaxHeight(size);

			this.setGraphic(currentFace);

			this.setOnMousePressed(e -> {
				currentFace.setImage(SMILEPRESSED);
			});

			this.setOnMouseReleased(e -> {
				currentFace.setImage(SMILE);
				newGame(state);
			});

		}

		private void reset() {
			currentFace.setImage(SMILE);
		}
	}

	private class Mine extends Button {
		private int x, y, adjMines;
		private boolean flagged, revealed, hasMine;
		private ImageView currentImage;
		private Image hiddenImage;

		Mine(int x, int y) {
			this.x = x;
			this.y = y;
			adjMines = 0;
			flagged = false;
			revealed = false;

			int size = 32;
			setMinWidth(size);
			setMaxWidth(size);
			setMinHeight(size);
			setMaxHeight(size);

			currentImage = new ImageView(EMPTYSPACE);

			currentImage.setFitHeight(size);
			currentImage.setFitWidth(size);
			setGraphic(currentImage);

			setOnMouseClicked(e -> {
				if (e.getButton() == MouseButton.PRIMARY) {
					leftClick();
				} else if (e.getButton() == MouseButton.SECONDARY) {
					rightClick();
				} else if (e.getButton() == MouseButton.MIDDLE) {
					middleClick();
				}
			});

			this.setOnMousePressed(e -> {
				if (!gameOver)
					face.currentFace.setImage(OFACE);
			});

			this.setOnMouseReleased(e -> {
				if (!gameOver)
					face.currentFace.setImage(SMILE);
			});
		}

		private void leftClick() {
			if (!gameOver && !revealed && !flagged) {

				if (firstClick) {
					timeline.play();
					generateMines(this.x, this.y);
					firstClick = false;
				}

				if (!hasMine) {
					reveal();
					recursiveReveal(this.x, this.y);
					resetFlags();
				} else {
					gameLost();
					this.currentImage.setImage(REDMINE);
				}
			}
		}

		private void rightClick() {
			if (!gameOver && !revealed) {
				if (flagged) {
					flagged = false;
					flagCounter.tick();
					currentImage.setImage(EMPTYSPACE);
				} else {
					if (flagCounter.value > 0) {
						flagged = true;
						currentImage.setImage(FLAG);
						flagCounter.antiTick();
					}
				}
			}
		}

		private void middleClick() {

			int numFlags = 0;
			if (this.adjMines > 0) {
				for (int i = ((x == 0) ? 0 : -1); i <= ((x == state.getWidth() - 1) ? 0 : 1); i++) {
					for (int j = ((y == 0) ? 0 : -1); j <= ((y == state.getHeight() - 1) ? 0 : 1); j++) {
						if (mines[this.x + i][this.y + j].flagged) {
							numFlags++;
						}
					}
				}
			}
			if (numFlags == adjMines) {
				for (int i = ((x == 0) ? 0 : -1); i <= ((x == state.getWidth() - 1) ? 0 : 1); i++) {
					for (int j = ((y == 0) ? 0 : -1); j <= ((y == state.getHeight() - 1) ? 0 : 1); j++) {
						if (!mines[this.x + i][this.y + j].revealed && !mines[this.x + i][this.y + j].flagged) {
							mines[this.x + i][this.y + j].reveal();
							if (mines[this.x + i][this.y + j].hasMine) {
								gameLost();
								mines[this.x + i][this.y + j].currentImage.setImage(REDMINE);
							}
						}
					}
				}
			}
		}

		private void reveal() {
			currentImage.setImage(hiddenImage);
			if (!revealed) {
				numRevealed++;
				if (!gameOver && numRevealed == (state.getWidth() * state.getHeight()) - state.getNMines()) {
					gameWon();
				}
			}

			revealed = true;
		}

	}

	private class MineField extends GridPane {
		MineField() {
			mines = new Mine[state.getWidth()][state.getHeight()];

			for (int i = 0; i < state.getWidth(); i++) {
				for (int j = 0; j < state.getHeight(); j++) {
					mines[i][j] = new Mine(i, j);
					this.add(mines[i][j], i, j);
				}
			}
			setVgap(0);
			setHgap(0);
		}

	}

	private void generateMines(int fx, int fy) {

		int nMines = state.getNMines();
		int x, y;

		// this generates randomly placed mines
		while (nMines > 0) {
			x = (int) (Math.random() * state.getWidth());
			y = (int) (Math.random() * state.getHeight());
			if (!mines[x][y].hasMine && Math.sqrt(Math.pow(x - fx, 2) + Math.pow(y - fy, 2)) >= 1.5) {
				mines[x][y].hasMine = true;
				nMines--;
			}
		}

		// this generates the number of adjacent mines
		for (int i = 0; i < state.getWidth(); i++) {
			for (int j = 0; j < state.getHeight(); j++) {

				for (int m = ((i == 0) ? 0 : -1); m <= ((i == state.getWidth() - 1) ? 0 : 1); m++) {
					for (int n = ((j == 0) ? 0 : -1); n <= ((j == state.getHeight() - 1) ? 0 : 1); n++) {

						if (mines[i + m][j + n].hasMine) {
							mines[i][j].adjMines++;
						}
					}
				}

				if (!mines[i][j].hasMine) {
					mines[i][j].hiddenImage = getMineImage(mines[i][j].adjMines);
				} else {
					mines[i][j].hiddenImage = MINE;

					// TODO remove this. its so i can test highscores
					// mines[i][j].currentImage.setImage(MINE);

				}
			}
		}

	}

	private void recursiveReveal(int x, int y) {

		if (mines[x][y].adjMines > 0 || mines[x][y].hasMine) {
			return;
		}

		for (int i = ((x == 0) ? 0 : -1); i <= ((x == state.getWidth() - 1) ? 0 : 1); i++) {
			for (int j = ((y == 0) ? 0 : -1); j <= ((y == state.getHeight() - 1) ? 0 : 1); j++) {
				if (mines[x + i][y + j].adjMines == 0 && !mines[x + i][y + j].revealed) {
					mines[x + i][y + j].reveal();
					recursiveReveal(x + i, y + j);
				} else {
					mines[x + i][y + j].reveal();

				}
			}
		}

	}

	private void resetFlags() {
		for (int i = 0; i < state.getWidth(); i++) {
			for (int j = 0; j < state.getHeight(); j++) {
				if (mines[i][j].flagged) {
					mines[i][j].revealed = false;
					mines[i][j].currentImage.setImage(FLAG);
				}
			}
		}
	}

	private void revealAll() {
		for (int i = 0; i < state.getWidth(); i++) {
			for (int j = 0; j < state.getHeight(); j++) {
				mines[i][j].reveal();
				if (mines[i][j].hasMine) {
					if (mines[i][j].flagged) {
						mines[i][j].hiddenImage = FLAG;
					} else {
						mines[i][j].hiddenImage = MINE;
					}
				} else {
					if (mines[i][j].flagged) {
						mines[i][j].hiddenImage = XMINE;

					}
				}
			}
		}
	}

	private class Counter extends HBox {

		private Integer value;
		ImageView digit1th, digit10th, digit100th;

		Counter() {

			int w = 13 * 2;
			int h = 24 * 2;
			w = 28;
			h = 52;

			digit1th = new ImageView(getDigitImage(0));
			digit10th = new ImageView(getDigitImage(0));
			digit100th = new ImageView(getDigitImage(0));

			digit1th.setFitWidth(w);
			digit1th.setFitHeight(h);

			digit10th.setFitWidth(w);
			digit10th.setFitHeight(h);

			digit100th.setFitWidth(w);
			digit100th.setFitHeight(h);

			getChildren().addAll(digit100th, digit10th, digit1th);
		}

		private int getValue() {
			return value;
		}

		private void reset(int n) {
			value = n;
			setDigits();
		}

		private void tick() {
			this.value++;
			if (this.value > 999) {
				this.value = 999;
			}
			this.setDigits();
		}

		private void antiTick() {
			this.value--;
			if (this.value < 0) {
				this.value = 0;
			}
			this.setDigits();
		}

		private void setDigits() {
			if (this.value < 10) {
				digit1th.setImage(getDigitImage(this.value));
				digit10th.setImage(getDigitImage(0));
				digit100th.setImage(getDigitImage(0));
			} else if (this.value < 100) {
				digit1th.setImage(getDigitImage(this.value.toString().charAt(1)));
				digit10th.setImage(getDigitImage(this.value.toString().charAt(0)));
				digit100th.setImage(getDigitImage(0));
			} else if (this.value < 1000) {
				digit1th.setImage(getDigitImage(this.value.toString().charAt(2)));
				digit10th.setImage(getDigitImage(this.value.toString().charAt(1)));
				digit100th.setImage(getDigitImage(this.value.toString().charAt(0)));
			}
		}

	}

	private interface Difficulty {
		int getWidth();

		int getHeight();

		int getNMines();

		String getName();

		scoreDifficulties getDifficulty();

	}

	private class BeginnerGame implements Difficulty {
		@Override
		public int getWidth() {
			return 8;
		}

		@Override
		public int getHeight() {
			return 8;
		}

		@Override
		public int getNMines() {
			return 10;
		}

		@Override
		public String getName() {
			return "Beginner";
		}

		@Override
		public scoreDifficulties getDifficulty() {
			return scoreDifficulties.BEGINNER;
		}
	}

	private class IntermediateGame implements Difficulty {
		@Override
		public int getWidth() {
			return 16;
		}

		@Override
		public int getHeight() {
			return 16;
		}

		@Override
		public int getNMines() {
			return 40;
		}

		@Override
		public String getName() {
			return "Intermediate";
		}

		@Override
		public scoreDifficulties getDifficulty() {
			return scoreDifficulties.INTERMEDIATE;
		}
	}

	private class ExpertGame implements Difficulty {
		@Override
		public int getWidth() {
			return 32;
		}

		@Override
		public int getHeight() {
			return 16;
		}

		@Override
		public int getNMines() {
			return 99;
		}

		@Override
		public String getName() {
			return "Expert";
		}

		@Override
		public scoreDifficulties getDifficulty() {
			return scoreDifficulties.EXPERT;
		}
	}

	private class CustomGame implements Difficulty {

		private int width, height, mines;

		CustomGame(int w, int h, int m) {
			width = w;
			height = h;
			mines = m;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getNMines() {
			return mines;
		}

		@Override
		public String getName() {
			return "" + width + "x" + height + ": " + mines;
		}

		@Override
		public scoreDifficulties getDifficulty() {
			return scoreDifficulties.CUSTOM;
		}

	}

}
