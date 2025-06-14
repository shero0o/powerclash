package at.fhv.spiel_service.service.room;

import at.fhv.spiel_service.messaging.EventPublisher;
import at.fhv.spiel_service.service.game.logic.DefaultIGameLogic;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class RoomManagerImpl implements IRoomManager {

    private final List<IGameRoom> rooms = new ArrayList<>();
    private final EventPublisher eventPublisher;

    public RoomManagerImpl(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public IGameRoom createRoom(String levelId) {
        IGameRoom newRoom = new GameRoomImpl(new DefaultIGameLogic(), eventPublisher, levelId);
        rooms.add(newRoom);
        return newRoom;
    }

    @Override
    public IGameRoom getRoom(String roomId) {
        return rooms.stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String assignToRoom(String playerId, String brawlerId, String levelId, String playerName) {
        for (IGameRoom room : rooms) {
            if (!room.isFull() && room.getLevelId().equals(levelId)) {
                if (!((GameRoomImpl) room).hasGameStarted()) {
                    room.addPlayer(playerId, brawlerId, playerName);
                    return room.getId();
                }
            }
        }

        IGameRoom newRoom = createRoom(levelId);
        newRoom.addPlayer(playerId, brawlerId, playerName);
        return newRoom.getId();
    }



    @Override
    public void removeFromRoom(String playerId) {
        Iterator<IGameRoom> it = rooms.iterator();
        while (it.hasNext()) {
            IGameRoom room = it.next();
            if (room.getPlayers().containsKey(playerId)) {
                room.removePlayer(playerId);
                if (room.getPlayers().isEmpty()) {
                    it.remove();
                }
                return;
            }
        }
    }
}
