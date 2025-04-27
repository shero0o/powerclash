package at.fhv.spiel_backend.config;

import at.fhv.spiel_backend.controller.JoinRequestDTO;
import at.fhv.spiel_backend.controller.JoinResponseDTO;
import at.fhv.spiel_backend.server.room.IRoomManager;
import at.fhv.spiel_backend.server.game.IGameRoom;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class SocketIOConfig {

    @Bean
    public SocketIOServer socketIOServer(@Lazy IRoomManager roomManager) {
        com.corundumstudio.socketio.Configuration config =
                new com.corundumstudio.socketio.Configuration();
        config.setHostname("localhost");
        config.setPort(8081);
        config.setOrigin("http://localhost:5173");

        SocketIOServer server = new SocketIOServer(config);

        server.addEventListener("joinRoom", JoinRequestDTO.class,
                (client, data, ack) -> {
                    // Spieler zu Raum zuweisen
                    String roomId = roomManager.assignToRoom(data.getPlayerId());
                    client.joinRoom(roomId);
                    ack.sendAckData(new JoinResponseDTO(roomId));

                    // Raum starten, sobald er voll ist
                    IGameRoom room = roomManager.getRoom(roomId);
                    server.getRoomOperations(roomId).sendEvent("startGame", null);
                    room.start();
                });

        server.start();
        return server;
    }
}
