package game;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // Для Firebird (RedExpert)
    private static final String DB_URL = "jdbc:firebirdsql://localhost:3050/C:\\Users\\Andrey\\Desktop\\3kurs\\sea_batl_30\\src\\main\\resources\\JAVA.FDB";
    private static final String USER = "SYSDBA";
    private static final String PASS = "zxc";

    static {
        try {
            // Регистрируем драйвер Firebird
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load Firebird JDBC driver");
        }
    }

    public static void saveGameResult(String player1, String player2, String winner, String gameMode, Timestamp timestamp) {
        String sql = "INSERT INTO BATTLE_HISTORY (PLAYER1, PLAYER2, WINNER, GAME_MODE, BATTLE_DATE) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, player1);
            stmt.setString(2, player2);
            stmt.setString(3, winner);
            stmt.setString(4, gameMode);
            stmt.setTimestamp(5, timestamp);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<BattleRecord> getBattleHistory() {
        List<BattleRecord> history = new ArrayList<>();
        String sql = "SELECT * FROM BATTLE_HISTORY ORDER BY BATTLE_DATE DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BattleRecord record = new BattleRecord(
                        rs.getString("PLAYER1"),
                        rs.getString("PLAYER2"),
                        rs.getString("WINNER"),
                        rs.getString("GAME_MODE"),
                        rs.getTimestamp("BATTLE_DATE")
                );
                history.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }
}