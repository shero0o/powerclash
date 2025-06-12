// src/main/java/at/fhv/spiel_service/services/managers/combat/CombatServiceImpl.java
package at.fhv.spiel_service.services.managers.combat;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.NPC;
import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Projectile;
import at.fhv.spiel_service.entities.Position;
import at.fhv.spiel_service.services.managers.crate.ICrateService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class CombatServiceImpl implements ICombatService {

    @Override
    public void applyCombatOnProjectile(Projectile proj,
                                        GameMap map,
                                        Map<String, Player> players,
                                        ICrateService crateService,
                                        Iterator<Projectile> it,
                                        long now) {
        Position pos = proj.getPosition();

        // 1) Wand-Kollision (Tile)
        int tileX = (int)(pos.getX() / map.getTileWidth());
        int tileY = (int)(pos.getY() / map.getTileHeight());
        if (map.isWallAt(tileX, tileY)) {
            it.remove();
            return;
        }

        // 2) Spieler-Treffer (Abstand < 40px)
        for (Player p : players.values()) {
            if (p.getId().equals(proj.getPlayerId())) continue;
            float dx = pos.getX() - p.getPosition().getX();
            float dy = pos.getY() - p.getPosition().getY();
            if (Math.hypot(dx, dy) < 40f) {
                p.setCurrentHealth(Math.max(0, p.getCurrentHealth() - proj.getDamage()));
                it.remove();
                return;
            }
        }

        // 3) Crate-Treffer (Tile-Key)
        String key = tileX + "," + tileY;
        crateService.handleCrateHit(key, proj.getDamage());
    }

    @Override
    public void applyCombatNPCs(float deltaSec,
                                GameMap map,
                                Map<String, Player> players,
                                List<NPC> npcs) {
        long now = System.currentTimeMillis();

        for (NPC npc : npcs) {
            // Cooldown prüfen
            if (now - npc.getLastAttackTime() < npc.getAttackCooldownMs()) continue;

            // nächsten Spieler finden
            Player target = players.values().stream()
                    .min(Comparator.comparingDouble(p -> {
                        float dx = p.getPosition().getX() - npc.getPosition().getX();
                        float dy = p.getPosition().getY() - npc.getPosition().getY();
                        return Math.hypot(dx, dy);
                    }))
                    .orElse(null);

            if (target != null) {
                float dx = target.getPosition().getX() - npc.getPosition().getX();
                float dy = target.getPosition().getY() - npc.getPosition().getY();
                if (Math.hypot(dx, dy) <= npc.getAttackRadius()) {
                    target.setCurrentHealth(Math.max(0, target.getCurrentHealth() - npc.getDamage()));
                    npc.setLastAttackTime(now);
                }
            }
        }
    }
}
