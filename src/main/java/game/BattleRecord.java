package game;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BattleRecord {
    private static final Logger LOGGER = Logger.getLogger(BattleRecord.class.getName());
    private final String player1;
    private final String player2;
    private final String winner;
    private final String gameMode;
    private final Timestamp battleDate;
    private static final String DB_URL = "jdbc:sqlite:./battlehistory/battlehistory.db";

    public BattleRecord(String player1, String player2, String winner, String gameMode, Timestamp battleDate) {
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
        this.gameMode = gameMode;
        this.battleDate = battleDate;
    }

    // Геттеры
    public String getPlayer1() { return player1; }
    public String getPlayer2() { return player2; }
    public String getWinner() { return winner; }
    public String getGameMode() { return gameMode; }
    public Timestamp getBattleDate() { return battleDate; }

    // Метод для сохранения записи в базу данных
    public void save() {
        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            String sql = "INSERT INTO battle_history (player1, player2, winner, game_mode, battle_date) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, player1 != null ? player1.trim() : "");
                stmt.setString(2, player2 != null ? player2.trim() : "");
                stmt.setString(3, winner != null ? winner.trim() : "");
                stmt.setString(4, gameMode != null ? gameMode.trim() : "");
                stmt.setTimestamp(5, battleDate);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    LOGGER.info("Запись боя успешно сохранена: " + this);
                } else {
                    LOGGER.warning("Не удалось сохранить запись боя: нет затронутых строк");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при сохранении записи боя: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s vs %s | Победитель: %s | Режим: %s | Дата: %s",
                player1, player2, winner, gameMode, battleDate);
    }
}