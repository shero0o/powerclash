package at.fhv.spiel_service.service.game.manager.environmentalEffects;

import at.fhv.spiel_service.domain.GameMap;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;

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
    public void applyEnvironmentalEffects(float deltaSec) {
        long now = System.currentTimeMillis();
        for (Player p : players.values()) {
            if (p.getCurrentHealth() <= 0) {
                continue;
            }
            Position pos = p.getPosition();
            int tileX = (int) (pos.getX() / gameMap.getTileWidth());
            int tileY = (int) (pos.getY() / gameMap.getTileHeight());
            Position tile = new Position(tileX, tileY);

            applyPoison(p, tile, now);
            applyHeal(p, tile, now);
        }
    }

    private void applyPoison(Player p, Position tile, long now) {
        if (gameMap.isPoisonTile(tile) && now - p.getLastPoisonTime() >= 1000) {
            p.setCurrentHealth(Math.max(0, p.getCurrentHealth() - 15));
            p.setLastPoisonTime(now);
            if (p.getCurrentHealth() <= 0) {
                p.setVisible(false);
            }
        }
        else if (!gameMap.isPoisonTile(tile)) {
            p.setLastPoisonTime(0);
        }
    }

    private void applyHeal(Player player, Position tile, long now) {
        if (gameMap.isHealTile(tile) && now - player.getLastHealTime() >= 1000) {
            player.setCurrentHealth(Math.min(player.getMaxHealth(), player.getCurrentHealth() + 15));
            player.setLastHealTime(now);
        }
        else if (!gameMap.isHealTile(tile)) {
            player.setLastHealTime(0);
        }
    }
}
