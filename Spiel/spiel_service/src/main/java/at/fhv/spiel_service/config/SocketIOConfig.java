package at.fhv.spiel_service.config;

import com.corundumstudio.socketio.SocketIOServer;
import at.fhv.spiel_service.domain.Gadget;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.dto.*;
import at.fhv.spiel_service.service.game.logic.DefaultIGameLogic;
import at.fhv.spiel_service.service.gameSession.GameSessionImpl;
import at.fhv.spiel_service.service.gameSession.IGameSession;
import at.fhv.spiel_service.service.room.IRoomService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConfigurationProperties(prefix = "socketio")
public class SocketIOConfig {

    private static final Logger log = LoggerFactory.getLogger(SocketIOConfig.class);

    // ========== Konfigurierbare Felder ==========
    @NotBlank
    private String hostname;
    @Min(1)
    @Max(65535)
    private int port;
    @NotBlank
    private String origin;

    // ========== Getter & Setter ==========
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    // ========== Server Bean ==========
    @Bean(destroyMethod = "stop")
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(hostname);
        config.setPort(port);
        config.setOrigin(origin);

        log.info("Socket.IO Server config → Host: {}, Port: {}, Origin: {}", hostname, port, origin);
        SocketIOServer server = new SocketIOServer(config);
        server.start();
        return server;
    }
}

