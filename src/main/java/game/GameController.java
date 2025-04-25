package game;

import igame.IGameController;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import java.sql.Timestamp;
import java.util.Optional;

public class GameController implements IGameController {
    private Board player1Board;
    private Board player2Board;
    private Player player1;
    private Player player2;
    private Computer computer;
    private GameView gameView;
    private boolean isPlayer1Turn = true;
    private final int[] shipSizes = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
    private int currentShipIndex = 0;
    private boolean isPvPMode = false;
    private boolean isFirstPlayerPlacing = true;
    private String player1Name;
    private String player2Name;

    public GameController() {
    }

    @Override
    public void startGame(Stage primaryStage, boolean pvpMode) {
        isPvPMode = pvpMode;
        currentShipIndex = 0;
        isPlayer1Turn = true;
        isFirstPlayerPlacing = true;

        player1Name = getPlayerName("Введите имя Игрока 1", "Игрок 1");
        player1Board = new Board();

        if (isPvPMode) {
            player2Name = getPlayerName("Введите имя Игрока 2", "Игрок 2");
            player2Board = new Board();
            player1 = new Player(player1Board, player2Board);
            player2 = new Player(player2Board, player1Board);
            gameView = new GameView(primaryStage, player1, player2, this);

            // Начальная настройка доступности полей
            gameView.setPlayer1GridClickable(false); // Свое поле не кликабельно
            gameView.setPlayer2GridClickable(true);  // Поле противника кликабельно
        } else {
            player2Name = "Компьютер";
            player2Board = new Board();
            player2Board.placeShipsRandomly();
            player1 = new Player(player1Board, player2Board);
            computer = new Computer(player2Board, player1Board);
            gameView = new GameView(primaryStage, player1, computer, this);

            // Начальная настройка доступности полей
            gameView.setPlayer1GridClickable(false);
            gameView.setPlayer2GridClickable(true);
        }

        gameView.initialize();
        gameView.startShipPlacement();
    }

    private String getPlayerName(String title, String defaultName) {
        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText("Введите имя игрока:");

        Optional<String> result = dialog.showAndWait();
        return result.filter(name -> !name.trim().isEmpty()).orElse(defaultName);
    }

    @Override
    public int getCurrentShipSize() {
        if (currentShipIndex < shipSizes.length) {
            return shipSizes[currentShipIndex];
        }
        return -1;
    }

    @Override
    public boolean placePlayerShip(int x, int y, boolean isVertical) {
        if (currentShipIndex < shipSizes.length) {
            Ship ship = new Ship(shipSizes[currentShipIndex]);
            Board currentBoard = isPvPMode && isFirstPlayerPlacing ? player1Board : player1Board;
            if (isPvPMode && !isFirstPlayerPlacing) {
                currentBoard = player2Board;
            }

            if (currentBoard.placeShip(ship, x, y, isVertical)) {
                currentShipIndex++;
                gameView.updateGrid();
                if (currentShipIndex == shipSizes.length) {
                    if (isPvPMode && isFirstPlayerPlacing) {
                        isFirstPlayerPlacing = false;
                        currentShipIndex = 0;
                        gameView.hidePlayer1Grid();
                        gameView.switchToSecondPlayerPlacement();
                    } else if (isPvPMode && !isFirstPlayerPlacing) {
                        gameView.hidePlayer2Grid();
                        gameView.showMessage("Игра началась", "Ход " + player1Name + "!");
                    } else {
                        gameView.startGame();
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public void handlePlayerMove(int x, int y) {
        if (currentShipIndex < shipSizes.length) {
            showError("Сначала разместите все корабли!");
            return;
        }

        if (gameView != null && gameView.isGameEnded()) {
            showError("Игра завершена. Начните новую игру или выйдите.");
            return;
        }

        if (isPvPMode) {
            // Определяем какое поле должно быть кликабельным
            gameView.setPlayer1GridClickable(!isPlayer1Turn);
            gameView.setPlayer2GridClickable(isPlayer1Turn);

            Board targetBoard = isPlayer1Turn ? player2Board : player1Board;
            String currentPlayer = isPlayer1Turn ? player1Name : player2Name;
            String nextPlayer = isPlayer1Turn ? player2Name : player1Name;

            if (!targetBoard.isCellWithinBounds(x, y)) {
                showError("Выстрел за пределы поля!");
                return;
            }

            if (targetBoard.isCellAttacked(x, y)) {
                showError("Вы уже стреляли в эту клетку!");
                return;
            }

            boolean isHit = targetBoard.receiveAttack(x, y);
            gameView.updateGrid();

            if (targetBoard.areAllShipsSunk()) {
                gameView.revealAllShips();
                saveBattleResult(currentPlayer);
                gameView.showMessage("Победа!", currentPlayer + " потопил все корабли противника!");
                return;
            }

            if (!isHit) {
                isPlayer1Turn = !isPlayer1Turn;
                // Обновляем доступность полей при смене хода
                gameView.setPlayer1GridClickable(!isPlayer1Turn);
                gameView.setPlayer2GridClickable(isPlayer1Turn);
                gameView.showMessage("Промах", "Ход переходит к " + nextPlayer + "!");
            } else {
                gameView.showMessage("Попадание", currentPlayer + " стреляет снова!");
            }
        } else {
            // PvE-режим (остается без изменений)
            gameView.setPlayer1GridClickable(false); // Свое поле не кликабельно
            gameView.setPlayer2GridClickable(true);  // Поле компьютера кликабельно

            if (!player2Board.isCellWithinBounds(x, y)) {
                showError("Выстрел за пределы поля!");
                return;
            }

            if (player2Board.isCellAttacked(x, y)) {
                showError("Вы уже стреляли в эту клетку!");
                return;
            }

            boolean isHit = player1.makeMove(x, y);
            gameView.updateGrid();

            if (player2Board.areAllShipsSunk()) {
                gameView.revealAllShips();
                saveBattleResult(player1Name);
                gameView.showMessage("Победа!", player1Name + " потопил все корабли противника!");
                return;
            }

            if (!isHit) {
                isPlayer1Turn = false;
                computer.makeMoveUntilMiss();
                gameView.updateGrid();

                if (player1Board.areAllShipsSunk()) {
                    gameView.revealAllShips();
                    saveBattleResult(player2Name);
                    gameView.showMessage("Поражение", "Все корабли " + player1Name + " потоплены!");
                    return;
                }

                isPlayer1Turn = true;
                gameView.showMessage("Промах", "Ход " + player1Name + "!");
            }
        }
    }

    private void saveBattleResult(String winner) {
        String gameMode = isPvPMode ? "PvP" : "PvE";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // Создаем новую запись боя
        BattleRecord record = new BattleRecord(
                player1Name,
                player2Name,
                winner,
                gameMode,
                timestamp
        );

        // Сохраняем запись
        record.save();
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public boolean isPvPMode() {
        return isPvPMode;
    }

    @Override
    public boolean isFirstPlayerPlacing() {
        return isFirstPlayerPlacing;
    }

    public boolean isPlayer1Turn() {
        return isPlayer1Turn;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public boolean isGameEnded() {
        return gameView != null && gameView.isGameEnded();
    }
}