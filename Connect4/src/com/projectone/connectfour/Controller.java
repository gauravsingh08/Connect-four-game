package com.projectone.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	private static final int CIRCLE_DIAMETER = 80;
	private static final String discColor1 = "#24303E";
	private static final String discColor2 = "#4CAA88";

	private static String PLAYER_ONE = "Player One";
	private static String PLAYER_TWO = "Player Two";

	private boolean isPlayerOneTurn = true;
	private boolean isAllowedToInsert = true;

	private Disc[][] insertedDiscArray = new Disc[ROWS][COLUMNS];  // For Structural Changes: For Developers

	@FXML
	public GridPane rootGridPane;

	@FXML
	public Pane insertedDiscsPane;

	@FXML
	public Label playerNameLabel;

	@FXML
	public TextField playerOneTextField;

	@FXML
	public TextField playerTwoTextField;

	@FXML
	public Button setNamesButton, stopMusicButton;

	//adding rectangleWithHoles and Rectangle list for hover over effect.
	public void createPlayground() {

		setNamesButton.setOnAction(event -> {
			String input1 = playerOneTextField.getText().toUpperCase();
			String input2 = playerTwoTextField.getText().toUpperCase();

			PLAYER_ONE = input1;
			PLAYER_TWO = input2;

			playerNameLabel.setText(input1);

		});

		Shape rectangleWithHoles = createGameStructuralGrid();
		rootGridPane.add(rectangleWithHoles, 0, 1);

		List<Rectangle> rectangle = createClickableColumns();

		for (Rectangle r : rectangle) {
			rootGridPane.add(r, 0, 1);
		}


	}


	// giving rectangle with holes
	private Shape createGameStructuralGrid() {

		Shape rectangleWithHoles = new Rectangle((COLUMNS + 1) * CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);

		for (int row = 0; row < ROWS; row++) {

			for (int col = 0; col < COLUMNS; col++) {
				Circle circle = new Circle();
				circle.setRadius(CIRCLE_DIAMETER / 2);
				circle.setCenterX(CIRCLE_DIAMETER / 2);
				circle.setCenterY(CIRCLE_DIAMETER / 2);
				circle.setSmooth(true);

				circle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);
				circle.setTranslateY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);

				rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle);
			}
		}

		rectangleWithHoles.setFill(Color.WHITE);

		return rectangleWithHoles;
	}


	//create clickAbleColums and calling  insertDisc method
	private List<Rectangle> createClickableColumns() {

		List<Rectangle> rectangleList = new ArrayList<>();

		for (int col = 0; col < COLUMNS; col++) {
			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER); // height same as rectangle with hole.
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4); // same as setTranslateX of circle.

			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			// on click event to add disc in require column.
			final int column = col;
			rectangle.setOnMouseClicked(event -> {
				if (isAllowedToInsert){
					isAllowedToInsert=false;
					insertDisc(new Disc(isPlayerOneTurn), column);
				}

			});

			rectangleList.add(rectangle);

		}

		return rectangleList;
	}


	//insertDisc in column by checking emptyspace in row to add disc .
	// animation on disc.
	//changing name of players.
	//changing color.(isPlayerOneTurn != isPlayerOneTurn).
	private void insertDisc(Disc disc, int column) {

		int row = ROWS - 1;

		while (row >= 0) {
			if (getDiscIfPresent(row, column) == null)
				break;

			row--;
		}

		if (row < 0) {
			return;
		}

		insertedDiscArray[row][column] = disc;   // for structural change(developer).
		insertedDiscsPane.getChildren().add(disc); // for visual change(user).See that disc are adding on pane not gridPane.

		disc.setTranslateX(column * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4); // same as position of circle(as hole) in rectangle.


		// animation for entering disc
		int currentRow = row;

		TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), disc);
		transition.setToY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);  //(tricky part)

		transition.setOnFinished(event -> {

			if (gameEnded(currentRow, column)) {

				gameOver();
				return;
			}

			isPlayerOneTurn = !isPlayerOneTurn;

			isAllowedToInsert = true;
			playerNameLabel.setText(isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO);
		});

		transition.play();

	}


	//check if game is ended.
	private boolean gameEnded(int row, int column) {
		//vertical points
		List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3, row + 3)
				.mapToObj(r -> new Point2D(r, column))
				.collect(Collectors.toList());

		//Horizontal points
		List<Point2D> horizontalPoints = IntStream.rangeClosed(column - 3, column + 3)
				.mapToObj(col -> new Point2D(row, col))
				.collect(Collectors.toList());

		//Diagonal 1
		Point2D startPoint1 = new Point2D(row - 3, column + 3);
		List<Point2D> diagonal1Points = IntStream.rangeClosed(0, 6)
				.mapToObj(i -> startPoint1.add(i, -i))
				.collect(Collectors.toList());

		//Diagonal 2
		Point2D startPoint2 = new Point2D(row - 3, column - 3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0, 6)
				.mapToObj(i -> startPoint2.add(i, i))
				.collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints) || checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);

		return isEnded;
	}


	// check all the 4 combination
	private boolean checkCombinations(List<Point2D> points) {
		int chain = 0;
		for (Point2D point : points) {

			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();

			Disc disc = getDiscIfPresent(rowIndexForArray, columnIndexForArray);

			if (disc != null && disc.isPlayerOneMove == isPlayerOneTurn) {
				chain++;
				if (chain == 4) {
					return true;
				}
			} else {
				chain = 0;
			}
		}

		return false;

	}


	// return disc if present and null if not
	private Disc getDiscIfPresent(int row, int column) {
		if (row >= ROWS || row < 0 || column >= COLUMNS || column < 0)
			return null;

		return insertedDiscArray[row][column];
	}


	//gameover
	private void gameOver() {
		String winner = isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner is " + winner);
		alert.setContentText("Want to play again ?");

		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No, Exit");

		alert.getButtonTypes().setAll(yesBtn, noBtn);

		Platform.runLater(() -> {           //Because showAndWait is not allowed during animation or layout processing
			Optional<ButtonType> btnClicked = alert.showAndWait();

			if (btnClicked.isPresent() && btnClicked.get() == yesBtn) {
				resetGame();
			} else {
				Platform.exit();
				System.exit(0);
			}

		});

	}


	//reset game
	public void resetGame() {
		insertedDiscsPane.getChildren().clear();      // removing all the disc.

		for (int row = 0; row < insertedDiscArray.length; row++){   // making all elemts to null.
			for (int col = 0; col < insertedDiscArray[row].length; col++){

				insertedDiscArray[row][col] = null;
			}
		}

		isPlayerOneTurn = true;  // lets plyer start the game
		playerNameLabel.setText(PLAYER_ONE);   // reset the player game

		createPlayground(); //prepare fresh playground
	}


	//creating disc every time called and have colour according to player turn.
	//its is a class called nested class.
	private static class Disc extends Circle {

		private final boolean isPlayerOneMove;

		private Disc(boolean isPlayerOneMove) {
			this.isPlayerOneMove = isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER / 2);
			setFill(isPlayerOneMove ? Color.valueOf(discColor1) : Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER / 2);
			setCenterY(CIRCLE_DIAMETER / 2);
		}
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
