package Entities;

import Misc.*;

public class Food implements Entity{
    private Position position;

    private Food() {}

    private static class Holder {
        private static final Food INSTANCE = new Food();
    }

    public static Food getInstance() {
        return Holder.INSTANCE;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public Position getPosition() {
        return position;
    }


}
