package at.fhv.spiel_backend.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;


import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {
    private final SocketIOServer server;

    public EventPublisher(SocketIOServer server) {
        this.server = server;
    }

    /**
     * Sendet ein Event an alle Clients im Raum.
     * @param roomId ID des Raums
     * @param event   Payload (StateUpdateMessage o.ä.)
     */
    public void publish(String roomId, Object event) {
        server.getRoomOperations(roomId).sendEvent("stateUpdate", event);
    }
}

