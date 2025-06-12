package at.fhv.spiel_service.services.managers.movement;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Position;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service

public class MovementServiceImpl implements IMovementService{
    // interne State‐Maps aus DefaultGameLogic entfallen – wir holen alle Spieler via PlayerService

    @Override
    public void movePlayer(String playerId, float x, float y, float angle) {
        // dieser Aufruf kommt direkt vom SocketIO‐Event
        // p.setInput ... oder p.setPosition direkt, je nach früherer Logik
    }

    @Override
    public void updateMovement(float deltaSec, GameMap map, Map<String, Player> players) {
        for (Player p : players.values()) {
            float newX = p.getPosition().getX() + p.getInputX() * p.getSpeed() * deltaSec;
            float newY = p.getPosition().getY() + p.getInputY() * p.getSpeed() * deltaSec;
            int tx = (int)(newX / map.getTileWidth()), ty = (int)(newY / map.getTileHeight());
            if (!map.isWallAt(tx, ty)) {
                Position pos = p.getPosition();
                pos.setX(newX);
                pos.setY(newY);
                pos.setAngle(p.getInputAngle());
            }
        }
    }
}
