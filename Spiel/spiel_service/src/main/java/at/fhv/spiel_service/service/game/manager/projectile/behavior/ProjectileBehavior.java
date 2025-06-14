package at.fhv.spiel_service.service.game.manager.projectile.behavior;

import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.domain.Projectile;
import at.fhv.spiel_service.domain.ProjectileType;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileContext;

public interface ProjectileBehavior {
    void spawn(String playerId, Position pos, Position dir, ProjectileType type, ProjectileContext ctx);
    void update(Projectile p, ProjectileContext ctx);
}
