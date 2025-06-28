package at.fhv.spiel_service.service.gameSession;

import at.fhv.spiel_service.domain.*;
import at.fhv.spiel_service.messaging.EventPublisher;
import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.service.game.gameLoop.GameLoop;
import at.fhv.spiel_service.service.game.logic.IGameLogic;
import at.fhv.spiel_service.service.game.logic.DefaultIGameLogic;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameSessionImpl implements IGameSession {
    private final String id        = UUID.randomUUID().toString();
    private final String levelId;
    private final Map<String,Object> players      = new ConcurrentHashMap<>();
    private final Map<String,Object> readyPlayers = new ConcurrentHashMap<>();

    private final IGameLogic    logic;
    private final EventPublisher publisher;
    private final GameMap       gameMap;
    private final GameLoop loop;

    private static final int MAX_PLAYERS = 4;

    public GameSessionImpl(EventPublisher publisher, String levelId) {
        this.logic     = new DefaultIGameLogic();
        this.publisher = publisher;
        this.levelId   = levelId;
        this.gameMap   = new GameMap(levelId);
        this.logic.setGameMap(this.gameMap);
        this.loop      = new GameLoop(id, logic, publisher, gameMap);
    }

    @Override public String getId()              { return id; }
    @Override public String getLevelId()         { return levelId; }
    @Override public int    getPlayerCount()     { return players.size(); }
    @Override public boolean isFull()            { return players.size() >= MAX_PLAYERS; }
    @Override public int    getMaxPlayers()      { return MAX_PLAYERS; }
    @Override public IGameLogic getGameLogic()   { return logic; }
    @Override public Map<String,Object> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    @Override
    public void addPlayer(String playerId, String brawlerId, String playerName) {
        if (hasGameStarted())    throw new IllegalStateException("Game already started");
        if (isFull())            throw new IllegalStateException("Room is full");
        logic.addPlayer(playerId, brawlerId, playerName);
        players.put(playerId, new Object());
        StateUpdateMessage init = logic.buildStateUpdate();
        publisher.publish(id, init);
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
        readyPlayers.remove(playerId);
        logic.removePlayer(playerId);
        loop.removePlayer(playerId);
    }

    @Override
    public void markReady(String playerId, String brawlerId) {
        readyPlayers.put(playerId, new Object());
    }
    @Override public int getReadyCount()          { return readyPlayers.size(); }

    @Override
    public void start() {
        if (hasGameStarted() || getPlayerCount() < MAX_PLAYERS) return;
        if ("level3".equals(levelId) && logic instanceof DefaultIGameLogic) {
            DefaultIGameLogic lg = (DefaultIGameLogic) logic;
            float cx = gameMap.getWidthInPixels()/2f;
            float cy = gameMap.getHeightInPixels()/2f;
            lg.addNpc("zombie-1", new Position(cx, cy, 0), 50, 32f, 10, 100f, 1000L);
            lg.addNpc("zombie-2", new Position(cx, cy, 0), 50, 32f, 10, 100f, 1000L);
            lg.activateZone(new Position(cx, cy, 0), (float)Math.hypot(cx, cy), 2*60_000L);
        }
        loop.start();
    }

    @Override public boolean hasGameStarted()     { return loop.isRunning(); }

    @Override public StateUpdateMessage buildStateUpdate() {
        return logic.buildStateUpdate();
    }

    @Override
    public void setPlayerInput(String playerId, float dirX, float dirY, float angle) {
        loop.submitMovement(playerId, dirX, dirY, angle);
    }

    public void handleFire(String playerId, Position pos, Position dir, ProjectileType t) {
        logic.spawnProjectile(playerId, pos, dir, t);
    }

}
