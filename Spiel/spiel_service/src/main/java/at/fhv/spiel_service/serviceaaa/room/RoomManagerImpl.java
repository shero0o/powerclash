// src/main/java/at/fhv/spiel_service/serviceaaa/room/RoomManagerImpl.java
package at.fhv.spiel_service.serviceaaa.room;

import at.fhv.spiel_service.factoryaaa.IMapFactory;
import at.fhv.spiel_service.messaging.EventPublisher;
import at.fhv.spiel_service.serviceaaa.game.GameRoomImpl;
import at.fhv.spiel_service.serviceaaa.game.core.IGameRoom;
import at.fhv.spiel_service.services.core.IGameLogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoomManagerImpl implements IRoomManager {
    private final List<IGameRoom> rooms = new ArrayList<>();
    private final IMapFactory mapFactory;
    private final EventPublisher eventPublisher;
    private final IGameLogicService gameLogic;

    @Autowired
    public RoomManagerImpl(IMapFactory mapFactory,
                           EventPublisher eventPublisher,
                           IGameLogicService gameLogic) {
        this.mapFactory     = mapFactory;
        this.eventPublisher = eventPublisher;
        this.gameLogic      = gameLogic;
    }

    @Override
    public IGameRoom createRoom(String levelId) {
        GameRoomImpl room = new GameRoomImpl(
                mapFactory,
                gameLogic,
                /* npcService + zoneService injected inside GameRoomImpl via Spring */ null,
                null,
                eventPublisher,
                levelId
        );
        rooms.add(room);
        return room;
    }

    @Override
    public String assignToRoom(String playerId, String brawlerId, String levelId, String playerName) {
        for (IGameRoom r : rooms) {
            if (!r.isFull() && r.getLevelId().equals(levelId) && !((GameRoomImpl)r).hasGameStarted()) {
                r.addPlayer(playerId, brawlerId, playerName);
                return r.getId();
            }
        }
        IGameRoom newRoom = createRoom(levelId);
        newRoom.addPlayer(playerId, brawlerId, playerName);
        return newRoom.getId();
    }

    @Override
    public IGameRoom getRoom(String roomId) {
        return rooms.stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void removeFromRoom(String playerId) {
        rooms.removeIf(r -> {
            if (r.getPlayers().containsKey(playerId)) {
                r.removePlayer(playerId);
                return r.getPlayers().isEmpty();
            }
            return false;
        });
    }

}
