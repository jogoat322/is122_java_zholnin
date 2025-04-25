package game;

import igame.IGameView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

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
    private final Label orientationLabel;
    private boolean isPlacingShips = false;
    private int currentShipSize = -1;
    private boolean isVertical = false;
    private boolean gameEnded = false;

    private static final Color EMPTY_CELL_COLOR = Color.WHITE;
    private static final Color SHIP_COLOR = Color.BLACK;
    private static final Color HIT_COLOR = Color.RED;
    private static final Color MISS_COLOR = Color.BLUE;
    private static final Color SUNK_COLOR = Color.rgb(102, 0, 51);




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
        orientationLabel = new Label("Ориентация: Горизонтально");
    }

    public GameView(Stage primaryStage, Player player1, Player player2, GameController gameController) {
        this.primaryStage = primaryStage;
        this.player1 = player1;
        this.player2 = player2;
        this.computer = null;
        this.gameController = gameController;
        player1Grid = new GridPane();
        player2Grid = new GridPane();
        player1Label = new Label("Поле " + gameController.getPlayer1Name() + ":");
        player2Label = new Label("Поле " + gameController.getPlayer2Name() + ":");
        orientationLabel = new Label("Ориентация: Горизонтально");
    }

    public boolean isGameEnded() {
        return gameEnded;
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

        VBox root = new VBox(20, hbox, orientationLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight()); // Фиксированный размер окна
        primaryStage.setScene(scene);
        primaryStage.setTitle("Морской бой");
        primaryStage.centerOnScreen(); // Центрируем окно
        primaryStage.show();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.Z && isPlacingShips) {
                isVertical = !isVertical;
                orientationLabel.setText("Ориентация: " + (isVertical ? "Вертикально" : "Горизонтально"));
                updateShipPreview();
            }
        });
    }

    @Override
    public void startShipPlacement() {
        isPlacingShips = true;
        currentShipSize = gameController.getCurrentShipSize();
        orientationLabel.setVisible(true);
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
        orientationLabel.setVisible(false);
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
                if (gameController.isFirstPlayerPlacing() || !gameController.isPvPMode() || gameEnded) {
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
                } else if (gameEnded) {
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

        if (title.equals("Победа!") || title.equals("Поражение")) {
            ButtonType playAgainButton = new ButtonType("Играть снова");
            ButtonType exitButton = new ButtonType("Выйти");
            alert.getButtonTypes().setAll(playAgainButton, exitButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == playAgainButton) {
                    returnToMainMenu();
                } else if (response == exitButton) {
                    primaryStage.close();
                }
            });
        } else {
            alert.showAndWait();
        }
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

    public void revealAllShips() {
        gameEnded = true;
        updateGrid();
        disableGrids();
    }

    private void disableGrids() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button cell = (Button) player1Grid.getChildren().get(i * 10 + j);
                cell.setDisable(true);
                cell.setOnAction(null);
                cell.setOnMouseMoved(null);
                cell.setOnMouseClicked(null);
            }
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button cell = (Button) player2Grid.getChildren().get(i * 10 + j);
                cell.setDisable(true);
                cell.setOnAction(null);
                cell.setOnMouseMoved(null);
                cell.setOnMouseClicked(null);
            }
        }
    }

    private void returnToMainMenu() {
        MainMenu mainMenu = new MainMenu(primaryStage);
        mainMenu.show();
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

                if (isClickable && !gameEnded) {
                    final int x = i;
                    final int y = j;
                    cell.setOnAction(e -> {
                        gameController.handlePlayerMove(x, y);
                    });
                } else if (board[i][j] == 1) {
                    cell.setStyle(getColorStyle(SHIP_COLOR));
                } else {
                    cell.setStyle(getColorStyle(EMPTY_CELL_COLOR));
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
                    if (isPlacingShips && !gameEnded) {
                        showShipPreview(grid, x, y, currentShipSize, isVertical);
                    }
                });

                cell.setOnMouseClicked(e -> {
                    if (isPlacingShips && !gameEnded) {
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
            case 0:
                cell.setStyle(getColorStyle(EMPTY_CELL_COLOR));
                break;
            case 1:
                cell.setStyle(getColorStyle(SHIP_COLOR));
                break;
            case 2:
                cell.setStyle(getColorStyle(HIT_COLOR));
                break;
            case 3:
                cell.setStyle(getColorStyle(MISS_COLOR));
                break;
            case 4:
                cell.setStyle(getColorStyle(SUNK_COLOR));
                break;
        }
    }

    private String getColorStyle(Color color) {
        return String.format("-fx-background-color: #%02X%02X%02X; -fx-font-size: 12;",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void showShipPreview(GridPane grid, int x, int y, int shipSize, boolean isVertical) {
        clearPreview(grid);

        boolean canPlace = true;
        int[][] board = (grid == player1Grid) ? player1.getBoard().getGrid() : player2.getBoard().getGrid();

        // 1. Проверяем возможность размещения
        for (int i = 0; i < shipSize; i++) {
            int previewX = x + (isVertical ? 0 : i);
            int previewY = y + (isVertical ? i : 0);

            if (previewX >= 10 || previewY >= 10) {
                canPlace = false;
                break;
            }

            if (board[previewX][previewY] != 0) {
                canPlace = false;
                // Не прерываем проверку, чтобы найти все занятые клетки
            }

            // Проверяем соседние клетки
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = previewX + dx;
                    int ny = previewY + dy;
                    if (nx >= 0 && nx < 10 && ny >= 0 && ny < 10 && board[nx][ny] != 0) {
                        canPlace = false;
                    }
                }
            }
        }

        Color borderColor = canPlace ? Color.LIGHTGREEN : Color.PINK;

        // 2. Подсвечиваем только пустые клетки светло-серым
        for (int i = 0; i < shipSize; i++) {
            int previewX = x + (isVertical ? 0 : i);
            int previewY = y + (isVertical ? i : 0);

            if (previewX < 10 && previewY < 10) {
                Button shipCell = (Button) grid.getChildren().get(previewX * 10 + previewY);
                // Подсвечиваем только пустые клетки
                if (board[previewX][previewY] == 0) {
                    shipCell.setStyle(getColorStyle(Color.LIGHTGRAY));
                } else {
                    // Для занятых клеток сохраняем их текущий стиль
                    updateCellStyle(shipCell, board[previewX][previewY]);
                }
            }
        }

        // 3. Подсвечиваем границу вокруг всего корабля (только пустые клетки)
        Set<String> borderCells = new HashSet<>();

        for (int i = 0; i < shipSize; i++) {
            int shipX = x + (isVertical ? 0 : i);
            int shipY = y + (isVertical ? i : 0);

            if (shipX >= 10 || shipY >= 10) continue;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;

                    int borderX = shipX + dx;
                    int borderY = shipY + dy;

                    if (borderX >= 0 && borderX < 10 && borderY >= 0 && borderY < 10) {
                        // Проверяем, что это не часть самого корабля
                        boolean isShipCell = false;
                        for (int j = 0; j < shipSize; j++) {
                            int checkX = x + (isVertical ? 0 : j);
                            int checkY = y + (isVertical ? j : 0);
                            if (borderX == checkX && borderY == checkY) {
                                isShipCell = true;
                                break;
                            }
                        }

                        if (!isShipCell && board[borderX][borderY] == 0) {
                            borderCells.add(borderX + "," + borderY);
                        }
                    }
                }
            }
        }

        // Подсвечиваем граничные клетки
        for (String cell : borderCells) {
            String[] coords = cell.split(",");
            int bx = Integer.parseInt(coords[0]);
            int by = Integer.parseInt(coords[1]);

            Button borderCell = (Button) grid.getChildren().get(bx * 10 + by);
            borderCell.setStyle(getColorStyle(borderColor));
        }
    }

    private void updateShipPreview() {
        GridPane grid = gameController.isFirstPlayerPlacing() ? player1Grid : player2Grid;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button cell = (Button) grid.getChildren().get(i * 10 + j);
                int cellState = (grid == player1Grid) ? player1.getBoard().getGrid()[i][j]
                        : player2.getBoard().getGrid()[i][j];
                if (cellState == 0) {
                    cell.setStyle(getColorStyle(EMPTY_CELL_COLOR));
                }
            }
        }
    }

    private void clearPreview(GridPane grid) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button cell = (Button) grid.getChildren().get(i * 10 + j);
                int cellState = (grid == player1Grid) ? player1.getBoard().getGrid()[i][j]
                        : player2.getBoard().getGrid()[i][j];
                if (cellState == 0) {
                    cell.setStyle(getColorStyle(EMPTY_CELL_COLOR));
                }
            }
        }
    }
    public void setPlayer1GridClickable(boolean clickable) {
        // Реализация для игрового поля игрока 1
        for (Node node : player1Grid.getChildren()) {
            node.setMouseTransparent(!clickable);
        }
    }

    public void setPlayer2GridClickable(boolean clickable) {
        // Реализация для игрового поля игрока 2
        for (Node node : player2Grid.getChildren()) {
            node.setMouseTransparent(!clickable);
        }
    }
}