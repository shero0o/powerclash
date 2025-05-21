package at.fhv.spiel_backend.ws;

import at.fhv.spiel_backend.model.Position;
import at.fhv.spiel_backend.model.ProjectileType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerState {
    private String playerId;
    private Position position;
    private int currentHealth;
    private boolean visible;
    private int ammo;
    private ProjectileType currentWeapon;
    private String brawlerId; // NEU

}
