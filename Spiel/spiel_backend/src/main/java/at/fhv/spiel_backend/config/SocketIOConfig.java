package at.fhv.spiel_backend.config;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIOConfig {

    @Bean
    public SocketIOServer socketIOServer() {
        // Vollqualifizierter Typ, da "Configuration" hier doppelt besetzt wäre
        com.corundumstudio.socketio.Configuration config =
                new com.corundumstudio.socketio.Configuration();
        config.setHostname("localhost");
        config.setPort(8081); // Socket.IO Port

        final SocketIOServer server = new SocketIOServer(config);

        // Wenn sich ein Client verbindet
        server.addConnectListener(client -> {
            String roomId   = client.getHandshakeData().getSingleUrlParam("roomId");
            String playerId = client.getHandshakeData().getSingleUrlParam("playerId");
            System.out.println("Player connected: " + playerId + " to room: " + roomId);
            client.joinRoom(roomId);
        });

        // Wenn sich ein Client trennt
        server.addDisconnectListener(client ->
                System.out.println("Player disconnected: " + client.getSessionId())
        );

        // Event-Listener für Nachrichten
        server.addEventListener("message", ChatMessage.class, (client, data, ackRequest) -> {
            // roomId holt ihr weiter über query-Param
            String roomId   = client.getHandshakeData().getSingleUrlParam("roomId");
            // jetzt aus dem Payload
            String playerId = data.getPlayerId();

            // Logging:
            System.out.println("📥 Received message from player "
                    + playerId + " in room " + roomId
                    + ": " + data.getText());

            // Broadcast (evtl. mit dem setzten playerId im Payload)
            server.getRoomOperations(roomId)
                    .sendEvent("message", data);
        });


        // Server tatsächlich starten
        server.start();

        return server;
    }
}
