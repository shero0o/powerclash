package at.fhv.spiel_backend.server.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomManagerImpl implements IRoomManager {
    private final Map<String, IGameRoom> rooms = new ConcurrentHashMap<>();
    private final IRoomFactory roomFactory;

    public RoomManagerImpl(IRoomFactory roomFactory) {
        this.roomFactory = roomFactory;
    }

    @Override
    public IGameRoom createRoom() {
        IGameRoom room = roomFactory.createRoom();
        rooms.put(room.getId(), room);
        return room;
    }

    @Override
    public IGameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }


    @Override
    public void assignToRoom(WebSocketSession session, String roomId) {
        IGameRoom room = rooms.get(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Raum existiert nicht: " + roomId);
        }
        room.addPlayer(session.getId());
    }
}

