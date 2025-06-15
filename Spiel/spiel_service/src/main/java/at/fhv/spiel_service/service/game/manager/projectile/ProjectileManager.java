package at.fhv.spiel_service.service.game.manager.projectile;

import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.domain.Projectile;
import at.fhv.spiel_service.domain.ProjectileType;

import java.util.List;

public interface ProjectileManager {

    void spawnProjectile(String playerId, Position position, Position direction, ProjectileType type);
    void updateProjectiles(float deltaSec);
    List<Projectile> getProjectiles();
    int getCurrentAmmo(String playerId);
    ProjectileType getCurrentWeapon(String playerId);
    void initPlayer(String playerId, ProjectileType initialWeapon);
    void setWeapon(String playerId, ProjectileType weapon);
    void removeProjectileById(String projectileId);
}
