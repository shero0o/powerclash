package at.fhv.spiel_service.service.gameSession;

import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.service.game.logic.IGameLogic;

import java.util.Map;

public interface IGameSession {

    String getId();
    String getLevelId();
    void addPlayer(String playerId, String brawlerId, String playerName);
    void removePlayer(String playerId);
    int getPlayerCount();
    boolean isFull();
    void start();
    boolean hasGameStarted();
    void markReady(String playerId, String brawlerId);
    int getReadyCount();
    StateUpdateMessage buildStateUpdate();
    Map<String, Object> getPlayers();
    IGameLogic getGameLogic();
    int getMaxPlayers();
    void setPlayerInput(String playerId, float dirX, float dirY, float angle);

}
