package at.fhv.spiel_backend.server.room;
import at.fhv.spiel_backend.server.game.IGameRoom;
import org.springframework.web.socket.WebSocketSession;

public interface IRoomManager {

    String assignToRoom(String playerId);

//    void assignToRoom(WebSocketSession session);
//    void movePlayer(WebSocketSession session, String playerId, float x, float y);
//    void playerAttack(WebSocketSession session, String playerId, float targetX, float targetY);
//    void playerUseGadget(WebSocketSession session, String playerId);
//    void removeFromRoom(WebSocketSession session);
//    StateUpdateMessage buildStateUpdate();
}

