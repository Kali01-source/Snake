package Entities.Snake;

import Misc.*;
import Entities.*;

public class SnakeSegment implements Entity {
    private Position position;
    @Override
    public Position getPosition() {
        return position;
    }
    public void setPosition(Position position) {
        this.position = position;
    }

    public SnakeSegment(Position position) {
        this.position = position;
    }
}

