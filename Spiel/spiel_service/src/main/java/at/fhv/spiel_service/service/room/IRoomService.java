package at.fhv.spiel_service.service.room;

import at.fhv.spiel_service.service.gameSession.IGameSession;

public interface IRoomService {

    IGameSession createRoom(Long levelId);
    IGameSession getGameRoom(String roomId);
    String assignPlayerToRoom(String playerId, String brawlerId, Long levelId, String playerName);
    void removePlayerFromRoom(String playerId);
}

