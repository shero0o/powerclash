package at.fhv.spiel_service.messaging;

import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.domain.ProjectileType;
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
    private String brawlerId;
    private String playerName;
    private int coinCount;
    private int maxHealth;
}
