package at.fhv.spiel_backend.model;
import lombok.*;
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
    Attack attack;
    boolean visible = true;

    public Brawler(String id, Attack attack, int level, int maxHealth, Position pos) {
        this.id = id;
        this.attack = attack;
        this.level = level;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.position = pos;
    }

    /**
     * Checks if the brawler can attack based on cooldown and ammo.
     */
    public boolean canAttack() {
        return attack.canFire();
    }

    /**
     * Record an attack, consumes ammo and enforces cooldown.
     */
    public void recordAttack() {
        attack.recordFire();
    }

    @Override public Position getPosition() { return position; }
    @Override public void setPosition(Position position) { this.position = position; }
}
