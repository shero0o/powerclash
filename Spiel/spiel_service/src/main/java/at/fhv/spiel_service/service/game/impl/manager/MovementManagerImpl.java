package at.fhv.spiel_service.service.game.impl.manager;

import at.fhv.spiel_service.domain.Crate;
import at.fhv.spiel_service.domain.GameMap;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.service.game.manager.MovementManager;

import java.util.Map;

public class MovementManagerImpl implements MovementManager {

    private final GameMap gameMap;
    private final Map<String, Player> players;
    private final Map<String, Crate> crates;

    // Füge hier via Konstruktor alle Abhängigkeiten hinzu, die du aus DefaultGameLogic brauchst
    public MovementManagerImpl(GameMap gameMap,
                               Map<String, Player> players,
                               Map<String, Crate> crates) {
        this.gameMap = gameMap;
        this.players = players;
        this.crates = crates;
    }

    @Override
    public void movePlayer(String playerId, float x, float y, float angle) {
        Player p = players.get(playerId);
        if (p == null || gameMap == null) return;

        int tileX = (int) (x / gameMap.getTileWidth());
        int tileY = (int) (y / gameMap.getTileHeight());
        // nur bewegen, wenn kein Wall-Tile
        boolean crateBlocks = crates.values().stream()
                .anyMatch(c -> {
                    int cx = (int) c.getPosition().getX();
                    int cy = (int) c.getPosition().getY();
                    return cx == tileX && cy == tileY;
                });

        if (!gameMap.isWallAt(tileX, tileY) && !crateBlocks) {
//                System.out.println("Move to " + tileX + "," + tileY +
//                        " – wall: " + gameMap.isWallAt(tileX, tileY) +
//                        ", crate: " + crateBlocks);

            p.setPosition(new Position(x, y, angle));

            // 🟡 Sichtbarkeit setzen anhand Busch
            Position tilePos = new Position(tileX, tileY);
            if (gameMap.isBushTile(tilePos)) {
                p.setVisible(false);
            } else {
                p.setVisible(true);
            }
        }
    }

    @Override
    public void applyEnvironmentalEffects() {
        long now = System.currentTimeMillis();
        for (Player p : players.values()) {
            if (p.getCurrentHealth() <= 0) continue;
            Position pos = p.getPosition();
            Position tilePos = new Position(
                    (int)(pos.getX() / gameMap.getTileWidth()),
                    (int)(pos.getY() / gameMap.getTileHeight())
            );
            // Poison
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
            // Heal
            if (gameMap.isHealTile(tilePos)) {
                long lastHeal = p.getLastHealTime();
                if (now - lastHeal >= 1000) {
                    p.setCurrentHealth(
                            Math.min(p.getMaxHealth(), p.getCurrentHealth() + 15)
                    );
                    p.setLastHealTime(now);
                }
            }
        }
    }

}
