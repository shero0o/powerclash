package at.fhv.spiel_backend.config;

import at.fhv.spiel_backend.DTO.*;
import at.fhv.spiel_backend.server.game.GameRoomImpl;
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

        // inside socketIOServer(…)
        server.addEventListener("move", MoveRequestDTO.class, (client, data, ack) -> {
            IGameRoom generic = roomManager.getRoom(data.getRoomId());
            if (!(generic instanceof GameRoomImpl)) {
                ack.sendAckData("error: invalid room");
                return;
            }
            GameRoomImpl room = (GameRoomImpl) generic;

            log.info("Recording input – player {}, dirX={}, dirY={}, angle={}",
                    data.getPlayerId(), data.getDirX(), data.getDirY(), data.getAngle()
            );

            // THIS is the key: stash it for the next tick
            room.setPlayerInput(
                    data.getPlayerId(),
                    data.getDirX(),
                    data.getDirY(),
                    data.getAngle()
            );

            // ack quickly – the scheduled tick will broadcast
            ack.sendAckData("ok");
        });

        server.addEventListener("attack", AttackRequestDTO.class, (client, data, ack) -> {
            // 1) look up the room
            IGameRoom generic = roomManager.getRoom(data.getRoomId());
            if (!(generic instanceof GameRoomImpl)) {
                ack.sendAckData("error: invalid room");
                return;
            }
            GameRoomImpl room = (GameRoomImpl) generic;

            // 2) log for debugging
            log.info("Player {} attacks in room {} with dirX={}, dirY={}, angle={}",
                    data.getPlayerId(),
                    data.getRoomId(),
                    data.getDirX(),
                    data.getDirY(),
                    data.getAngle()
            );

            // 3) apply damage + broadcast
            room.handleAttack(
                    data.getPlayerId(),
                    data.getDirX(),
                    data.getDirY(),
                    data.getAngle()
            );

            // 4) ack immediately
            ack.sendAckData("ok");
        });


        server.start();
        return server;
    }
}