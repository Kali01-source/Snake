package Misc;

import Entities.*;

public class Cell {
    private final Position position;
    private Entity entity;

    public Cell(Position position, Entity entity) {
        this.position = position;
        this.entity = entity;
    }

    public Cell(Position position) {
        this.position = position;
        this.entity = null;
    }

    public Position getPosition() {
        return position;
    }
    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
