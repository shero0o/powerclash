package at.fhv.spiel_service.service.game.manager.collision;

import at.fhv.spiel_service.config.GameConstants;
import at.fhv.spiel_service.domain.*;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static at.fhv.spiel_service.domain.ProjectileType.MINE;

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
        for (Projectile p : new ArrayList<>(projectiles)) {
            if (!handleNpcHit(p, npcs)
                    && !handleCrateHit(p, crates, playerCoins)
                    && !handleWallHit(p)) {

                handlePlayerHit(p, players);
            }
        }
    }

    private boolean handleNpcHit(Projectile p, List<NPC> npcs) {
        for (NPC npc : npcs) {
            if (npc.getCurrentHealth() <= 0) continue;
            double dx = npc.getPosition().getX() - p.getPosition().getX();
            double dy = npc.getPosition().getY() - p.getPosition().getY();
            if (Math.hypot(dx, dy) < 40f) {
                npc.setCurrentHealth(Math.max(0, npc.getCurrentHealth() - p.getDamage()));
                projectileManager.removeProjectileById(p.getId());
                return true;
            }
        }
        return false;
    }

    private boolean handleCrateHit(Projectile p,
                                   Map<String, Crate> crates,
                                   Map<String, Integer> playerCoins) {
        int x = (int)(p.getPosition().getX() / gameMap.getTileWidth());
        int y = (int)(p.getPosition().getY() / gameMap.getTileHeight());
        String key = x + "," + y;
        Crate crate = crates.get(key);
        if (crate != null) {
            crate.setWasHit(true);
            crate.setCurrentHealth(Math.max(0, crate.getCurrentHealth() - p.getDamage()));
            projectileManager.removeProjectileById(p.getId());
            if (crate.getCurrentHealth() <= 0) {
                crates.remove(key);
                playerCoins.merge(p.getPlayerId(), 10, Integer::sum);
            }
            return true;
        }
        return false;
    }

    private boolean handleWallHit(Projectile p) {
        if (p.getProjectileType() != MINE) {
            int x = (int)(p.getPosition().getX() / gameMap.getTileWidth());
            int y = (int)(p.getPosition().getY() / gameMap.getTileHeight());
            if (gameMap.isWallAt(x, y)) {
                projectileManager.removeProjectileById(p.getId());
                return true;
            }
        }
        return false;
    }

    private void handlePlayerHit(Projectile p, Map<String, Player> players) {
        String shooterId = p.getPlayerId();
        for (Player target : players.values()) {
            if (target.getId().equals(shooterId)) continue;
            double dx = target.getPosition().getX() - p.getPosition().getX();
            double dy = target.getPosition().getY() - p.getPosition().getY();
            if (Math.hypot(dx, dy) <= 32f) {
                int dmg = p.getDamage();
                Player shooter = players.get(shooterId);
                if (shooter != null
                        && shooter.isDamageBoostActive()
                        && System.currentTimeMillis() <= shooter.getDamageBoostEndTime()) {
                    dmg *= GameConstants.DAMAGE_MULTIPLIER;
                }
                target.setCurrentHealth(Math.max(0, target.getCurrentHealth() - dmg));
                if (target.getCurrentHealth() == 0) {
                    target.setVisible(false);
                }
                projectileManager.removeProjectileById(p.getId());
                break;
            }
        }
    }

}
