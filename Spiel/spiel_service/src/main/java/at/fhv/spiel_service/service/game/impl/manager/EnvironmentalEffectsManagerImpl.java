package at.fhv.spiel_service.service.game.impl.manager;

import at.fhv.spiel_service.domain.GameMap;
import at.fhv.spiel_service.domain.NPC;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.service.game.manager.EnvironmentalEffectsManager;

import java.util.Map;

public class EnvironmentalEffectsManagerImpl implements EnvironmentalEffectsManager {

    private final GameMap gameMap;
    private final Map<String, Player> players;

    public EnvironmentalEffectsManagerImpl(GameMap gameMap,
                                           Map<String, Player> players) {
        this.gameMap = gameMap;
        this.players = players;
    }


    @Override
    public void applyEnvironmentalEffects (float deltaSec) {
        long now = System.currentTimeMillis();

        for (Player p : players.values()) {
            if (p.getCurrentHealth() <= 0) continue;

            Position pos = p.getPosition();
            int tileX = (int) (pos.getX() / gameMap.getTileWidth());
            int tileY = (int) (pos.getY() / gameMap.getTileHeight());
            Position tilePos = new Position(tileX, tileY);

            if (gameMap.isPoisonTile(tilePos)) {
                long last = p.getLastPoisonTime();
                if (now - last >= 1000) {
                    p.setCurrentHealth(Math.max(0, p.getCurrentHealth() - 15));
                    p.setLastPoisonTime(now);

                    if (p.getCurrentHealth() <= 0) {
                        p.setVisible(false); // Optional: unsichtbar wenn tot
                    }
                }
            } else {
                // Nicht in Giftfeld → Reset poison timer
                p.setLastPoisonTime(0);
            }

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
