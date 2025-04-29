package at.fhv.spiel_backend.config;

import at.fhv.spiel_backend.DTO.*;
import at.fhv.spiel_backend.server.room.IRoomManager;
import at.fhv.spiel_backend.server.game.IGameRoom;
import at.fhv.spiel_backend.ws.StateUpdateMessage;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class SocketIOConfig {

    private static final Logger log = LoggerFactory.getLogger(SocketIOConfig.class);


    @Bean
    public SocketIOServer socketIOServer(@Lazy IRoomManager roomManager) {
        com.corundumstudio.socketio.Configuration config =
                new com.corundumstudio.socketio.Configuration();
        config.setHostname("localhost");
        config.setPort(8081);
        config.setOrigin("http://localhost:5173");

        SocketIOServer server = new SocketIOServer(config);

        log.info("Starting SocketIOServer on {}:{} with origin {}",
                config.getHostname(), config.getPort(), config.getOrigin());

        server.addEventListener("joinRoom", JoinRequestDTO.class,
                (client, data, ack) -> {
                    String roomId = roomManager.assignToRoom(data.getPlayerId());
                    client.joinRoom(roomId);
                    ack.sendAckData(new JoinResponseDTO(roomId));
                    log.info("Assigned player {} to room {}", data.getPlayerId(), roomId);
                });

        server.addEventListener("waitingReady", WaitingReadyDTO.class,
                (client, data, ack) -> {
                    String roomId   = data.getRoomId();
                    String playerId = data.getPlayerId();
                    IGameRoom room  = roomManager.getRoom(roomId);

                    room.markReady(playerId);
                    ack.sendAckData("ok");

                    if (room.getReadyCount() == room.getPlayerCount() && room.isFull()) {
                        server.getRoomOperations(roomId).sendEvent("startGame");
                        room.start();
                        room.buildStateUpdate();
                    }
                }
        );

        server.addEventListener("move", MoveRequestDTO.class, (client, data, ack) -> {
            IGameRoom room = roomManager.getRoom(data.getRoomId());
            log.info("PlayerId: {}, PlayerX: {}, PlayerY: {}", data.getPlayerId(), data.getX(), data.getY());

            room.getGameLogic().movePlayer(
                    data.getPlayerId(),
                    data.getX(),
                    data.getY()
            );

            StateUpdateMessage update = room.buildStateUpdate();
            server.getRoomOperations(data.getRoomId()).sendEvent("stateUpdate", update);
        });

        server.start();
        return server;
    }
}
