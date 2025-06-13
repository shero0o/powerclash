package at.fhv.spiel_service.services.managers.movement;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.Player;
import org.springframework.stereotype.Service;

import java.util.Map;


public interface IMovementService {
    void movePlayer(String playerId, float x, float y, float angle);
    void updateMovement(float deltaSec, GameMap map, Map<String, Player> players);
}
