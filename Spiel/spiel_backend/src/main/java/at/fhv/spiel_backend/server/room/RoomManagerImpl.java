package at.fhv.spiel_backend.server.room;

import at.fhv.spiel_backend.logic.DefaultGameLogic;
import at.fhv.spiel_backend.server.EventPublisher;
import at.fhv.spiel_backend.server.game.GameRoomImpl;
import at.fhv.spiel_backend.server.game.IGameRoom;
import at.fhv.spiel_backend.server.map.IMapFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class RoomManagerImpl implements IRoomManager {

    private final List<IGameRoom> rooms = new ArrayList<>();
    private final IMapFactory mapFactory;
    private final EventPublisher eventPublisher;

    public RoomManagerImpl(IMapFactory mapFactory, EventPublisher eventPublisher) {
        this.mapFactory = mapFactory;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public IGameRoom createRoom(String levelId) {
        IGameRoom newRoom = new GameRoomImpl(mapFactory, new DefaultGameLogic(), eventPublisher, levelId);
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
    public String assignToRoom(String playerId, String brawlerId, String levelId) {
        for (IGameRoom room : rooms) {
            if (!room.isFull() && room.getLevelId().equals(levelId)) {
                room.addPlayer(playerId, brawlerId);
                return room.getId();
            }
        }

        // Kein passender Raum mit gleichem Level → neuen erstellen
        IGameRoom newRoom = createRoom(levelId);
        newRoom.addPlayer(playerId, brawlerId);
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
