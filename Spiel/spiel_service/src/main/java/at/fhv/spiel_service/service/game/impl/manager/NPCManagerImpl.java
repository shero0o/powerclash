package at.fhv.spiel_service.service.game.impl.manager;

import at.fhv.spiel_service.domain.GameMap;
import at.fhv.spiel_service.domain.NPC;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.service.game.manager.NPCManager;

import java.util.List;
import java.util.Map;

public class NPCManagerImpl implements NPCManager {
    private final GameMap gameMap;
    private final List<NPC> npcs;

    public NPCManagerImpl(GameMap gameMap, List<NPC> npcs) {
        this.gameMap = gameMap;
        this.npcs    = npcs;
    }

    @Override
    public void updateNPCs(float deltaSec, Map<String, Player> players, List<NPC> npcs) {
        long now = System.currentTimeMillis();

        for (NPC npc : npcs) {
            // 1) nächster lebender Spieler
            Player target = null;
            float minDist = Float.MAX_VALUE;
            for (Player p : players.values()) {
                if (p.getCurrentHealth() <= 0) continue;
                float dx = p.getPosition().getX() - npc.getPosition().getX();
                float dy = p.getPosition().getY() - npc.getPosition().getY();
                float d = (float) Math.hypot(dx, dy);
                if (d < minDist) {
                    minDist = d;
                    target = p;
                }
            }
            if (target == null) continue;

            // 2) Blickwinkel berechnen
            float dx = target.getPosition().getX() - npc.getPosition().getX();
            float dy = target.getPosition().getY() - npc.getPosition().getY();
            float angle = (float) Math.atan2(dy, dx);

            // 3) Bewegung außerhalb Angriffsreichweite
            if (minDist > npc.getAttackRadius()) {
                float len = (float) Math.hypot(dx, dy);
                if (len > 0) {
                    float nx = dx / len, ny = dy / len;
                    float moveX = npc.getPosition().getX() + nx * npc.getSpeed() * deltaSec;
                    float moveY = npc.getPosition().getY() + ny * npc.getSpeed() * deltaSec;
                    int tx = (int) (moveX / gameMap.getTileWidth());
                    int ty = (int) (moveY / gameMap.getTileHeight());
                    if (!gameMap.isWallAt(tx, ty)) {
                        npc.setPosition(new Position(moveX, moveY, angle));
                    } else {
                        // Hindernis: nur drehen
                        npc.setPosition(new Position(
                                npc.getPosition().getX(),
                                npc.getPosition().getY(),
                                angle
                        ));
                    }
                }
            } else {
                // innerhalb Reichweite: nur drehen
                npc.setPosition(new Position(
                        npc.getPosition().getX(),
                        npc.getPosition().getY(),
                        angle
                ));
            }

            // 4) Melee-Angriff, wenn in Reichweite und Cooldown vorbei
            if (minDist <= npc.getAttackRadius()
                    && now - npc.getLastAttackTime() >= npc.getAttackCooldownMs()) {
                int newHp = Math.max(0, target.getCurrentHealth() - npc.getDamage());
                target.setCurrentHealth(newHp);
                if (newHp == 0) {
                    target.setVisible(false);
                }
                npc.setLastAttackTime(now);
            }
        }
    }
}
