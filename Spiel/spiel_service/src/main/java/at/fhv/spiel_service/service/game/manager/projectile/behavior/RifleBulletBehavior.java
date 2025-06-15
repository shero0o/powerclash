package at.fhv.spiel_service.service.game.manager.projectile.behavior;

import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.domain.Projectile;
import at.fhv.spiel_service.domain.ProjectileType;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileContext;

public class RifleBulletBehavior implements ProjectileBehavior {
    private static final float SPEED = 1000f;
    private static final int   DAMAGE = 2;
    private static final float MAX_RANGE = 2000f;

    @Override
    public void spawn(String playerId, Position pos, Position dir, ProjectileType type, ProjectileContext ctx) {
        long t = ctx.now();
        Projectile p = new Projectile(
                playerId + "-" + t, playerId,
                new Position(pos.getX(), pos.getY(), pos.getAngle()),
                new Position(dir.getX(), dir.getY(), 0),
                SPEED, DAMAGE, t,
                ProjectileType.RIFLE_BULLET, MAX_RANGE, 0f
        );
        ctx.addProjectile(p);
    }

    @Override
    public void update(Projectile p, ProjectileContext ctx) {
        // einfache Bewegung + Range-Cleanup
        float delta = ctx.deltaSec();
        float len = (float)Math.hypot(p.getDirection().getX(), p.getDirection().getY());
        float nx = len>0? p.getDirection().getX()/len : 0;
        float ny = len>0? p.getDirection().getY()/len : 0;
        float dist = SPEED*delta;
        p.getPosition().setX(p.getPosition().getX() + nx*dist);
        p.getPosition().setY(p.getPosition().getY() + ny*dist);
        p.setTravelled(p.getTravelled() + dist);
        if (p.getTravelled() >= p.getMaxRange()) {
            ctx.removeProjectileById(p.getId());
        }
        // Wand-Kollision
        int tx = (int)(p.getPosition().getX()/ctx.getGameMap().getTileWidth());
        int ty = (int)(p.getPosition().getY()/ctx.getGameMap().getTileHeight());
        if (ctx.getGameMap().isWallAt(tx, ty)) {
            ctx.removeProjectileById(p.getId());
        }
    }
}
