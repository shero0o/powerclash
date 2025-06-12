// src/main/java/at/fhv/spiel_service/services/managers/effects/EffectsServiceImpl.java
package at.fhv.spiel_service.services.managers.effects;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Position;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EffectsServiceImpl implements IEffectsService {

    @Override
    public void applyEnvironmentalEffects(float deltaSec,
                                          GameMap gameMap,
                                          Map<String, Player> players) {
        long now = System.currentTimeMillis();

        for (Player p : players.values()) {
            if (p.getCurrentHealth() <= 0) continue;

            Position pos = p.getPosition();
            int tileX = (int) (pos.getX() / gameMap.getTileWidth());
            int tileY = (int) (pos.getY() / gameMap.getTileHeight());
            Position tilePos = new Position(tileX, tileY);

            // Poison-Tile
            if (gameMap.isPoisonTile(tilePos)) {
                long last = p.getLastPoisonTime();
                if (now - last >= 1000) {
                    p.setCurrentHealth(Math.max(0, p.getCurrentHealth() - 15));
                    p.setLastPoisonTime(now);
                    if (p.getCurrentHealth() <= 0) {
                        p.setVisible(false);
                    }
                }
            } else {
                p.setLastPoisonTime(0);
            }

            // Heal-Tile
            if (gameMap.isHealTile(tilePos)) {
                long lastHeal = p.getLastHealTime();
                if (now - lastHeal >= 1000) {
                    int newHP = Math.min(p.getMaxHealth(), p.getCurrentHealth() + 15);
                    p.setCurrentHealth(newHP);
                    p.setLastHealTime(now);
                }
            } else {
                p.setLastHealTime(0);
            }
        }
    }
}
