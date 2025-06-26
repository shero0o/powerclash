package at.fhv.spiel_service.service.game.manager.projectile.behavior;

import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.domain.Projectile;
import at.fhv.spiel_service.domain.ProjectileType;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileContext;

public class ShotgunBehavior implements ProjectileBehavior {
    private static final int    PELLETS = 5;
    private static final float  SPREAD_DEG = 10f;
    private static final float  SPEED = 800f;
    private static final int    DAMAGE = 5;
    private static final float  MAX_RANGE = 700f;

    @Override
    public void spawn(String playerId, Position pos, Position dir, ProjectileType type, ProjectileContext ctx) {
        double base = Math.atan2(dir.getY(), dir.getX());
        double step = SPREAD_DEG / (PELLETS-1);
        for(int i=0; i<PELLETS; i++){
            double offset = -SPREAD_DEG/2 + i*step;
            double ang = base + Math.toRadians(offset);
            Position d2 = new Position((float)Math.cos(ang),(float)Math.sin(ang),0);
            long now = ctx.now();
            Projectile p = new Projectile(
                    playerId + "-" + now + "-" + i, playerId,
                    new Position(pos.getX(),pos.getY(),pos.getAngle()), d2,
                    SPEED, DAMAGE, now, ProjectileType.SHOTGUN_PELLET, MAX_RANGE, 0f
            );
            ctx.addProjectile(p);
        }
    }

    @Override
    public void update(Projectile p, ProjectileContext ctx) {
        doMovementAndRange(p, ctx, SPEED, MAX_RANGE);
    }
}
