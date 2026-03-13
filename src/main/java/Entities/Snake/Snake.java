package Entities.Snake;

import java.util.ArrayList;
import Misc.*;

public class Snake {
    private final ArrayList<SnakeSegment> segments;
    Direction direction;

    public Snake(int startX, int startY) {
        direction = Direction.values()[(int) (Math.random()*4)]; //véletlenszerű irány
        segments = new ArrayList<>();
        segments.add(new SnakeHead(new Position(startX, startY))); //fej
        switch (direction) { //csörgő
            case Direction.UP -> segments.add(new SnakeRattle(new Position(startX, startY-1)));
            case Direction.DOWN -> segments.add(new SnakeRattle(new Position(startX, startY+1)));
            case Direction.RIGHT -> segments.add(new SnakeRattle(new Position(startX-1, startY)));
            case Direction.LEFT -> segments.add(new SnakeRattle(new Position(startX+1, startY)));
            default -> throw new IllegalArgumentException();
        }
    }

    public void move() {
        for (int i = segments.size() - 1; i >= 1; i--) {
            Position prev = segments.get(i - 1).getPosition();
            segments.get(i).setPosition(new Position(prev.getX(), prev.getY()));
        }
        switch (direction) {
            case Direction.UP -> segments.getFirst().getPosition().moveUp();
            case Direction.DOWN -> segments.getFirst().getPosition().moveDown();
            case Direction.RIGHT -> segments.getFirst().getPosition().moveRight();
            case Direction.LEFT -> segments.getFirst().getPosition().moveLeft();
            default -> throw new IllegalArgumentException();
        }
    }

    public boolean checkSelfCollision() {
        Position p = segments.getFirst().getPosition();
        for (int i = 1; i < segments.size(); i++) {
            if (p.equals(segments.get(i).getPosition())) {
                return true;
            }
        }
        return false;
    }

    public Direction getDirection() { return direction; }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public ArrayList<SnakeSegment> getSegments() {
        return segments;
    }
}
