package igame;

import javafx.stage.Stage;

public interface IGameController {
    void startGame(Stage primaryStage, boolean pvpMode);
    boolean placePlayerShip(int x, int y, boolean isVertical);
    void handlePlayerMove(int x, int y);
    int getCurrentShipSize();
    boolean isPvPMode();
    boolean isFirstPlayerPlacing();
}