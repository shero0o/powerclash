package at.fhv.spiel_service.service.game.manager.collision;

import at.fhv.spiel_service.domain.*;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileManager;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CollisionManagerImpl implements CollisionManager {
    private final ProjectileManager projectileManager;
    private final GameMap gameMap;

    public CollisionManagerImpl(ProjectileManager projectileManager, GameMap gameMap) {
        this.projectileManager = projectileManager;
        this.gameMap = gameMap;
    }

    @Override
    public void processCollisions(
            List<Projectile> projectiles,
            Map<String, Player> players,
            List<NPC> npcs,
            Map<String, Crate> crates,
            Map<String, Integer> playerCoins
    ) {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            String shooterId = p.getPlayerId();

            // 1) NPC-Treffer
            boolean hit = false;
            for (NPC npc : npcs) {
                if (npc.getCurrentHealth() <= 0) continue;
                float dx = npc.getPosition().getX() - p.getPosition().getX();
                float dy = npc.getPosition().getY() - p.getPosition().getY();
                if (Math.hypot(dx, dy) < 40f) {
                    npc.setCurrentHealth(Math.max(0, npc.getCurrentHealth() - p.getDamage()));
                    projectileManager.removeProjectile(p.getId());
                    hit = true;
                    break;
                }
            }
            if (hit) continue;

            // 2) Kisten-Treffer
            int tx = (int)(p.getPosition().getX() / gameMap.getTileWidth());
            int ty = (int)(p.getPosition().getY() / gameMap.getTileHeight());
            String key = tx + "," + ty;
            Crate crate = crates.get(key);
            if (crate != null) {
                crate.setWasHit(true);
                crate.setCurrentHealth(Math.max(0, crate.getCurrentHealth() - p.getDamage()));
                projectileManager.removeProjectile(p.getId());
                if (crate.getCurrentHealth() <= 0) {
                    crates.remove(key);
                    playerCoins.merge(shooterId, 10, Integer::sum);
                }
                continue;
            }

            if (gameMap.isWallAt(tx, ty)) {
                // Projektil zerstören
                projectileManager.removeProjectile(p.getId());
                it.remove();
                continue;
            }

            // 4) Spieler-Treffer
            for (Player target : players.values()) {
                if (target.getId().equals(shooterId)) continue;
                float dxP = target.getPosition().getX() - p.getPosition().getX();
                float dyP = target.getPosition().getY() - p.getPosition().getY();
                if (Math.hypot(dxP, dyP) <= 32f) {
                    int dmg = p.getDamage();
                    Player shooter = players.get(shooterId);
                    if (shooter != null
                            && shooter.isDamageBoostActive()
                            && System.currentTimeMillis() <= shooter.getDamageBoostEndTime()) {
                        dmg *= Player.DAMAGE_MULTIPLIER;
                    }
                    target.setCurrentHealth(Math.max(0, target.getCurrentHealth() - dmg));
                    if (target.getCurrentHealth() == 0) {
                        target.setVisible(false);
                    }
                    projectileManager.removeProjectile(p.getId());
                    break;
                }
            }
        }
    }

}
