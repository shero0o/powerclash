package at.fhv.spiel_service.service.game.manager.NPC;

import at.fhv.spiel_service.domain.GameMap;
import at.fhv.spiel_service.domain.NPC;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;

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
    public void updateNPCs(float deltaSec, Map<String, Player> players) {
        long now = System.currentTimeMillis();
        for (NPC npc : npcs) {
            if (npc.getCurrentHealth() <= 0) continue;
            Player target = findClosestAlivePlayer(npc, players.values());
            if (target == null) continue;
            float dx = target.getPosition().getX() - npc.getPosition().getX();
            float dy = target.getPosition().getY() - npc.getPosition().getY();
            float distSq = dx*dx + dy*dy;
            float attackRadius = npc.getAttackRadius();
            float radiusSq = attackRadius * attackRadius;
            float angle = (float) Math.atan2(dy, dx);

            if (distSq > radiusSq) {
                moveTowards(npc, dx, dy, deltaSec, angle);
            } else {
                npc.setPosition(new Position(npc.getPosition().getX(),
                        npc.getPosition().getY(),
                        angle));
                tryAttack(npc, target, now);
            }
        }
    }

    private Player findClosestAlivePlayer(NPC npc, Iterable<Player> players) {
        Player best = null;
        float bestDistSq = Float.MAX_VALUE;
        for (Player p : players) {
            if (p.getCurrentHealth() <= 0) continue;
            float dx = p.getPosition().getX() - npc.getPosition().getX();
            float dy = p.getPosition().getY() - npc.getPosition().getY();
            float dsq = dx*dx + dy*dy;
            if (dsq < bestDistSq) {
                bestDistSq = dsq;
                best = p;
            }
        }
        return best;
    }

    private void moveTowards(NPC npc, float dx, float dy, float deltaSec, float angle) {
        float len = (float) Math.sqrt(dx*dx + dy*dy);
        if (len == 0) return;
        float nx = dx / len;
        float ny = dy / len;
        float newX = npc.getPosition().getX() + nx * npc.getSpeed() * deltaSec;
        float newY = npc.getPosition().getY() + ny * npc.getSpeed() * deltaSec;
        int tx = (int)(newX / gameMap.getTileWidth());
        int ty = (int)(newY / gameMap.getTileHeight());

        if (!gameMap.isWallAt(tx, ty)) {
            npc.setPosition(new Position(newX, newY, angle));
        } else {
            npc.setPosition(new Position(npc.getPosition().getX(),
                    npc.getPosition().getY(),
                    angle));
        }
    }

    private void tryAttack(NPC npc, Player target, long now) {
        if (now - npc.getLastAttackTime() < npc.getAttackCooldownMs()) {
            return;
        }
        int newHp = Math.max(0, target.getCurrentHealth() - npc.getDamage());
        target.setCurrentHealth(newHp);
        if (newHp == 0) {
            target.setVisible(false);
        }
        npc.setLastAttackTime(now);
    }
}
