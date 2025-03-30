package game;

import igame.IMainMenu;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;

public class MainMenu implements IMainMenu {
    private final Stage primaryStage;

    public MainMenu(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void show() {
        File file = new File("src/main/resources/world-of-warships-1k7yh.jpg");
        Image backgroundImage = new Image(file.toURI().toString());
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setFitWidth(primaryStage.getWidth());
        backgroundView.setFitHeight(primaryStage.getHeight());
        backgroundView.setPreserveRatio(false);

        VBox menuBox = new VBox(40);
        menuBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button playButtonPvE = new Button("Играть против компьютера");
        playButtonPvE.setMinSize(300, 80);
        playButtonPvE.setStyle("-fx-font-size: 24px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        playButtonPvE.setOnAction(e -> startGamePvE());

        Button playButtonPvP = new Button("Играть против игрока");
        playButtonPvP.setMinSize(300, 80);
        playButtonPvP.setStyle("-fx-font-size: 24px; -fx-background-color: #FF9800; -fx-text-fill: white;");
        playButtonPvP.setOnAction(e -> startGamePvP());

        Button exitButton = new Button("Выйти");
        exitButton.setMinSize(300, 80);
        exitButton.setStyle("-fx-font-size: 24px; -fx-background-color: #f44336; -fx-text-fill: white;");
        exitButton.setOnAction(e -> primaryStage.close());

        menuBox.getChildren().addAll(playButtonPvE, playButtonPvP, exitButton);

        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundView, menuBox);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Главное меню");
        primaryStage.setFullScreen(true); // Устанавливаем полноэкранный режим для меню
        primaryStage.show();
    }

    private void startGamePvE() {
        GameController gameController = new GameController();
        gameController.startGame(primaryStage, false);
    }

    private void startGamePvP() {
        GameController gameController = new GameController();
        gameController.startGame(primaryStage, true);
    }
}