package at.fhv.spiel_backend.model;


import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GameMap {
    String id;
    int[][] walls;
    List<Grass> grasses;
    List<Chest> chests;
    List<Player> players;
    List<Projectile> projectiles;
    SafeZone safeZone;

    /**
     * Updates global state (e.g., safe zone, projectiles).
     */
    public void update(float deltaSeconds) {
        // shrink safe zone
        if (safeZone != null) safeZone.update(deltaSeconds);
        // move projectiles
        projectiles.forEach(p -> p.update(deltaSeconds));
        // cleanup expired projectiles omitted
    }
}
