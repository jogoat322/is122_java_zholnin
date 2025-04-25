package game;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    // URL подключения к SQLite
    private static final String DB_URL = "jdbc:sqlite:./battlehistory/battlehistory.db";
    private static Connection connection;

    static {
        try {
            // Регистрируем драйвер SQLite
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Не удалось загрузить драйвер JDBC SQLite", e);
            throw new RuntimeException("Не удалось загрузить драйвер JDBC SQLite", e);
        }
    }

    private static void initializeDatabase() {
        try {
            // Создаем директорию battlehistory, если она не существует
            Files.createDirectories(Paths.get("./battlehistory"));

            // Подключаемся к SQLite
            LOGGER.info("Подключение к базе данных SQLite: " + DB_URL);
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            createTableIfNotExists();
            LOGGER.info("База данных SQLite успешно инициализирована");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Не удалось инициализировать базу данных SQLite", e);
            throw new RuntimeException("Не удалось инициализировать базу данных SQLite()", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка при создании директории или удалении файла базы данных", e);
            throw new RuntimeException("Ошибка при создании директории или удалении файла базы данных", e);
        }
    }

    private static void createTableIfNotExists() throws SQLException {
        String createBattleHistoryTableSQL = """
        CREATE TABLE IF NOT EXISTS battle_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            player1 TEXT,
            player2 TEXT,
            winner TEXT,
            game_mode TEXT,
            battle_date TIMESTAMP
        )
    """;

        String createPlayerStatsTableSQL = """
        CREATE TABLE IF NOT EXISTS player_stats (
            player_name TEXT PRIMARY KEY,
            wins INTEGER DEFAULT 0,
            losses INTEGER DEFAULT 0
        )
    """;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createBattleHistoryTableSQL);
            stmt.executeUpdate(createPlayerStatsTableSQL);
            LOGGER.info("Таблицы успешно созданы");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при создании таблиц", e);
            throw e;
        }
    }

    public static void saveGameResult(String player1, String player2, String winner, String gameMode, Timestamp timestamp) {
        String sql = "INSERT INTO battle_history (player1, player2, winner, game_mode, battle_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, player1);
            stmt.setString(2, player2);
            stmt.setString(3, winner);
            stmt.setString(4, gameMode);
            stmt.setTimestamp(5, timestamp);
            stmt.executeUpdate();
            LOGGER.fine("Результат игры успешно сохранен");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при сохранении результата игры", e);
        }
    }

    public static List<BattleRecord> getBattleHistory() {
        List<BattleRecord> history = new ArrayList<>();
        String sql = "SELECT * FROM battle_history ORDER BY battle_date DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BattleRecord record = new BattleRecord(
                        rs.getString("player1"),
                        rs.getString("player2"),
                        rs.getString("winner"),
                        rs.getString("game_mode"),
                        rs.getTimestamp("battle_date")
                );
                history.add(record);
            }
            LOGGER.fine("История боев успешно получена");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при получении истории боев", e);
        }
        return history;
    }

    public static List<PlayerStats> getPlayerStats() {
        List<PlayerStats> stats = new ArrayList<>();
        String sql = "SELECT player_name, wins, losses FROM player_stats ORDER BY wins DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                PlayerStats stat = new PlayerStats(
                        rs.getString("player_name"),
                        rs.getInt("wins"),
                        rs.getInt("losses")
                );
                stats.add(stat);
            }
            LOGGER.fine("Статистика игроков успешно получена");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при получении статистики игроков", e);
        }
        return stats;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Соединение с базой данных закрыто");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Ошибка при закрытии соединения с базой данных", e);
        }
    }

    public static void updatePlayerStats(String winner, String loser) {
        // SQL для проверки существования записи
        String selectSQL = "SELECT 1 FROM player_stats WHERE player_name = ?";
        // SQL для вставки новой записи
        String insertSQL = "INSERT INTO player_stats (player_name, wins, losses) VALUES (?, ?, ?)";
        // SQL для обновления побед
        String updateWinnerSQL = "UPDATE player_stats SET wins = wins + 1 WHERE player_name = ?";
        // SQL для обновления поражений
        String updateLoserSQL = "UPDATE player_stats SET losses = losses + 1 WHERE player_name = ?";

        try {
            // Обновляем статистику победителя
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
                selectStmt.setString(1, winner);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    // Игрок существует, обновляем победы
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateWinnerSQL)) {
                        updateStmt.setString(1, winner);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Игрок не существует, вставляем новую запись
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
                        insertStmt.setString(1, winner);
                        insertStmt.setInt(2, 1); // 1 победа
                        insertStmt.setInt(3, 0); // 0 поражений
                        insertStmt.executeUpdate();
                    }
                }
            }

            // Обновляем статистику проигравшего (если это не компьютер)
            if (!loser.equals("Компьютер")) {
                try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
                    selectStmt.setString(1, loser);
                    ResultSet rs = selectStmt.executeQuery();
                    if (rs.next()) {
                        // Игрок существует, обновляем поражения
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateLoserSQL)) {
                            updateStmt.setString(1, loser);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        // Игрок не существует, вставляем новую запись
                        try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
                            insertStmt.setString(1, loser);
                            insertStmt.setInt(2, 0); // 0 побед
                            insertStmt.setInt(3, 1); // 1 поражение
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }

            LOGGER.fine("Статистика игроков обновлена");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при обновлении статистики игроков", e);
        }
    }
}