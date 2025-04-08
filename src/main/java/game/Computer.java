package game;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import igame.IComputer;

public class Computer implements IComputer {
    private final Board myBoard;
    private final Board opponentBoard;
    private final Random random;
    private final Queue<int[]> huntQueue; // Очередь координат для "охоты"
    private boolean huntingMode; // Режим охоты после попадания

    public Computer(Board myBoard, Board opponentBoard) {
        this.myBoard = myBoard;
        this.opponentBoard = opponentBoard;
        this.random = new Random();
        this.huntQueue = new ArrayDeque<>();
        this.huntingMode = false;
    }

    public Board getBoard() {
        return myBoard;
    }

    @Override
    public void makeMoveUntilMiss() {
        boolean isHit;
        do {
            int x, y;

            if (huntingMode && !huntQueue.isEmpty()) {
                // Режим охоты: берем координаты из очереди
                int[] coords = huntQueue.poll();
                x = coords[0];
                y = coords[1];
            } else {
                // Случайный режим: ищем новую цель
                do {
                    x = random.nextInt(10);
                    y = random.nextInt(10);
                } while (opponentBoard.isCellAttacked(x, y));
            }

            isHit = opponentBoard.receiveAttack(x, y);

            if (isHit && !opponentBoard.areAllShipsSunk()) {
                // Если попали, переходим в режим охоты
                huntingMode = true;
                addNeighborsToQueue(x, y);
            } else if (!isHit) {
                // Если промахнулись и очередь пуста, выходим из режима охоты
                if (huntQueue.isEmpty()) {
                    huntingMode = false;
                }
            }
        } while (isHit && !opponentBoard.areAllShipsSunk());
    }

    // Добавление соседних клеток в очередь для проверки
    private void addNeighborsToQueue(int x, int y) {
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Вниз, вправо, вверх, влево
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (isValidCoordinate(newX, newY) && !opponentBoard.isCellAttacked(newX, newY)) {
                huntQueue.offer(new int[]{newX, newY});
            }
        }
    }

    // Проверка валидности координат
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }
}