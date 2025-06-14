package at.fhv.spiel_service.service.room;

import at.fhv.spiel_service.messaging.EventPublisher;
import at.fhv.spiel_service.service.gameSession.GameSessionImpl;
import at.fhv.spiel_service.service.gameSession.IGameSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class RoomServiceImpl implements IRoomService {

    private final List<IGameSession> rooms = new ArrayList<>();
    private final EventPublisher eventPublisher;

    public RoomServiceImpl(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public IGameSession createRoom(String levelId) {
        IGameSession room = new GameSessionImpl(eventPublisher, levelId);
        rooms.add(room);
        return room;
    }

    @Override
    public IGameSession getGameRoom(String roomId) {
        return rooms.stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst().orElse(null);
    }

    @Override
    public String assignPlayerToRoom(String playerId, String brawlerId, String levelId, String playerName) {
        for (IGameSession room : rooms) {
            if (!room.isFull() && room.getLevelId().equals(levelId) && !room.hasGameStarted()) {
                room.addPlayer(playerId, brawlerId, playerName);
                return room.getId();
            }
        }
        IGameSession newRoom = createRoom(levelId);
        newRoom.addPlayer(playerId, brawlerId, playerName);
        return newRoom.getId();
    }

    @Override
    public void removePlayerFromRoom(String playerId) {
        Iterator<IGameSession> it = rooms.iterator();
        while (it.hasNext()) {
            IGameSession room = it.next();
            if (room.getPlayers().containsKey(playerId)) {
                room.removePlayer(playerId);
                if (room.getPlayers().isEmpty()) it.remove();
                return;
            }
        }
    }
}
