package at.fhv.spiel_service.service.game.manager.projectile.behavior;

import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.domain.Projectile;
import at.fhv.spiel_service.domain.ProjectileType;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileContext;

public class MineBehavior implements ProjectileBehavior {
    private static final float SPEED = 750f;
    private static final int   DAMAGE = 40;
    private static final float MAX_RANGE = 700f;
    private static final long  ARM_DELAY = 2000L;

    @Override
    public void spawn(String playerId, Position pos, Position dir, ProjectileType type, ProjectileContext ctx) {
        long now = ctx.now();
        Projectile p = new Projectile(
                playerId + "-" + now, playerId,
                new Position(pos.getX(), pos.getY(), pos.getAngle()),
                new Position(dir.getX(), dir.getY(), 0),
                SPEED, DAMAGE, now, ProjectileType.MINE, MAX_RANGE, 0f
        );
        p.setArmTime(now + ARM_DELAY);
        p.setArmed(false);
        ctx.addProjectile(p);
    }

    @Override
    public void update(Projectile p, ProjectileContext ctx) {
        if (!p.isArmed() && ctx.now() >= p.getArmTime()) {
            p.setArmed(true);
            return;
        }
        doMovementAndRange(p, ctx, SPEED, MAX_RANGE);
    }
}
