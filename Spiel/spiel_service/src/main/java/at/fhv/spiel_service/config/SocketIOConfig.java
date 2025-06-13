// src/main/java/at/fhv/spiel_service/config/SocketIOConfig.java
package at.fhv.spiel_service.config;

import com.corundumstudio.socketio.SocketIOServer;
import at.fhv.spiel_service.dto.*;
import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.serviceaaa.game.core.IGameRoom;
import at.fhv.spiel_service.serviceaaa.room.IRoomManager;
import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Position;
import at.fhv.spiel_service.services.core.IGameLogicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class SocketIOConfig {

    private static final Logger log = LoggerFactory.getLogger(SocketIOConfig.class);

    @Bean(destroyMethod = "stop")
    public SocketIOServer socketIOServer(@Lazy IRoomManager roomManager) {
        com.corundumstudio.socketio.Configuration cfg = new com.corundumstudio.socketio.Configuration();
        cfg.setHostname("localhost");
        cfg.setPort(8081);
        cfg.setOrigin("http://localhost:5173");

        SocketIOServer server = new SocketIOServer(cfg);
        log.info("SocketIOServer running on {}:{} (origin {})",
                cfg.getHostname(), cfg.getPort(), cfg.getOrigin());

        // --- joinRoom ---
        server.addEventListener("joinRoom", JoinRequestDTO.class,
                (client, data, ack) -> {
                    String roomId = roomManager.assignToRoom(
                            data.getPlayerId(),
                            data.getBrawlerId(),
                            data.getLevelId(),
                            data.getPlayerName()
                    );
                    client.joinRoom(roomId);

                    IGameRoom room = roomManager.getRoom(roomId);
                    server.getRoomOperations(roomId)
                            .sendEvent("stateUpdate", room.buildStateUpdate());

                    IGameLogicService logic = room.getGameLogic();
                    if (data.getChosenWeapon() != null) {
                        logic.setPlayerWeapon(
                                data.getPlayerId(),
                                data.getChosenWeapon().name()        // <-- enum→String
                        );
                    }
                    if (data.getChosenGadget() != null) {
                        logic.setPlayerGadget(
                                data.getPlayerId(),
                                data.getChosenGadget().name()        // <-- enum→String
                        );
                    }
                    ack.sendAckData(new JoinResponseDTO(roomId));
                }
        );

        // --- waitingReady ---
        server.addEventListener("waitingReady", WaitingReadyDTO.class,
                (client, data, ack) -> {
                    IGameRoom room = roomManager.getRoom(data.getRoomId());
                    if (room == null) {
                        String newId = roomManager.assignToRoom(
                                data.getPlayerId(),
                                data.getBrawlerId(),
                                data.getLevelId(),
                                data.getPlayerName()
                        );
                        client.joinRoom(newId);
                        ack.sendAckData(new JoinResponseDTO(newId));
                        room = roomManager.getRoom(newId);
                        log.info("Auto-created room {} for {}", newId, data.getPlayerId());
                    } else {
                        ack.sendAckData("ok");
                    }

                    room.markReady(data.getPlayerId(), data.getBrawlerId());
                    if (room.getReadyCount() == room.getPlayerCount()
                            && room.getPlayerCount() == room.getMaxPlayers()) {
                        server.getRoomOperations(room.getId()).sendEvent("startGame");
                        room.start();
                    }
                }
        );

        // --- move ---
        server.addEventListener("move", MoveRequestDTO.class,
                (client, data, ack) -> {
                    IGameRoom room = roomManager.getRoom(data.getRoomId());
                    if (room == null) {
                        log.warn("MOVE for unknown room {}", data.getRoomId());
                        ack.sendAckData("error: room_not_found");
                        return;
                    }
                    Player p = room.getGameLogic().getPlayer(data.getPlayerId());
                    if (p == null || p.getCurrentHealth() <= 0) {
                        ack.sendAckData("dead");
                        return;
                    }
                    room.setPlayerInput(
                            data.getPlayerId(),
                            data.getDirX(),
                            data.getDirY(),
                            data.getAngle()
                    );
                    ack.sendAckData("ok");
                }
        );

        // --- shootProjectile ---
        server.addEventListener("shootProjectile", ShootProjectileDTO.class,
                (client, data, ack) -> {
                    IGameRoom room = roomManager.getRoom(data.getRoomId());
                    if (room != null) {
                        Position dir = data.getDirection();
                        room.getGameLogic().spawnProjectile(
                                data.getPlayerId(),
                                dir.getX(),
                                dir.getY(),
                                data.getProjectileType().name()     // <-- enum→String
                        );
                    }
                    ack.sendAckData("ok");
                }
        );

        // --- changeWeapon ---
        server.addEventListener("changeWeapon", ChangeWeaponDTO.class,
                (client, data, ack) -> {
                    IGameRoom room = roomManager.getRoom(data.getRoomId());
                    if (room != null) {
                        room.getGameLogic().setPlayerWeapon(
                                data.getPlayerId(),
                                data.getProjectileType().name()    // <-- enum→String
                        );
                    }
                    ack.sendAckData("ok");
                }
        );

        // --- useGadget ---
        server.addEventListener("useGadget", UseGadgetDTO.class,
                (client, data, ack) -> {
                    IGameRoom room = roomManager.getRoom(data.getRoomId());
                    if (room != null) {
                        room.getGameLogic().useGadget(data.getPlayerId());
                    }
                    ack.sendAckData("ok");
                }
        );

        // --- leaveRoom ---
        server.addEventListener("leaveRoom", LeaveRoomDTO.class,
                (client, data, ack) -> {
                    roomManager.removeFromRoom(data.getPlayerId());
                    log.info("Player {} left", data.getPlayerId());
                    ack.sendAckData("ok");
                }
        );

        // --- matchOver ---
        server.addEventListener("matchOver", RoomIdOnlyDTO.class,
                (client, data, ack) -> {
                    server.getRoomOperations(data.getRoomId())
                            .sendEvent("matchOver");
                    ack.sendAckData("ok");
                }
        );

        // --- disconnect ---
        server.addDisconnectListener(client -> {
            String pid = client.getHandshakeData()
                    .getSingleUrlParam("playerId");
            if (pid != null) {
                roomManager.removeFromRoom(pid);
                log.info("Disconnected {}", pid);
            }
        });

        server.start();
        return server;
    }
}
