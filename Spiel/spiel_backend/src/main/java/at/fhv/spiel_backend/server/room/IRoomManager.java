package at.fhv.spiel_backend.server.room;

public interface IRoomManager {

    IGameRoom createRoom();

    /**
     * Holt einen existierenden Raum per ID (oder null)
     */
    IGameRoom getRoom(String roomId);

    void assignToRoom(WebSocketSession session);



//    void assignToRoom(WebSocketSession session);
//    void movePlayer(WebSocketSession session, String playerId, float x, float y);
//    void playerAttack(WebSocketSession session, String playerId, float targetX, float targetY);
//    void playerUseGadget(WebSocketSession session, String playerId);
//    void removeFromRoom(WebSocketSession session);
//    StateUpdateMessage buildStateUpdate();
}

