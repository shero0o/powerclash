package at.fhv.spiel_service.service.game.manager.projectile.behavior;

import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.domain.Projectile;
import at.fhv.spiel_service.domain.ProjectileType;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileContext;

public class SniperBehavior implements ProjectileBehavior {
    private static final float SPEED = 1400f;
    private static final int   DAMAGE = 30;
    private static final float MAX_RANGE = 2500f;

    @Override
    public void spawn(String playerId, Position pos, Position dir, ProjectileType type, ProjectileContext ctx) {
        long now = ctx.now();
        Projectile p = new Projectile(
                playerId + "-" + now, playerId,
                new Position(pos.getX(), pos.getY(), pos.getAngle()),
                new Position(dir.getX(), dir.getY(), 0),
                SPEED, DAMAGE, now, ProjectileType.SNIPER, MAX_RANGE, 0f
        );
        ctx.addProjectile(p);
    }

    @Override
    public void update(Projectile p, ProjectileContext ctx) {
        doMovementAndRange(p, ctx, SPEED, MAX_RANGE);
    }
}
