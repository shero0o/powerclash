package at.fhv.spiel_service.ws;

import at.fhv.spiel_service.config.GameConstants;
import at.fhv.spiel_service.domain.Gadget;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.dto.*;
import at.fhv.spiel_service.service.game.logic.DefaultIGameLogic;
import at.fhv.spiel_service.service.gameSession.GameSessionImpl;
import at.fhv.spiel_service.service.gameSession.IGameSession;
import at.fhv.spiel_service.service.room.IRoomService;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static at.fhv.spiel_service.domain.GadgetType.DAMAGE_BOOST;

@Component
public class SocketIOEvents {
    private static final Logger log = LoggerFactory.getLogger(SocketIOEvents.class);
    private final IRoomService roomManager;

    public SocketIOEvents(SocketIOServer server, @Lazy IRoomService roomManager) {
        this.roomManager = roomManager;
        registerEvents(server);
    }

    private void registerEvents(SocketIOServer server) {
        server.addEventListener("joinRoom", JoinRequestDTO.class, this::onJoinRoom);
        server.addEventListener("waitingReady", WaitingReadyDTO.class, this::onWaitingReady);
        server.addEventListener("move", MoveRequestDTO.class, this::onMove);
        server.addEventListener("shootProjectile", ShootProjectileDTO.class, this::onShootProjectile);
        server.addEventListener("changeWeapon", ChangeWeaponDTO.class, this::onChangeWeapon);
        server.addEventListener("useGadget", UseGadgetDTO.class, this::onUseGadget);
        server.addEventListener("leaveRoom", LeaveRoomDTO.class, this::onLeaveRoom);
        server.addEventListener("matchOver", RoomIdOnlyDTO.class, this::onMatchOver);

        server.addDisconnectListener(client -> {
            String playerId = client.getHandshakeData().getSingleUrlParam("playerId");
            if (playerId != null) {
                roomManager.removePlayerFromRoom(playerId);
                LoggerFactory.getLogger(SocketIOEvents.class).info("Player {} disconnected and was removed from room", playerId);
            }
        });
    }
    private void onJoinRoom(SocketIOClient client, JoinRequestDTO data, AckRequest ack) {
        String roomId = roomManager.assignPlayerToRoom(data.getPlayerId(), data.getBrawlerId(), data.getLevelId(), data.getPlayerName());
        client.joinRoom(roomId);
        roomManager.getGameRoom(roomId).buildStateUpdate();
        client.getNamespace().getRoomOperations(roomId).sendEvent("stateUpdate", roomManager.getGameRoom(roomId).buildStateUpdate());

        if (data.getChosenWeapon() != null) {
            roomManager.getGameRoom(roomId).getGameLogic().setPlayerWeapon(data.getPlayerId(), data.getChosenWeapon());
        }
        if (data.getChosenGadget() != null) {
            roomManager.getGameRoom(roomId).getGameLogic().setPlayerGadget(data.getPlayerId(), data.getChosenGadget());
        }

        ack.sendAckData(new JoinResponseDTO(roomId));
    }

    private void onWaitingReady(SocketIOClient client, WaitingReadyDTO data, AckRequest ack) {
        String incoming = data.getRoomId();
        IGameSession room = roomManager.getGameRoom(incoming);
        if (room == null) {
            String newId = roomManager.assignPlayerToRoom(data.getPlayerId(), data.getBrawlerId(), data.getLevelId(), data.getPlayerName());
            client.joinRoom(newId);
            ack.sendAckData(new JoinResponseDTO(newId));
            room = roomManager.getGameRoom(newId);
            log.info("Auto-created room {} for player {}", newId, data.getPlayerId());
        } else {
            ack.sendAckData("ok");
        }

        room.markReady(data.getPlayerId(), data.getBrawlerId());
        if (room.getReadyCount() == room.getPlayerCount() && room.getPlayerCount() == room.getMaxPlayers()) {
            client.getNamespace().getRoomOperations(room.getId()).sendEvent("startGame");
            room.start();
        }
    }

    private void onMove(SocketIOClient client, MoveRequestDTO data, AckRequest ack) {
        GameSessionImpl room = (GameSessionImpl) roomManager.getGameRoom(data.getRoomId());
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

    private void onShootProjectile(SocketIOClient client, ShootProjectileDTO data, AckRequest ack) {
        IGameSession room = roomManager.getGameRoom(data.getRoomId());
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

    private void onChangeWeapon(SocketIOClient client, ChangeWeaponDTO data, AckRequest ack) {
        IGameSession room = roomManager.getGameRoom(data.getRoomId());
        if (room != null) {
            room.getGameLogic().setPlayerWeapon(data.getPlayerId(), data.getProjectileType());
        }
        ack.sendAckData("ok");
    }

    private void onUseGadget(SocketIOClient client, UseGadgetDTO data, AckRequest ack) {
        GameSessionImpl room = (GameSessionImpl) roomManager.getGameRoom(data.getRoomId());
        DefaultIGameLogic logic = (DefaultIGameLogic) room.getGameLogic();
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
            case SPEED_BOOST -> p.setSpeedBoostActive(true);
            case HEALTH_BOOST -> {
                p.setMaxHealth(p.getMaxHealth() + GameConstants.HP_BOOST_AMOUNT);
                p.setCurrentHealth(Math.min(p.getMaxHealth(), p.getCurrentHealth() + GameConstants.HP_BOOST_AMOUNT));
            }
            case DAMAGE_BOOST -> p.setDamageBoostEndTime(now + 10_000);
        }

        g.setTimeRemaining(10_000L);
        g.setRemainingUses(g.getRemainingUses() - 1);
        ack.sendAckData("ok");
    }

    private void onLeaveRoom(SocketIOClient client, LeaveRoomDTO data, AckRequest ack) {
        roomManager.removePlayerFromRoom(data.getPlayerId());
        log.info("Player {} left the room", data.getPlayerId());
        ack.sendAckData("ok");
    }

    private void onMatchOver(SocketIOClient client, RoomIdOnlyDTO data, AckRequest ack) {
        log.info("Match over in room {}", data.getRoomId());
        client.getNamespace().getRoomOperations(data.getRoomId()).sendEvent("matchOver");
        ack.sendAckData("ok");
    }

    private void onDisconnect(SocketIOClient client) {
        String playerId = client.getHandshakeData().getSingleUrlParam("playerId");
        if (playerId != null) {
            roomManager.removePlayerFromRoom(playerId);
            log.info("Player {} disconnected and was removed from their room", playerId);
        }
    }

}
