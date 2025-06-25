package at.fhv.spiel_service.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Crate implements Entity {

    private final String id;
    private final Position position;
    private int currentHealth;
    private boolean wasHit;

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
}
