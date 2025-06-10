package at.fhv.spiel_backend.config;

import at.fhv.spiel_backend.DTO.JoinRequestDTO;
import at.fhv.spiel_backend.DTO.JoinResponseDTO;
import at.fhv.spiel_backend.DTO.WaitingReadyDTO;
import at.fhv.spiel_backend.DTO.MoveRequestDTO;
import at.fhv.spiel_backend.DTO.ShootProjectileDTO;
import at.fhv.spiel_backend.DTO.ChangeWeaponDTO;
import at.fhv.spiel_backend.DTO.UseGadgetDTO;
import at.fhv.spiel_backend.DTO.LeaveRoomDTO;
import at.fhv.spiel_backend.DTO.RoomIdOnlyDTO;
import at.fhv.spiel_backend.logic.DefaultGameLogic;
import at.fhv.spiel_backend.model.Gadget;
import at.fhv.spiel_backend.model.Player;
import at.fhv.spiel_backend.server.game.GameRoomImpl;
import at.fhv.spiel_backend.model.Position;
import at.fhv.spiel_backend.server.game.IGameRoom;
import at.fhv.spiel_backend.server.room.IRoomManager;
import com.corundumstudio.socketio.SocketIOServer;
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
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname("localhost");
        config.setPort(8081);
        config.setOrigin("http://localhost:5173");

        SocketIOServer server = new SocketIOServer(config);
        log.info("Starting SocketIOServer on {}:{} with origin {}",
                config.getHostname(), config.getPort(), config.getOrigin());

        // joinRoom
        server.addEventListener("joinRoom", JoinRequestDTO.class, (client, data, ack) -> {
            String roomId = roomManager.assignToRoom(data.getPlayerId(), data.getBrawlerId(), data.getLevelId(), data.getPlayerName());
            client.joinRoom(roomId);
            // initial state
            roomManager.getRoom(roomId).buildStateUpdate();
            server.getRoomOperations(roomId).sendEvent("stateUpdate", roomManager.getRoom(roomId).buildStateUpdate());

            if (data.getChosenWeapon() != null) {
                roomManager.getRoom(roomId).getGameLogic().setPlayerWeapon(data.getPlayerId(), data.getChosenWeapon());
            }
            if (data.getChosenGadget() != null) {
                roomManager.getRoom(roomId).getGameLogic().setPlayerGadget(data.getPlayerId(), data.getChosenGadget());
            }
            ack.sendAckData(new JoinResponseDTO(roomId));
        });

        // waitingReady
        server.addEventListener("waitingReady", WaitingReadyDTO.class,
                (client, data, ack) -> {
                    String incoming = data.getRoomId();
                    IGameRoom room = roomManager.getRoom(incoming);
                    if (room == null) {
                        String newId = roomManager.assignToRoom(
                                data.getPlayerId(), data.getBrawlerId(), data.getLevelId(), data.getPlayerName());
                        client.joinRoom(newId);
                        ack.sendAckData(new JoinResponseDTO(newId));
                        room = roomManager.getRoom(newId);
                        log.info("Auto-created room {} for player {}", newId, data.getPlayerId());
                    } else {
                        ack.sendAckData("ok");
                    }
                    room.markReady(data.getPlayerId(), data.getBrawlerId());
                    if (room.getReadyCount() == room.getPlayerCount() && room.getPlayerCount() == room.getMaxPlayers()) {
                        server.getRoomOperations(room.getId()).sendEvent("startGame");
                        room.start();
                    }
                }
        );

        // move
        server.addEventListener("move", MoveRequestDTO.class,
                (client, data, ack) -> {
                    GameRoomImpl room = (GameRoomImpl) roomManager.getRoom(data.getRoomId());
                    if (room == null) {
                        log.warn("Received MOVE for unknown room {}", data.getRoomId());
                        ack.sendAckData("error: room_not_found");
                        return;
                    }
                    if (room.getGameLogic().getPlayer(data.getPlayerId()).getCurrentHealth() <= 0) {
                        ack.sendAckData("dead");
                        return;
                    }
                    room.setPlayerInput(data.getPlayerId(), data.getDirX(), data.getDirY(), data.getAngle());
                    ack.sendAckData("ok");
                }
        );

        // shootProjectile
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
                    ack.sendAckData("ok");
                }
        );

        // changeWeapon
        server.addEventListener("changeWeapon", ChangeWeaponDTO.class,
                (client, data, ack) -> {
                    IGameRoom room = roomManager.getRoom(data.getRoomId());
                    room.getGameLogic().setPlayerWeapon(data.getPlayerId(), data.getProjectileType());
                    ack.sendAckData("ok");
                }
        );

        // useGadget
        server.addEventListener("useGadget", UseGadgetDTO.class,
                (client, data, ack) -> {
                    GameRoomImpl room = (GameRoomImpl) roomManager.getRoom(data.getRoomId());
                    DefaultGameLogic logic = (DefaultGameLogic) room.getGameLogic();
                    Player p = logic.getPlayer(data.getPlayerId());
                    Gadget g = room.getGameLogic().getGadget(data.getPlayerId());
                    if (g == null) {
                        ack.sendAckData("error:no_gadget");
                        return;
                    }
                    if (g.getRemainingUses() <= 0) {
                        ack.sendAckData("error:no_uses_left");
                        return;
                    }
                    if (g.getTimeRemaining() > 0) {
                        ack.sendAckData("error:cooldown");
                        return;
                    }
                    long now = System.currentTimeMillis();
                    switch (g.getType()) {
                        case SPEED_BOOST:
                            p.setSpeedBoostActive(true);
                            break;
                        case HEALTH_BOOST:
                            p.setMaxHealth(p.getMaxHealth() + Player.HP_BOOST_AMOUNT);
                            p.setCurrentHealth(
                                    Math.min(p.getMaxHealth(),
                                            p.getCurrentHealth() + Player.HP_BOOST_AMOUNT)
                            );
                            break;
                        case DAMAGE_BOOST:
                            p.setDamageBoostEndTime(now + 10_000);
                            break;
                    }
                    g.setTimeRemaining(10_000L);
                    g.setRemainingUses(g.getRemainingUses() - 1);
                    ack.sendAckData("ok");
                }
        );

        // leaveRoom
        server.addEventListener("leaveRoom", LeaveRoomDTO.class, (client, data, ack) -> {
            roomManager.removeFromRoom(data.getPlayerId());
            log.info("Player {} left the room", data.getPlayerId());
            ack.sendAckData("ok");
        });

        // matchOver
        server.addEventListener("matchOver", RoomIdOnlyDTO.class, (client, data, ack) -> {
            log.info("Match over in room {}", data.getRoomId());
            server.getRoomOperations(data.getRoomId()).sendEvent("matchOver");
            ack.sendAckData("ok");
        });

        server.addDisconnectListener(client -> {
            String playerId = client.getHandshakeData().getSingleUrlParam("playerId");
            if (playerId != null) {
                roomManager.removeFromRoom(playerId);
                log.info("Player {} disconnected and was removed from their room", playerId);
            }
        });

        server.start();
        return server;
    }
}
