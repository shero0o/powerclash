// at.fhv.spiel_backend.config.SocketIOConfig.java
package at.fhv.spiel_backend.config;

import at.fhv.spiel_backend.DTO.*;
import at.fhv.spiel_backend.logic.DefaultGameLogic;
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
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname("localhost");
        config.setPort(8081);
        config.setOrigin("http://localhost:5173");

        SocketIOServer server = new SocketIOServer(config);
        log.info("Starting SocketIOServer on {}:{} with origin {}",
                config.getHostname(), config.getPort(), config.getOrigin());

        // Spieler joinen und anlegen
        server.addEventListener("joinRoom", JoinRequestDTO.class, (client, data, ack) -> {
            String roomId = roomManager.assignToRoom(data.getPlayerId());
            IGameRoom room = roomManager.getRoom(roomId);
            client.joinRoom(roomId);
            server.getRoomOperations(roomId).sendEvent("stateUpdate", room.buildStateUpdate());
            ack.sendAckData(new JoinResponseDTO(roomId));
            log.info("Player {} assigned to room {}", data.getPlayerId(), roomId);
        });

        // Ready-Phase
        server.addEventListener("waitingReady", WaitingReadyDTO.class,
                (client, data, ack) -> {
                    String incoming = data.getRoomId();
                    IGameRoom room     = roomManager.getRoom(incoming);
                    boolean isNewRoom  = false;

                    // — if no such room, auto-create & join a fresh one —
                    if (room == null) {
                        String newId = roomManager.assignToRoom(data.getPlayerId());
                        room = roomManager.getRoom(newId);
                        client.joinRoom(newId);

                        // ACK back the new room ID so the front-end can update
                        ack.sendAckData(new JoinResponseDTO(newId));
                        log.info("Auto-created room {} for player {}", newId, data.getPlayerId());

                        isNewRoom = true;
                    } else {
                        // ACK the normal “ready” OK
                        ack.sendAckData("ok");
                    }

                    // — now mark ready in whichever room we have —
                    room.markReady(data.getPlayerId());

                    // — if everybody’s marked, start the game —
                    if (room.getReadyCount() == room.getPlayerCount()) {
                        server.getRoomOperations(room.getId()).sendEvent("startGame");
                        room.start();
                    }
                }
        );





        // Bewegungspayload verarbeiten
        server.addEventListener("move", MoveRequestDTO.class,
                (client, data, ack) -> {
                    GameRoomImpl room = (GameRoomImpl) roomManager.getRoom(data.getRoomId());
                    if (room == null) {
                        log.warn("Received MOVE for unknown room {}", data.getRoomId());
                        ack.sendAckData("error: room_not_found");
                        return;
                    }
                    if (((DefaultGameLogic)room.getGameLogic()).getPlayer(data.getPlayerId()).getCurrentHealth() <= 0) {
                        ack.sendAckData("dead");
                        return;
                    }
                    room.setPlayerInput(data.getPlayerId(), data.getDirX(), data.getDirY(), data.getAngle());
                    ack.sendAckData("ok");
                }
        );

        // Angriffspayload verarbeiten
        server.addEventListener("attack", AttackRequestDTO.class,
                (client, data, ack) -> {
                    GameRoomImpl room = (GameRoomImpl) roomManager.getRoom(data.getRoomId());
                    if (room == null) {
                        log.warn("Received ATTACK for unknown room {}", data.getRoomId());
                        ack.sendAckData("error: room_not_found");
                        return;
                    }
                    if (((DefaultGameLogic)room.getGameLogic()).getPlayer(data.getPlayerId()).getCurrentHealth() <= 0) {
                        ack.sendAckData("dead");
                        return;
                    }
                    room.handleAttack(data.getPlayerId(), data.getDirX(), data.getDirY(), data.getAngle());
                    ack.sendAckData("ok");
                }
        );

        server.start();
        return server;
    }
}
