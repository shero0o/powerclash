package at.fhv.spiel_backend.server.room;

import at.fhv.spiel_backend.server.game.IGameRoom;
import at.fhv.spiel_backend.ws.StateUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RoomManagerImpl implements IRoomManager {
    private final List<IGameRoom> rooms = new ArrayList<IGameRoom>();
    private final IRoomFactory roomFactory;

    public RoomManagerImpl(IRoomFactory roomFactory) {
        this.roomFactory = roomFactory;
    }

    public IGameRoom createRoom(String levelId) {
        IGameRoom newRoom = roomFactory.createRoom(levelId);
        rooms.add(newRoom);
        return newRoom;
    }

    /**
     * Sucht einen Raum nach seiner ID, oder liefert null zurück.
     */

    public IGameRoom getRoom(String roomId) {
        return rooms.stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Weist den Spieler einem nicht-vollen Raum zu oder erstellt einen neuen.
     * @return die ID des gewählten Raums
     */
    @Override

    public String assignToRoom(String playerId, String brawlerId, String levelId) {
        for (IGameRoom room : rooms) {
            if (!room.isFull()) {
                room.addPlayer(playerId, brawlerId);
                return room.getId();
            }
        }

        IGameRoom newRoom = createRoom(levelId); // <– levelId hier verwenden
        newRoom.addPlayer(playerId, brawlerId);
        return newRoom.getId();
    }



    /**
     * Entfernt den Spieler aus seinem Raum und löscht leere Räume.
     */

    public void removeFromRoom(String playerId) {

    }
}





