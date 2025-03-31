package igame;

public interface IPlayer {
    boolean makeMove(int x, int y);
    IBoard  getBoard();
}
