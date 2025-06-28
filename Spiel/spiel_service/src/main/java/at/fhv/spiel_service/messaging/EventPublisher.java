package at.fhv.spiel_service.messaging;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {
    private final SocketIOServer server;

    public EventPublisher(SocketIOServer server) {
        this.server = server;
    }

    public void publish(String roomId, Object event) {
        server.getRoomOperations(roomId).sendEvent("stateUpdate", event);
    }
}
