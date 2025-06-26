package at.fhv.spiel_service.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Brawler implements Entity {
    String id;
    int level;
    int currentHealth;
    int maxHealth;
    Position position;
    boolean visible = true;

    public Brawler(String id, int level, int maxHealth, Position pos) {
        this.id = id;
        this.level = level;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.position = pos;
    }

    @Override public Position getPosition() { return position; }
    @Override public void setPosition(Position position) { this.position = position; }
}
