package at.fhv.spiel_backend.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Chest implements Entity {
    String id;
    Position position;
    int currentHealth;
    int maxHealth;

    /**
     * Inflicts damage; returns true if destroyed.
     */
    public boolean damage(int amount) {
        currentHealth -= amount;
        return currentHealth <= 0;
    }
}
