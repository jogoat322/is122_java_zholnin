package game;

import igame.IMainMenu;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.File;
import java.sql.Timestamp;
import java.util.List;

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

        // Общий стиль для всех кнопок
        String buttonStyle = "-fx-font-size: 24px; " +
                "-fx-background-color: white; " +
                "-fx-text-fill: #2196F3; " +
                "-fx-border-color: #2196F3; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px;";

        Button playButtonPvE = new Button("Играть против пк");
        playButtonPvE.setMinSize(300, 80);
        playButtonPvE.setStyle(buttonStyle);
        playButtonPvE.setOnAction(e -> startGamePvE());

        Button playButtonPvP = new Button("Играть против игрока");
        playButtonPvP.setMinSize(300, 80);
        playButtonPvP.setStyle(buttonStyle);
        playButtonPvP.setOnAction(e -> startGamePvP());

        Button historyButton = new Button("История боев");
        historyButton.setMinSize(300, 80);
        historyButton.setStyle(buttonStyle);
        historyButton.setOnAction(e -> showBattleHistory());

        Button statsButton = new Button("Статистика игроков");
        statsButton.setMinSize(300, 80);
        statsButton.setStyle(buttonStyle);
        statsButton.setOnAction(e -> showPlayerStats());

        Button exitButton = new Button("Выйти");
        exitButton.setMinSize(300, 80);
        exitButton.setStyle(buttonStyle);
        exitButton.setOnAction(e -> primaryStage.close());

        menuBox.getChildren().addAll(playButtonPvE, playButtonPvP, historyButton, statsButton, exitButton);

        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundView, menuBox);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Главное меню");
        primaryStage.setFullScreen(true);
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

    private void showBattleHistory() {
        Stage historyStage = new Stage();
        historyStage.setTitle("История боев");

        // Создаем TableView для отображения истории боев
        TableView<BattleRecord> historyTable = new TableView<>();
        historyTable.setPlaceholder(new javafx.scene.control.Label("История боев пуста"));

        // Определяем колонки таблицы
        TableColumn<BattleRecord, String> player1Column = new TableColumn<>("Игрок 1");
        player1Column.setCellValueFactory(new PropertyValueFactory<>("player1"));
        player1Column.setPrefWidth(150);

        TableColumn<BattleRecord, String> player2Column = new TableColumn<>("Игрок 2");
        player2Column.setCellValueFactory(new PropertyValueFactory<>("player2"));
        player2Column.setPrefWidth(150);

        TableColumn<BattleRecord, String> winnerColumn = new TableColumn<>("Победитель");
        winnerColumn.setCellValueFactory(new PropertyValueFactory<>("winner"));
        winnerColumn.setPrefWidth(150);

        TableColumn<BattleRecord, String> gameModeColumn = new TableColumn<>("Режим игры");
        gameModeColumn.setCellValueFactory(new PropertyValueFactory<>("gameMode"));
        gameModeColumn.setPrefWidth(100);

        TableColumn<BattleRecord, Timestamp> dateColumn = new TableColumn<>("Дата боя");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("battleDate"));
        dateColumn.setPrefWidth(150);

        // Добавляем колонки в таблицу
        historyTable.getColumns().addAll(player1Column, player2Column, winnerColumn, gameModeColumn, dateColumn);

        // Заполняем таблицу данными
        List<BattleRecord> battles = DatabaseManager.getBattleHistory();
        historyTable.getItems().addAll(battles);

        // Стиль для кнопки "Назад"
        String backButtonStyle = "-fx-font-size: 16px; " +
                "-fx-background-color: white; " +
                "-fx-text-fill: #2196F3; " +
                "-fx-border-color: #2196F3; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px;";

        Button backButton = new Button("Назад");
        backButton.setStyle(backButtonStyle);
        backButton.setOnAction(e -> historyStage.close());

        VBox historyBox = new VBox(20);
        historyBox.setAlignment(javafx.geometry.Pos.CENTER);
        historyBox.setPadding(new javafx.geometry.Insets(20));
        historyBox.getChildren().addAll(historyTable, backButton);

        Scene historyScene = new Scene(historyBox, 700, 500);
        historyStage.setScene(historyScene);
        historyStage.show();
    }

    private void showPlayerStats() {
        Stage statsStage = new Stage();
        statsStage.setTitle("Статистика игроков");

        // Создаем TableView для отображения статистики игроков
        TableView<PlayerStats> statsTable = new TableView<>();
        statsTable.setPlaceholder(new javafx.scene.control.Label("Статистика игроков пуста"));

        // Определяем колонки таблицы
        TableColumn<PlayerStats, String> playerNameColumn = new TableColumn<>("Имя игрока");
        playerNameColumn.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        playerNameColumn.setPrefWidth(200);

        TableColumn<PlayerStats, Integer> winsColumn = new TableColumn<>("Победы");
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));
        winsColumn.setPrefWidth(100);

        TableColumn<PlayerStats, Integer> lossesColumn = new TableColumn<>("Поражения");
        lossesColumn.setCellValueFactory(new PropertyValueFactory<>("losses"));
        lossesColumn.setPrefWidth(100);

        // Добавляем колонки в таблицу
        statsTable.getColumns().addAll(playerNameColumn, winsColumn, lossesColumn);

        // Заполняем таблицу данными
        List<PlayerStats> stats = DatabaseManager.getPlayerStats();
        statsTable.getItems().addAll(stats);

        // Стиль для кнопки "Назад"
        String backButtonStyle = "-fx-font-size: 16px; " +
                "-fx-background-color: white; " +
                "-fx-text-fill: #2196F3; " +
                "-fx-border-color: #2196F3; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px;";

        Button backButton = new Button("Назад");
        backButton.setStyle(backButtonStyle);
        backButton.setOnAction(e -> statsStage.close());

        VBox statsBox = new VBox(20);
        statsBox.setAlignment(javafx.geometry.Pos.CENTER);
        statsBox.setPadding(new javafx.geometry.Insets(20));
        statsBox.getChildren().addAll(statsTable, backButton);

        Scene statsScene = new Scene(statsBox, 500, 500);
        statsStage.setScene(statsScene);
        statsStage.show();
    }
}