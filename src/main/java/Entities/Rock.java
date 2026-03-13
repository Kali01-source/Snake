package Entities;

import Misc.*;

public class Rock implements Entity {
    private final Position position;
    @Override
    public Position getPosition() {
        return position;
    }

    public Rock(Position position) {
        this.position = position;
    }
}
