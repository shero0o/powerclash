package at.fhv.spiel_service.service.game.manager.movement;

import at.fhv.spiel_service.domain.Crate;
import at.fhv.spiel_service.domain.GameMap;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;

import java.util.Map;

public class MovementManagerImpl implements MovementManager {
    private final GameMap gameMap;
    private final Map<String, Player> players;
    private final Map<String, Crate> crates;

    public MovementManagerImpl(GameMap gameMap,
                               Map<String, Player> players,
                               Map<String, Crate> crates) {
        this.gameMap = gameMap;
        this.players = players;
        this.crates = crates;
    }

    @Override
    public void movePlayer(String playerId, float x, float y, float angle) {
        Player player = players.get(playerId);
        if (player == null || gameMap == null) return;
        int tileX = (int) (x / gameMap.getTileWidth());
        int tileY = (int) (y / gameMap.getTileHeight());
        boolean crateBlocks = crates.values().stream()
                .anyMatch(c -> {
                    int cx = (int) c.getPosition().getX();
                    int cy = (int) c.getPosition().getY();
                    return cx == tileX && cy == tileY;
                });

        if (!gameMap.isWallAt(tileX, tileY) && !crateBlocks) {
            player.setPosition(new Position(x, y, angle));
            Position tilePos = new Position(tileX, tileY);
            if (gameMap.isBushTile(tilePos)) {
                player.setVisible(false);
            } else {
                player.setVisible(true);
            }
        }
    }

}
