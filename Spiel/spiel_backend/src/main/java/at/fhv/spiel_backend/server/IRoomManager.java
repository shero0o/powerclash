package at.fhv.spiel_backend.server;

import at.fhv.spiel_backend.ws.InputMessage;
import at.fhv.spiel_backend.ws.StateUpdateMessage;
import org.springframework.web.socket.WebSocketSession;

public interface IRoomManager {
    void assignToRoom(WebSocketSession session);
    void movePlayer(WebSocketSession session, String playerId, float x, float y);
    void playerAttack(WebSocketSession session, String playerId, float targetX, float targetY);
    void playerUseGadget(WebSocketSession session, String playerId);
    void removeFromRoom(WebSocketSession session);
    StateUpdateMessage buildStateUpdate();
}

