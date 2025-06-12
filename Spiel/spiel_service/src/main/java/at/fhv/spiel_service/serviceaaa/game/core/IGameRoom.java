// src/main/java/at/fhv/spiel_service/serviceaaa/game/core/IGameRoom.java
package at.fhv.spiel_service.serviceaaa.game.core;

import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.services.core.IGameLogicService;

import java.util.Map;

public interface IGameRoom {
    String getId();
    String getLevelId();
    int    getPlayerCount();
    boolean isFull();
    int    getReadyCount();
    void   addPlayer(String playerId, String brawlerId, String playerName);
    void   removePlayer(String playerId);
    void   markReady(String playerId, String brawlerId);
    void   start();
    boolean hasGameStarted();
    int    getMaxPlayers();
    StateUpdateMessage buildStateUpdate();
    IGameLogicService getGameLogic();

    // <<< add this so RoomManagerImpl.compile calls r.getPlayers() >>>
    Map<String, ?> getPlayers();

    // <<< and this for your config:
    void setPlayerInput(String playerId, float dirX, float dirY, float angle);
}
