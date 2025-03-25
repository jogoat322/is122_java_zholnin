package game;

import igame.IGameView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

public class GameView implements IGameView {
    private final Stage primaryStage;
    private final Player player1;
    private final Player player2;
    private final Computer computer;
    private final GameController gameController;
    private final GridPane player1Grid;
    private final GridPane player2Grid;
    private final Label player1Label;
    private final Label player2Label;
    private boolean isPlacingShips = false;
    private int currentShipSize = -1;
    private boolean isVertical = false;

    public GameView(Stage primaryStage, Player player, Computer computer, GameController gameController) {
        this.primaryStage = primaryStage;
        this.player1 = player;
        this.player2 = null;
        this.computer = computer;
        this.gameController = gameController;
        player1Grid = new GridPane();
        player2Grid = new GridPane();
        player1Label = new Label("Ваше поле:");
        player2Label = new Label("Поле компьютера:");
    }

    public GameView(Stage primaryStage, Player player1, Player player2, GameController gameController) {
        this.primaryStage = primaryStage;
        this.player1 = player1;
        this.player2 = player2;
        this.computer = null;
        this.gameController = gameController;
        player1Grid = new GridPane();
        player2Grid = new GridPane();
        player1Label = new Label("Поле Игрока 1:");
        player2Label = new Label("Поле Игрока 2:");
    }

    @Override
    public void initialize() {
        setupGrid(player1Grid, player1.getBoard().getGrid(), false);
        if (gameController.isPvPMode()) {
            setupGrid(player2Grid, player2.getBoard().getGrid(), false);
        } else {
            setupGrid(player2Grid, new int[10][10], true);
        }

        HBox hbox = new HBox(20);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(20));

        VBox player1Box = new VBox(10, player1Label, addCoordinates(player1Grid, true));
        VBox player2Box = new VBox(10, player2Label, addCoordinates(player2Grid, false));
        hbox.getChildren().addAll(player1Box, player2Box);

