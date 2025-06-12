package at.fhv.spiel_service.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Crate implements Entity {

    private final String id;
    private final Position position;
    private int currentHealth = 100;
    private boolean wasHit = false;

    public Crate(String id, Position position) {
        this.id = id;
        this.position = position;
        this.currentHealth = 100;
        this.wasHit = false;
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public void setPosition(Position position) {

    }
}
