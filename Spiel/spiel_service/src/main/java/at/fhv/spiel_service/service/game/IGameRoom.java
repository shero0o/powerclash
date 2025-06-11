package at.fhv.spiel_service.service.game;

import at.fhv.spiel_service.messaging.StateUpdateMessage;
import java.util.Map;

public interface IGameRoom {

    /**
     * @return unique identifier for this room
     */
    String getId();

    String getLevelId();
    /**
     * Adds a player to this room.
     * @param playerId unique identifier of the player
     */

    void addPlayer(String playerId, String brawlerId, String playerName);

    /**
     * Removes a player from this room.
     * @param playerId unique identifier of the player
     */
    void removePlayer(String playerId);

    /**
     * @return current number of players in this room
     */
    int getPlayerCount();

    /**
     * @return true if room has reached its max capacity
     */
    boolean isFull();

    /**
     * Initializes and starts the game loop for this room.
     */
    void start();

    /**
     * Builds the data structure describing the current game state.
     * @return a StateUpdateMessage to broadcast to clients
     */
    StateUpdateMessage buildStateUpdate();

    /**
     * @return read-only view of the players map (playerId -> session or data)
     */
    Map<String, Object> getPlayers();
    void markReady(String playerId, String brawlerId);
    int  getReadyCount();
    GameLogic getGameLogic();

    int getMaxPlayers();
}
