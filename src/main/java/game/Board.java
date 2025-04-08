package game;

import igame.IBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board implements IBoard {
    private final int[][] grid;
    private static final int SIZE = 10;
    private final Random random;
    private final List<ShipInfo> ships; // Список кораблей с их координатами и ориентацией

    public Board() {
        grid = new int[SIZE][SIZE];
        random = new Random();
        ships = new ArrayList<>();
    }

    // Внутренний класс для хранения информации о корабле
    private static class ShipInfo {
        Ship ship;
        int x; // Начальная координата X
        int y; // Начальная координата Y
        boolean isVertical; // Ориентация корабля

        ShipInfo(Ship ship, int x, int y, boolean isVertical) {
            this.ship = ship;
            this.x = x;
            this.y = y;
            this.isVertical = isVertical;
        }
    }

    public boolean isCellAttacked(int x, int y) {
        return grid[x][y] == 2 || grid[x][y] == 3 || grid[x][y] == 4;
    }

    public boolean placeShip(Ship ship, int x, int y, boolean isVertical) {
        int size = ship.getSize();
        if (isVertical) {
            if (y + size > SIZE) return false;
            for (int i = y - 1; i < y + size + 1; i++) {
                for (int j = x - 1; j <= x + 1; j++) {
                    if (i >= 0 && i < SIZE && j >= 0 && j < SIZE && grid[j][i] != 0) {
                        return false;
                    }
                }
            }
            for (int i = y; i < y + size; i++) {
                grid[x][i] = 1;
            }
        } else {
            if (x + size > SIZE) return false;
            for (int i = x - 1; i < x + size + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if (i >= 0 && i < SIZE && j >= 0 && j < SIZE && grid[i][j] != 0) {
                        return false;
                    }
                }
            }
            for (int i = x; i < x + size; i++) {
                grid[i][y] = 1;
            }
        }
        ships.add(new ShipInfo(ship, x, y, isVertical)); // Сохраняем информацию о корабле
        return true;
    }

    public void placeShipsRandomly() {
        int[] shipSizes = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                int x = random.nextInt(SIZE);
                int y = random.nextInt(SIZE);
                boolean isVertical = random.nextBoolean();
                Ship ship = new Ship(size);
                placed = placeShip(ship, x, y, isVertical);
            }
        }
    }

    public boolean receiveAttack(int x, int y) {
        if (isCellAttacked(x, y)) {
            return false;
        }
        if (grid[x][y] == 1) {
            grid[x][y] = 2;
            checkIfShipSunk(x, y);
            return true;
        } else if (grid[x][y] == 0) {
            grid[x][y] = 3;
            return false;
        }
        return false;
    }

    private void checkIfShipSunk(int x, int y) {
        for (ShipInfo shipInfo : ships) {
            if (!shipInfo.ship.isSunk()) {
                int size = shipInfo.ship.getSize();
                int startX = shipInfo.x;
                int startY = shipInfo.y;
                boolean isVertical = shipInfo.isVertical;

                // Проверяем, попадает ли атака в этот корабль
                boolean isHitOnShip = false;
                if (isVertical) {
                    if (x == startX && y >= startY && y < startY + size) {
                        isHitOnShip = true;
                    }
                } else {
                    if (y == startY && x >= startX && x < startX + size) {
                        isHitOnShip = true;
                    }
                }

                if (isHitOnShip) {
                    // Проверяем, все ли клетки корабля подбиты
                    int hits = 0;
                    if (isVertical) {
                        for (int i = startY; i < startY + size; i++) {
                            if (grid[startX][i] == 2) {
                                hits++;
                            }
                        }
                    } else {
                        for (int i = startX; i < startX + size; i++) {
                            if (grid[i][startY] == 2) {
                                hits++;
                            }
                        }
                    }

                    // Если все клетки подбиты, помечаем корабль как потопленный
                    if (hits == size) {
                        shipInfo.ship.hit(); // Уменьшаем размер до 0 и помечаем как потопленный
                        if (isVertical) {
                            for (int i = startY; i < startY + size; i++) {
                                if (grid[startX][i] == 2) {
                                    grid[startX][i] = 4; // Бордовый цвет для потопленного корабля
                                }
                            }
                        } else {
                            for (int i = startX; i < startX + size; i++) {
                                if (grid[i][startY] == 2) {
                                    grid[i][startY] = 4; // Бордовый цвет для потопленного корабля
                                }
                            }
                        }
                    }
                    break; // Выходим из цикла после обработки корабля
                }
            }
        }
    }

    public boolean areAllShipsSunk() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] getGrid() {
        return grid;
    }



}