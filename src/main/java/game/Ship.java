package game;

public class Ship {
    private int size;
    private int hits;

    public Ship(int size) {
        this.size = size;
        this.hits = 0;
    }

    public int getSize() {
        return size;
    }

    public void hit() {
        hits++;
    }

    public boolean isSunk() {
        return hits >= size;
    }
}