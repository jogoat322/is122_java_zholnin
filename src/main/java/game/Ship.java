package game;

import igame.IShip;

public class Ship implements IShip {
    private int size;
    private boolean isSunk;

    public Ship(int size) {
        this.size = size;
        this.isSunk = false;
    }

    public int getSize() {
        return size;
    }

    public boolean isSunk() {
        return isSunk;
    }

    public void hit() {
        size--;
        if (size <= 0) {
            isSunk = true;
        }
    }
}