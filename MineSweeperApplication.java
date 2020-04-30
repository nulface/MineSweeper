package minesweeper;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class MineSweeperApplication extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(@SuppressWarnings("exports") Stage stage) throws Exception {

		stage.setTitle("Mine Sweeper");
		stage.setScene(new Scene(new MineSweeper(stage)));
		stage.show();

	}
}
