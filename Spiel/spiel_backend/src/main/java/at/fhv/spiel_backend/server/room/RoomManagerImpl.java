package at.fhv.spiel_backend.server.room;

import at.fhv.spiel_backend.server.game.IGameRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomManagerImpl implements IRoomManager {
    private final List<IGameRoom> rooms = new ArrayList<IGameRoom>();
    private final IRoomFactory roomFactory;

    public RoomManagerImpl(IRoomFactory roomFactory) {
        this.roomFactory = roomFactory;
    }

    @Override
    public String assignToRoom(String playerId){
        for(IGameRoom room : rooms){
            if(!room.isFull()){
                room.addPlayer(playerId);
                return room.getId();
            }
        }
        IGameRoom gameRoom = roomFactory.createRoom();
        rooms.add(gameRoom);
        return gameRoom.getId();

    }




}

