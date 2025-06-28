package at.fhv.spiel_service.service.game.manager.projectile.behavior;

import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.domain.Projectile;
import at.fhv.spiel_service.domain.ProjectileType;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileContext;

public interface ProjectileBehavior {
    void spawn(String playerId, Position pos, Position dir, ProjectileType type, ProjectileContext ctx);
    void update(Projectile p, ProjectileContext ctx);
    default void doMovementAndRange(Projectile p, ProjectileContext ctx,
                                    float speed, float maxRange) {
        float delta = ctx.deltaSec();
        float dx = p.getDirection().getX();
        float dy = p.getDirection().getY();
        float len = (float)Math.hypot(dx, dy);
        float nx = len>0? dx/len : 0;
        float ny = len>0? dy/len : 0;

        float dist = speed * delta;
        p.getPosition().setX(p.getPosition().getX() + nx*dist);
        p.getPosition().setY(p.getPosition().getY() + ny*dist);
        p.setTravelled(p.getTravelled() + dist);
        if (p.getTravelled() >= maxRange) {
            ctx.removeProjectileById(p.getId());
            return;
        }

        int tx = (int)(p.getPosition().getX()/ctx.getGameMap().getTileWidth());
        int ty = (int)(p.getPosition().getY()/ctx.getGameMap().getTileHeight());
        if (ctx.getGameMap().isWallAt(tx, ty)) {
            ctx.removeProjectileById(p.getId());
        }
    }
}
