package game;

public class PlayerStats {
    private final String playerName;
    private final int wins;
    private final int losses;

    public PlayerStats(String playerName, int wins, int losses) {
        this.playerName = playerName;
        this.wins = wins;
        this.losses = losses;
    }

    // Геттеры
    public String getPlayerName() {
        return playerName;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }
}