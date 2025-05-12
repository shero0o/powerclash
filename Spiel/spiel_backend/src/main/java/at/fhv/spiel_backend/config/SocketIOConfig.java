package at.fhv.spiel_backend.config;

import at.fhv.spiel_backend.DTO.ChangeWeaponDTO;
import at.fhv.spiel_backend.DTO.JoinRequestDTO;
import at.fhv.spiel_backend.DTO.JoinResponseDTO;
import at.fhv.spiel_backend.DTO.MoveRequestDTO;
import at.fhv.spiel_backend.DTO.ShootProjectileDTO;
import at.fhv.spiel_backend.DTO.WaitingReadyDTO;
import at.fhv.spiel_backend.logic.DefaultGameLogic;
import at.fhv.spiel_backend.server.game.GameRoomImpl;
import at.fhv.spiel_backend.model.Position;
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
            String roomId = roomManager.assignToRoom(data.getPlayerId(), data.getBrawlerId(), data.getLevelId());
            IGameRoom room = roomManager.getRoom(roomId);
            client.joinRoom(roomId);
            server.getRoomOperations(roomId).sendEvent("stateUpdate", room.buildStateUpdate());
            room.start();
            ack.sendAckData(new JoinResponseDTO(roomId));
        });

        // Ready-Phase
        server.addEventListener("waitingReady", WaitingReadyDTO.class,
                (client, data, ack) -> {
                    String incoming = data.getRoomId();
                    IGameRoom room     = roomManager.getRoom(incoming);
                    boolean isNewRoom  = false;

                    if (room == null) {
                        String newId = roomManager.assignToRoom(
                                data.getPlayerId(), data.getBrawlerId(), data.getLevelId()
                        );
                        room = roomManager.getRoom(newId);
                        client.joinRoom(newId);
                        ack.sendAckData(new JoinResponseDTO(newId));
                        log.info("Auto-created room {} for player {}", newId, data.getPlayerId());
                        isNewRoom = true;
                    } else {
                        ack.sendAckData("ok");
                    }

                    room.markReady(data.getPlayerId(), data.getBrawlerId());
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

        // Projektil-Schuss verarbeiten (frontend nutzt 'shootProjectile')
        server.addEventListener("shootProjectile", ShootProjectileDTO.class,
                (client, data, ack) -> {
                    IGameRoom room = roomManager.getRoom(data.getRoomId());
                    if (room != null) {
                        room.getGameLogic().spawnProjectile(
                                data.getPlayerId(),
                                room.getGameLogic().getPlayerPosition(data.getPlayerId()),
                                new Position(data.getDirection().getX(), data.getDirection().getY()),
                                data.getProjectileType()
                        );
                    }
                    // Optionally ack here if DTO supports it
                }
        );

        // Weapon-Change-event
        server.addEventListener("changeWeapon", ChangeWeaponDTO.class,
                (client, data, ack) -> {
                    IGameRoom room = roomManager.getRoom(data.getRoomId());
                    room.getGameLogic().setPlayerWeapon(data.getPlayerId(), data.getProjectileType());
                    ack.sendAckData("ok");
                }
        );

        server.start();
        return server;
    }
}
