package at.fhv.spiel_backend.server.game;

import at.fhv.spiel_backend.ws.StateUpdateMessage;

public interface IGameRoom {
    String getId();
    void addPlayer(String playerId);
    boolean isFull();


}