        Scene scene = new Scene(hbox, 1000, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Морской бой");
        primaryStage.show();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.Z && isPlacingShips) {
                isVertical = !isVertical;
                showMessage("Ориентация корабля", isVertical ? "Вертикально" : "Горизонтально");
                updateShipPreview();
            }
        });
    }

    @Override
    public void startShipPlacement() {
        isPlacingShips = true;
        currentShipSize = gameController.getCurrentShipSize();
        if (gameController.isPvPMode()) {
            showMessage("Расстановка кораблей", "Игрок 1: Разместите ваши корабли на левом поле. Нажмите Z для изменения ориентации.");
            setupGridForShipPlacement(player1Grid);
        } else {
            showMessage("Расстановка кораблей", "Разместите ваши корабли на поле. Нажмите Z, чтобы изменить ориентацию.");
            setupGridForShipPlacement(player1Grid);
        }
    }

    public void switchToSecondPlayerPlacement() {
        isPlacingShips = true;
        currentShipSize = gameController.getCurrentShipSize();
        showMessage("Расстановка кораблей", "Игрок 2: Разместите ваши корабли на правом поле. Нажмите Z для изменения ориентации.");
        setupGridForShipPlacement(player2Grid);
    }

    @Override
    public void startGame() {
        if (gameController.getCurrentShipSize() != -1) {
            showError("Ошибка", "Не все корабли размещены!");
            return;
        }
        isPlacingShips = false;
        if (!gameController.isPvPMode()) {
            setupGrid(player2Grid, new int[10][10], true);
            showMessage("Игра началась", "Ваш ход!");
        }
    }

    @Override
    public void updateGrid() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button cell = (Button) player1Grid.getChildren().get(i * 10 + j);
                int cellState = player1.getBoard().getGrid()[i][j];
                if (gameController.isFirstPlayerPlacing() || !gameController.isPvPMode()) {
                    updateCellStyle(cell, cellState);
                } else {
                    updateCellStyle(cell, cellState == 2 || cellState == 3 || cellState == 4 ? cellState : 0);
                }
            }
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button cell = (Button) player2Grid.getChildren().get(i * 10 + j);
                int cellState = (player2 != null) ? player2.getBoard().getGrid()[i][j] : computer.getBoard().getGrid()[i][j];
                if (gameController.isPvPMode() && gameController.isFirstPlayerPlacing()) {
                    updateCellStyle(cell, 0);
                } else if (gameController.isPvPMode() && !gameController.isFirstPlayerPlacing() && isPlacingShips) {
                    updateCellStyle(cell, cellState);
                } else {
                    updateCellStyle(cell, cellState == 2 || cellState == 3 || cellState == 4 ? cellState : 0);
                }
            }
        }
    }

    @Override
    public void showMessage(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void hidePlayer1Grid() {
        setupGrid(player1Grid, new int[10][10], true);
    }

    public void hidePlayer2Grid() {
        setupGrid(player1Grid, new int[10][10], true);
        setupGrid(player2Grid, new int[10][10], true);
        isPlacingShips = false;
    }

    private GridPane addCoordinates(GridPane grid, boolean isPlayer) {
        GridPane wrapper = new GridPane();
        wrapper.setHgap(5);
        wrapper.setVgap(5);
        wrapper.setPadding(new Insets(10));

        for (char c = 'A'; c <= 'J'; c++) {
            Label label = new Label(String.valueOf(c));
            label.setMinSize(30, 30);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            wrapper.add(label, c - 'A' + 1, 0);
        }

        for (int i = 1; i <= 10; i++) {
            Label label = new Label(String.valueOf(i));
            label.setMinSize(30, 30);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            wrapper.add(label, 0, i);
        }

        wrapper.add(grid, 1, 1, 10, 10);
        return wrapper;
    }

    private void setupGrid(GridPane grid, int[][] board, boolean isClickable) {
        grid.getChildren().clear();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setPadding(new Insets(5));

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button cell = new Button();
                cell.setMinSize(30, 30);
                cell.setMaxSize(30, 30);

                if (isClickable) {
                    final int x = i;
                    final int y = j;
                    cell.setOnAction(e -> {
                        gameController.handlePlayerMove(x, y);
                    });
                } else if (board[i][j] == 1) {
                    cell.setStyle("-fx-background-color: black; -fx-font-size: 12;");
                }

                grid.add(cell, j, i);
            }
        }
    }

    private void setupGridForShipPlacement(GridPane grid) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button cell = (Button) grid.getChildren().get(i * 10 + j);
                final int x = i;
                final int y = j;

                cell.setOnMouseMoved(e -> {
                    if (isPlacingShips) {
                        showShipPreview(grid, x, y, currentShipSize, isVertical);
                    }
                });

                cell.setOnMouseClicked(e -> {
                    if (isPlacingShips) {
                        if (gameController.placePlayerShip(x, y, isVertical)) {
                            currentShipSize = gameController.getCurrentShipSize();
                            if (currentShipSize == -1 && !gameController.isPvPMode()) {
                                showMessage("Корабли размещены", "Игра начинается!");
                                startGame();
                            }
                        } else {
                            showError("Ошибка", "Невозможно разместить корабль здесь!");
                        }
                    }
                });
            }
        }
    }

    private void updateCellStyle(Button cell, int cellState) {
        switch (cellState) {
            case 0: // Пустая клетка
                cell.setStyle("-fx-background-color: white; -fx-font-size: 12;");
                break;
            case 1: // Корабль (живой)
                cell.setStyle("-fx-background-color: black; -fx-font-size: 12;");
                break;
            case 2: // Попадание
                cell.setStyle("-fx-background-color: red; -fx-font-size: 12;");
                break;
            case 3: // Промах
                cell.setStyle("-fx-background-color: blue; -fx-font-size: 12;");
                break;
            case 4: // Потопленный корабль (бордовый цвет)
                cell.setStyle("-fx-background-color: green; -fx-font-size: 12;");
                break;
        }
    }

    private void showShipPreview(GridPane grid, int x, int y, int shipSize, boolean isVertical) {
        clearPreview(grid);

        for (int i = 0; i < shipSize; i++) {
            int previewX = x + (isVertical ? 0 : i);
            int previewY = y + (isVertical ? i : 0);

            if (previewX < 10 && previewY < 10) {
                Button cell = (Button) grid.getChildren().get(previewX * 10 + previewY);
                int cellState = (grid == player1Grid) ? player1.getBoard().getGrid()[previewX][previewY] : player2.getBoard().getGrid()[previewX][previewY];
                if (cellState == 0) {
                    cell.setStyle("-fx-background-color: lightgray; -fx-font-size: 12;");
                }
            }
        }
    }

    private void updateShipPreview() {
        GridPane grid = gameController.isFirstPlayerPlacing() ? player1Grid : player2Grid;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button cell = (Button) grid.getChildren().get(i * 10 + j);
                int cellState = (grid == player1Grid) ? player1.getBoard().getGrid()[i][j] : player2.getBoard().getGrid()[i][j];
                if (cellState == 0) {
                    cell.setStyle("-fx-background-color: white; -fx-font-size: 12;");
                }
            }
        }
    }

    private void clearPreview(GridPane grid) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button cell = (Button) grid.getChildren().get(i * 10 + j);
                int cellState = (grid == player1Grid) ? player1.getBoard().getGrid()[i][j] : player2.getBoard().getGrid()[i][j];
                if (cellState == 0) {
                    cell.setStyle("-fx-background-color: white; -fx-font-size: 12;");
                }
            }
        }
    }
}