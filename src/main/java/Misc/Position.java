package Misc;

import java.util.Objects;

public class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int[] getPosition() {
        return new int[] {x, y};
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    public void moveUp() { y--; }
    public void moveDown() { y++; }
    public void moveRight() {
        x++;
    }
    public void moveLeft() {
        x--;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
