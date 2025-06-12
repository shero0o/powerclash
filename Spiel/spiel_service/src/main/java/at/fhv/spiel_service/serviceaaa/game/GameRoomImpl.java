// src/main/java/at/fhv/spiel_service/serviceaaa/game/GameRoomImpl.java
package at.fhv.spiel_service.serviceaaa.game;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.NPC;
import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Position;
import at.fhv.spiel_service.factoryaaa.IMapFactory;
import at.fhv.spiel_service.messaging.EventPublisher;
import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.serviceaaa.game.core.IGameRoom;
import at.fhv.spiel_service.services.core.IGameLogicService;
import at.fhv.spiel_service.services.managers.npc.INPCService;
import at.fhv.spiel_service.services.managers.zone.IZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GameRoomImpl implements IGameRoom {
    private final String id = UUID.randomUUID().toString();
    private final String levelId;

    private final Map<String,Object> players      = new ConcurrentHashMap<>();
    private final Map<String,Object> readyPlayers = new ConcurrentHashMap<>();
    private final Map<String,PlayerInput> inputs  = new ConcurrentHashMap<>();

    private final IMapFactory       mapFactory;
    private final IGameLogicService gameLogic;
    private final INPCService       npcService;
    private final IZoneService      zoneService;
    private final EventPublisher    eventPublisher;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final GameMap gameMap;
    private static final int    MAX_PLAYERS = 4;
    private static final float  TICK_DT     = 0.016f; // ~60 tps

    private boolean started    = false;
    private boolean hasStarted = false;


    public GameRoomImpl(IMapFactory         mapFactory,
                        IGameLogicService   gameLogic,
                        INPCService         npcService,
                        IZoneService        zoneService,
                        EventPublisher      eventPublisher,
                        String              levelId) {
        this.mapFactory     = mapFactory;
        this.gameLogic      = gameLogic;
        this.npcService     = npcService;
        this.zoneService    = zoneService;
        this.eventPublisher = eventPublisher;
        this.levelId        = levelId;

        this.gameMap = mapFactory.create(levelId);
        gameLogic.initGame(this.gameMap);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLevelId() {
        return levelId;
    }

    @Override
    public int getPlayerCount() {
        return players.size();
    }

    @Override
    public boolean isFull() {
        return players.size() >= MAX_PLAYERS;
    }


    public Map<String,Object> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    @Override
    public IGameLogicService getGameLogic() {
        return gameLogic;
    }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        return gameLogic.buildStateUpdate();
    }

    @Override
    public void addPlayer(String playerId, String brawlerId, String playerName) {
        if (hasStarted) {
            throw new IllegalStateException("Cannot join: Game already started.");
        }
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("Room " + id + " is full");
        }
        gameLogic.addPlayer(playerId, brawlerId, playerName);
        players.put(playerId, new Object());
        // push initial state
        eventPublisher.publish(id, gameLogic.buildStateUpdate());
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
        readyPlayers.remove(playerId);
        inputs.remove(playerId);
        gameLogic.removePlayer(playerId);
    }

    @Override
    public void markReady(String playerId, String brawlerId) {
        readyPlayers.putIfAbsent(playerId, new Object());
    }

    @Override
    public int getReadyCount() {
        return readyPlayers.size();
    }

    @Override
    public void setPlayerInput(String playerId, float dirX, float dirY, float angle) {
        inputs.put(playerId, new PlayerInput(dirX, dirY, angle));
    }

    @Override
    public synchronized void start() {
        if (started || getPlayerCount() < MAX_PLAYERS) return;
        started = true;
        hasStarted = true;

        if ("level3".equals(levelId)) {
            float cx = gameMap.getWidthInPixels()  / 2f;
            float cy = gameMap.getHeightInPixels() / 2f;

            npcService.addNpc("zombie-1",
                    new NPC("zombie-1", new Position(cx, cy, 0),
                            50, 32f, 10, 100f, 1000L));
            npcService.addNpc("zombie-2",
                    new NPC("zombie-2", new Position(cx, cy, 0),
                            50, 32f, 10, 100f, 1000L));

            float initialRadius = (float) Math.hypot(cx, cy);
            zoneService.activateZone(new Position(cx, cy, 0),
                    initialRadius,
                    2 * 60_000L);
        }

        executor.scheduleAtFixedRate(() -> {
            try {
                // 1) buffered movement
                players.keySet().forEach(pid -> {
                    PlayerInput in = inputs.get(pid);
                    if (in == null) return;
                    gameLogic.movePlayer(pid, in.dirX, in.dirY, in.angle);
                });

                // 2) full tick
                gameLogic.update(TICK_DT);

                // 3) broadcast
                eventPublisher.publish(id, gameLogic.buildStateUpdate());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 0, (long)(TICK_DT * 1000), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean hasGameStarted() {
        return hasStarted;
    }

    @Override
    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }

    // simple DTO for buffering inputs
    private static class PlayerInput {
        final float dirX, dirY, angle;
        PlayerInput(float dirX, float dirY, float angle) {
            this.dirX = dirX;
            this.dirY = dirY;
            this.angle = angle;
        }
    }
}
