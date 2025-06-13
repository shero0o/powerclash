// src/main/java/at/fhv/spiel_service/services/managers/projectile/IProjectileService.java
package at.fhv.spiel_service.services.managers.projectile;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Projectile;
import at.fhv.spiel_service.services.managers.crate.ICrateService;
import at.fhv.spiel_service.services.managers.combat.ICombatService;

import java.util.Collection;
import java.util.Map;

public interface IProjectileService {
    void spawnProjectile(
            String playerId,
            Player player,
            float dirX,
            float dirY,
            String projectileType,
            Map<String, Projectile> projectiles
    );
    void updateProjectiles(
            float deltaSec,
            GameMap map,
            Map<String, Player> players,
            Map<String, Projectile> projectiles,
            ICrateService crateService,
            ICombatService combatService
    );
    /** liefert alle aktiven Projectiles zurück */
    Collection<Projectile> getAllProjectiles();
}
