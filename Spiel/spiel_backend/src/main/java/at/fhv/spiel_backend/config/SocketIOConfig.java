package at.fhv.spiel_backend.config;

import at.fhv.spiel_backend.DTO.*;
import at.fhv.spiel_backend.logic.DefaultGameLogic;
import at.fhv.spiel_backend.model.Player;
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
            GameRoomImpl room = (GameRoomImpl) roomManager.getRoom(data.getRoomId());

            // ➊ refuse movement if player is dead
            Player shooter = ((DefaultGameLogic)room.getGameLogic()).getPlayer(data.getPlayerId());
            if (shooter.getCurrentHealth() <= 0) {
                ack.sendAckData("dead");
                return;
            }

            // otherwise proceed as before
            room.setPlayerInput(
                    data.getPlayerId(),
                    data.getDirX(),
                    data.getDirY(),
                    data.getAngle()
            );
            ack.sendAckData("ok");
        });

        server.addEventListener("attack", AttackRequestDTO.class, (client, data, ack) -> {
            GameRoomImpl room = (GameRoomImpl) roomManager.getRoom(data.getRoomId());

            // ➋ refuse attack if player is dead
            Player shooter = ((DefaultGameLogic)room.getGameLogic()).getPlayer(data.getPlayerId());
            if (shooter.getCurrentHealth() <= 0) {
                ack.sendAckData("dead");
                return;
            }

            room.handleAttack(
                    data.getPlayerId(),
                    data.getDirX(),
                    data.getDirY(),
                    data.getAngle()
            );
            ack.sendAckData("ok");
        });


        server.start();
        return server;
    }
}