package at.fhv.spiel_backend.ws;

import at.fhv.spiel_backend.server.IRoomManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {
    private final IRoomManager roomManager;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("➡️  WS connected: sessionId=" + session.getId());
        roomManager.assignToRoom(session);
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        InputMessage im = mapper.readValue(msg.getPayload(), InputMessage.class);
        switch (im.getType()) {
            case MOVE -> {
                MovePayload mp = mapper.convertValue(im.getPayload(), MovePayload.class);
                roomManager.movePlayer(session, im.getPlayerId(), mp.getX(), mp.getY());
            }
            case ATTACK -> {
                AttackPayload ap = mapper.convertValue(im.getPayload(), AttackPayload.class);
                roomManager.playerAttack(session, im.getPlayerId(), ap.getTargetX(), ap.getTargetY());
            }
            case USE_GADGET -> {
                roomManager.playerUseGadget(session, im.getPlayerId());
            }
        }
        sendState(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("⬅️  WS disconnected: sessionId=" + session.getId());
        roomManager.removeFromRoom(session);
    }


    private void sendState(WebSocketSession session) {
        StateUpdateMessage update = roomManager.buildStateUpdate();
        try {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(update)));
        } catch (Exception e) {
            // log or handle
        }
    }
}
