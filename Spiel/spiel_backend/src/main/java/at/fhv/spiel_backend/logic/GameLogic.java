package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.model.Player;
import at.fhv.spiel_backend.ws.StateUpdateMessage;

public interface GameLogic {
    StateUpdateMessage buildStateUpdate();
    void movePlayer(String playerId, float x, float y, float angle);
    void addPlayer(String playerId);
    void removePlayer(String playerId);

    /**
     * Expose the internal Player object so the room loop can apply speed.
     */
    Player getPlayer(String playerId);
    void attack(String playerId, float dirX, float dirY, float angle);

}
