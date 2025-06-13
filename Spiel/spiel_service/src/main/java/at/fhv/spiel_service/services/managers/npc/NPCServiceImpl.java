// src/main/java/at/fhv/spiel_service/services/managers/npc/NPCServiceImpl.java
package at.fhv.spiel_service.services.managers.npc;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.NPC;
import at.fhv.spiel_service.entities.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Strategy-Pattern für NPC‐Logik:
 * - Hält intern alle NPCs
 * - Aktualisiert Kampf-Logik pro NPC
 */
@Service
public class NPCServiceImpl implements INPCService {

    // Interne NPC-Liste
    private final List<NPC> npcList = new ArrayList<>();

    @Override
    public void addNpc(String npcId, NPC npc) {
        npcList.add(npc);
    }

    @Override
    public void updateNPCs(float deltaSec,
                           GameMap map,
                           Map<String, Player> players,
                           List<NPC> ignored) {
        long now = System.currentTimeMillis();

        for (NPC npc : npcList) {
            // Nächsten Spieler finden (minimale Distanz)
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
                float distance = (float) Math.hypot(dx, dy);

                // Wenn in Angriffs‐Radius und Cooldown abgelaufen:
                if (distance <= npc.getAttackRadius()
                        && now - npc.getLastAttackTime() >= npc.getAttackCooldownMs()) {
                    int newHp = Math.max(0, target.getCurrentHealth() - npc.getDamage());
                    target.setCurrentHealth(newHp);
                    npc.setLastAttackTime(now);
                }
            }
        }
    }

    @Override
    public List<NPC> getAllNpcs() {
        // Gib eine Kopie zurück, damit der interne Zustand geschützt bleibt
        return new ArrayList<>(npcList);
    }
}
