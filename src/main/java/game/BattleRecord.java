package game;

import java.sql.Timestamp;

public class BattleRecord {
    private final String player1;
    private final String player2;
    private final String winner;
    private final String gameMode;
    private final Timestamp battleDate;

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

    @Override
    public String toString() {
        return String.format("%s vs %s | Winner: %s | Mode: %s | Date: %s",
                player1, player2, winner, gameMode, battleDate);
    }
}