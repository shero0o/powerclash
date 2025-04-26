package at.fhv.spiel_backend.server;

import at.fhv.spiel_backend.command.CommandFactory;
import at.fhv.spiel_backend.ws.InputMessage;
import at.fhv.spiel_backend.ws.StateUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomManagerImpl implements IRoomManager {
    private final GameRoomImpl prototype;
    private final Map<String, GameRoomImpl> rooms = new HashMap<>();

    private GameRoomImpl getRoom() {
        return rooms.computeIfAbsent("room1", key -> {
            prototype.start();
            return prototype;
        });
    }

    @Override
    public void assignToRoom(WebSocketSession session) {
        getRoom().addPlayer(session);
    }

    @Override
    public void movePlayer(WebSocketSession session, String playerId, float x, float y) {
        // create and dispatch move command
        getRoom().handleInput(CommandFactory.move(playerId, x, y));
    }

    @Override
    public void playerAttack(WebSocketSession session, String playerId, float targetX, float targetY) {
        getRoom().handleInput(CommandFactory.attack(playerId, targetX, targetY));
    }

    @Override
    public void playerUseGadget(WebSocketSession session, String playerId) {
        getRoom().handleInput(CommandFactory.useGadget(playerId));
    }

    @Override
    public void removeFromRoom(WebSocketSession session) {
        getRoom().removePlayer(session);
    }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        return getRoom().getLogic().buildStateUpdate();
    }
}

