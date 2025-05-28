package at.fhv.spiel_backend.config;

import at.fhv.spiel_backend.DTO.*;
import at.fhv.spiel_backend.logic.DefaultGameLogic;
import at.fhv.spiel_backend.model.Gadget;
import at.fhv.spiel_backend.model.Player;
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

    @Bean(destroyMethod = "stop")
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
            String roomId = roomManager.assignToRoom(data.getPlayerId(), data.getBrawlerId(), data.getLevelId(), data.getPlayerName());
            IGameRoom room = roomManager.getRoom(roomId);
            client.joinRoom(roomId);
            server.getRoomOperations(roomId).sendEvent("stateUpdate", room.buildStateUpdate());

            if (data.getChosenWeapon() != null) {
                 room.getGameLogic().setPlayerWeapon(
                         data.getPlayerId(),
                         data.getChosenWeapon());
            }
            if (data.getChosenGadget() != null) {
                room.getGameLogic().setPlayerGadget(
                        data.getPlayerId(),
                        data.getChosenGadget());
            }


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
                                data.getPlayerId(), data.getBrawlerId(), data.getLevelId(), data.getPlayerName()
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
                    if (room.getReadyCount() == room.getPlayerCount() && room.getPlayerCount() == room.getMaxPlayers()) {
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
                    if (room.getGameLogic().getPlayer(data.getPlayerId()).getCurrentHealth() <= 0) {
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

        server.addDisconnectListener(client -> {
            String playerId = client.getHandshakeData().getSingleUrlParam("playerId");
            if (playerId != null) {
                roomManager.removeFromRoom(playerId);
                log.info("Player {} disconnected and was removed from their room", playerId);
            }
        });

        server.addEventListener("leaveRoom", WaitingReadyDTO.class, (client, data, ack) -> {
            String playerId = data.getPlayerId();
            roomManager.removeFromRoom(playerId);
            log.info("Player {} left the room voluntarily", playerId);
            ack.sendAckData("ok");
        });


        // Gadget-event
        server.addEventListener("useGadget", UseGadgetDTO.class, (client, data, ack) -> {
            System.out.println("[INFO] Gadget used for " + data.getPlayerId());
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

                case HP_BOOST:
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

            // Gemeinsames Starten von Cooldown & Use-Zähler
            g.setTimeRemaining(10_000L);                        // 10 Sekunden Buff-Laufzeit
            g.setRemainingUses(g.getRemainingUses() - 1);      // Use reduzieren
            System.out.println("Gadget "+ g.getType() +" used for " + data.getPlayerId());
            System.out.println("Remaining uses: " + g.getRemainingUses());

            ack.sendAckData("ok");
        });
        server.start();
        return server;
    }
}
