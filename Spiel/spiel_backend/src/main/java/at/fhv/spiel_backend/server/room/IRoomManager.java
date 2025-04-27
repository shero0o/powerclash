package at.fhv.spiel_backend.server.room;
import at.fhv.spiel_backend.server.game.IGameRoom;
import org.springframework.web.socket.WebSocketSession;
public interface IRoomManager {

    /**
     * Creates a new game room and registers it.
     * @return the created IGameRoom
     */
    IGameRoom createRoom();

    /**
     * Retrieves an existing room by its ID, or null if not found.
     * @param roomId the ID of the room
     * @return the IGameRoom instance or null
     */
    IGameRoom getRoom(String roomId);

    /**
     * Assigns the player to a non-full room or creates a new one.
     * @param playerId unique identifier for the player
     * @return the ID of the room the player was assigned to
     */
    String assignToRoom(String playerId);

    /**
     * Removes a player from their room and cleans up empty rooms.
     * @param playerId the ID of the player to remove
     */
    void removeFromRoom(String playerId);
}

