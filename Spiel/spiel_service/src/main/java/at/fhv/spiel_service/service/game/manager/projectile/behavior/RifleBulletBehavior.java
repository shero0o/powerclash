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
        doMovementAndRange(p, ctx, SPEED, MAX_RANGE);
    }
}
