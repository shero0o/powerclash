package at.fhv.spiel_service.service.game.manager.projectile;

import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.domain.Projectile;
import at.fhv.spiel_service.domain.ProjectileType;

import java.util.List;

public interface ProjectileManager {

    void spawnProjectile(String playerId,
                         Position position,
                         Position direction,
                         ProjectileType type);

    /**
     * Called each frame with delta-Sekunden, um alle Projektil-Updates zu machen.
     */
    void updateProjectiles(float deltaSec);

    /**
     * Liefert die aktuelle Liste aller aktiven Projektile.
     */
    List<Projectile> getProjectiles();

    /**
     * Bei State-Updates: Ammo-/Waffenstatus für einen Player ziehen.
     */
    int getCurrentAmmo(String playerId);
    ProjectileType getCurrentWeapon(String playerId);

    int getMaxAmmoForType(ProjectileType type);

    void initPlayer(String playerId, ProjectileType initialWeapon);
    void removeProjectile(String playerId);
    void setWeapon(String playerId, ProjectileType weapon);

    void removeProjectileById(String projectileId);
}