//    private static final Logger log = LoggerFactory.getLogger(SocketIOConfig.class);
//
//    @Bean(destroyMethod = "stop")
//    public SocketIOServer socketIOServer(@Lazy IRoomService roomManager) {
//        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
//        config.setHostname("localhost");
//        config.setPort(8081);
//        config.setOrigin("http://localhost:5173");
//
//        SocketIOServer server = new SocketIOServer(config);
//        log.info("Starting SocketIOServer on {}:{} with origin {}",
//                config.getHostname(), config.getPort(), config.getOrigin());
//
//        // joinRoom
//        server.addEventListener("joinRoom", JoinRequestDTO.class, (client, data, ack) -> {
//            String roomId = roomManager.assignPlayerToRoom(data.getPlayerId(), data.getBrawlerId(), data.getLevelId(), data.getPlayerName());
//            client.joinRoom(roomId);
//            // initial state
//            roomManager.getGameRoom(roomId).buildStateUpdate();
//            server.getRoomOperations(roomId).sendEvent("stateUpdate", roomManager.getGameRoom(roomId).buildStateUpdate());
//
//            if (data.getChosenWeapon() != null) {
//                roomManager.getGameRoom(roomId).getGameLogic().setPlayerWeapon(data.getPlayerId(), data.getChosenWeapon());
//            }
//            if (data.getChosenGadget() != null) {
//                roomManager.getGameRoom(roomId).getGameLogic().setPlayerGadget(data.getPlayerId(), data.getChosenGadget());
//            }
//            ack.sendAckData(new JoinResponseDTO(roomId));
//        });
//
//        // waitingReady
//        server.addEventListener("waitingReady", WaitingReadyDTO.class,
//                (client, data, ack) -> {
//                    String incoming = data.getRoomId();
//                    IGameSession room = roomManager.getGameRoom(incoming);
//                    if (room == null) {
//                        String newId = roomManager.assignPlayerToRoom(
//                                data.getPlayerId(), data.getBrawlerId(), data.getLevelId(), data.getPlayerName());
//                        client.joinRoom(newId);
//                        ack.sendAckData(new JoinResponseDTO(newId));
//                        room = roomManager.getGameRoom(newId);
//                        log.info("Auto-created room {} for player {}", newId, data.getPlayerId());
//                    } else {
//                        ack.sendAckData("ok");
//                    }
//                    room.markReady(data.getPlayerId(), data.getBrawlerId());
//                    if (room.getReadyCount() == room.getPlayerCount() && room.getPlayerCount() == room.getMaxPlayers()) {
//                        server.getRoomOperations(room.getId()).sendEvent("startGame");
//                        room.start();
//                    }
//                }
//        );
//
//        // move
//        server.addEventListener("move", MoveRequestDTO.class,
//                (client, data, ack) -> {
//                    GameSessionImpl room = (GameSessionImpl) roomManager.getGameRoom(data.getRoomId());
//                    if (room == null) {
//                        log.warn("Received MOVE for unknown room {}", data.getRoomId());
//                        ack.sendAckData("error: room_not_found");
//                        return;
//                    }
//                    if (room.getGameLogic().getPlayer(data.getPlayerId()).getCurrentHealth() <= 0) {
//                        ack.sendAckData("dead");
//                        return;
//                    }
//                    room.setPlayerInput(data.getPlayerId(), data.getDirX(), data.getDirY(), data.getAngle());
//                    ack.sendAckData("ok");
//                }
//        );
//
//        // shootProjectile
//        server.addEventListener("shootProjectile", ShootProjectileDTO.class,
//                (client, data, ack) -> {
//                    IGameSession room = roomManager.getGameRoom(data.getRoomId());
//                    if (room != null) {
//                        room.getGameLogic().spawnProjectile(
//                                data.getPlayerId(),
//                                room.getGameLogic().getPlayerPosition(data.getPlayerId()),
//                                new Position(data.getDirection().getX(), data.getDirection().getY()),
//                                data.getProjectileType()
//                        );
//                    }
//                    ack.sendAckData("ok");
//                }
//        );
//
//        // changeWeapon
//        server.addEventListener("changeWeapon", ChangeWeaponDTO.class,
//                (client, data, ack) -> {
//                    IGameSession room = roomManager.getGameRoom(data.getRoomId());
//                    room.getGameLogic().setPlayerWeapon(data.getPlayerId(), data.getProjectileType());
//                    ack.sendAckData("ok");
//                }
//        );
//
//        // useGadget
//        server.addEventListener("useGadget", UseGadgetDTO.class,
//                (client, data, ack) -> {
//                    GameSessionImpl room = (GameSessionImpl) roomManager.getGameRoom(data.getRoomId());
//                    DefaultIGameLogic logic = (DefaultIGameLogic) room.getGameLogic();
//                    Player p = logic.getPlayer(data.getPlayerId());
//                    Gadget g = room.getGameLogic().getGadget(data.getPlayerId());
//                    if (g == null) {
//                        ack.sendAckData("error:no_gadget");
//                        return;
//                    }
//                    if (g.getRemainingUses() <= 0) {
//                        ack.sendAckData("error:no_uses_left");
//                        return;
//                    }
//                    if (g.getTimeRemaining() > 0) {
//                        ack.sendAckData("error:cooldown");
//                        return;
//                    }
//                    long now = System.currentTimeMillis();
//                    switch (g.getType()) {
//                        case SPEED_BOOST:
//                            p.setSpeedBoostActive(true);
//                            break;
//                        case HEALTH_BOOST:
//                            p.setMaxHealth(p.getMaxHealth() + Player.HP_BOOST_AMOUNT);
//                            p.setCurrentHealth(
//                                    Math.min(p.getMaxHealth(),
//                                            p.getCurrentHealth() + Player.HP_BOOST_AMOUNT)
//                            );
//                            break;
//                        case DAMAGE_BOOST:
//                            p.setDamageBoostEndTime(now + 10_000);
//                            break;
//                    }
//                    g.setTimeRemaining(10_000L);
//                    g.setRemainingUses(g.getRemainingUses() - 1);
//                    ack.sendAckData("ok");
//                }
//        );
//
//        // leaveRoom
//        server.addEventListener("leaveRoom", LeaveRoomDTO.class, (client, data, ack) -> {
//            roomManager.removePlayerFromRoom(data.getPlayerId());
//            log.info("Player {} left the room", data.getPlayerId());
//            ack.sendAckData("ok");
//        });
//
//        // matchOver
//        server.addEventListener("matchOver", RoomIdOnlyDTO.class, (client, data, ack) -> {
//            log.info("Match over in room {}", data.getRoomId());
//            server.getRoomOperations(data.getRoomId()).sendEvent("matchOver");
//            ack.sendAckData("ok");
//        });
//
//        server.addDisconnectListener(client -> {
//            String playerId = client.getHandshakeData().getSingleUrlParam("playerId");
//            if (playerId != null) {
//                roomManager.removePlayerFromRoom(playerId);
//                log.info("Player {} disconnected and was removed from their room", playerId);
//            }
//        });
//
//        server.start();
//        return server;
//    }

