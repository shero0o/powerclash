// GameRoomImpl.java
package at.fhv.spiel_backend.server.game;

import at.fhv.spiel_backend.logic.DefaultGameLogic;
import at.fhv.spiel_backend.logic.GameLogic;
import at.fhv.spiel_backend.model.Player;
import at.fhv.spiel_backend.model.Position;
import at.fhv.spiel_backend.server.EventPublisher;
import at.fhv.spiel_backend.server.map.IMapFactory;
import at.fhv.spiel_backend.ws.StateUpdateMessage;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import at.fhv.spiel_backend.server.map.GameMap;

public class GameRoomImpl implements IGameRoom {
    private final String id = UUID.randomUUID().toString();
    private final Map<String, Object> players = new ConcurrentHashMap<>();
    private final Map<String, Object> readyPlayers = new ConcurrentHashMap<>();
    private final Map<String, PlayerInput> inputs = new ConcurrentHashMap<>();

    private final IMapFactory mapFactory;
    private final DefaultGameLogic gameLogic;
    private final EventPublisher eventPublisher;
    private final GameMap gameMap;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static final int MAX_PLAYERS = 2;
    private static final float MAX_SPEED = 300f; // pixels per second
    private static final float TICK_DT    = 0.016f;

    public GameRoomImpl(IMapFactory mapFactory, DefaultGameLogic gameLogic, EventPublisher eventPublisher) {
        this.mapFactory = mapFactory;
        this.gameLogic = gameLogic;
        this.eventPublisher = eventPublisher;
        this.gameMap = mapFactory.create("level1");
        this.gameLogic.setGameMap(this.gameMap);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void addPlayer(String playerId) {
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("Room " + id + " is full");
        }
        players.computeIfAbsent(playerId, pid -> {
            gameLogic.addPlayer(pid);
            return new Object();
        });
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
        readyPlayers.remove(playerId);
        inputs.remove(playerId);
        gameLogic.removePlayer(playerId);
    }

    @Override
    public int getPlayerCount() {
        return players.size();
    }

    @Override
    public boolean isFull() {
        return players.size() >= MAX_PLAYERS;
    }

    @Override
    public void markReady(String playerId) {
        if (!readyPlayers.containsKey(playerId)) {
            readyPlayers.put(playerId, new Object());
        }
    }

    @Override
    public int getReadyCount() {
        return readyPlayers.size();
    }

    @Override
    public GameLogic getGameLogic() {
        return gameLogic;
    }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        return gameLogic.buildStateUpdate();
    }

    @Override
    public Map<String, Object> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    public void setPlayerInput(String playerId, float dirX, float dirY, float angle) {
        inputs.put(playerId, new PlayerInput(dirX, dirY, angle));
    }

    private void broadcastState() {
        StateUpdateMessage update = buildStateUpdate();
        eventPublisher.publish(id, update);
    }

    public void handleAttack(String playerId, float dirX, float dirY, float angle) {
        gameLogic.attack(playerId, dirX, dirY, angle);
        broadcastState();
    }

    @Override
    public void start() {
        executor.scheduleAtFixedRate(() -> {
            try {
                inputs.forEach((pid, in) -> {
                    Player p = gameLogic.getPlayer(pid);
                    if (p == null || p.getCurrentHealth() <= 0) return;

                    Position pos = p.getPosition();
                    float len = (float) Math.hypot(in.dirX, in.dirY);
                    float nx  = len > 0 ? in.dirX / len : 0;
                    float ny  = len > 0 ? in.dirY / len : 0;

                    float newX = pos.getX() + nx * MAX_SPEED * TICK_DT;
                    float newY = pos.getY() + ny * MAX_SPEED * TICK_DT;
                    int tileX = (int) (newX / gameMap.getTileWidth());
                    int tileY = (int) (newY / gameMap.getTileHeight());

                    if (!gameMap.isWallAt(tileX, tileY)) {
                        pos.setX(newX);
                        pos.setY(newY);
                    }
                    pos.setAngle(in.angle);
                });

                gameLogic.updateBullets(TICK_DT);
                broadcastState();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, (long)(TICK_DT * 1000), TimeUnit.MILLISECONDS);
    }

    private static class PlayerInput {
        final float dirX, dirY, angle;
        PlayerInput(float dx, float dy, float a) {
            this.dirX = dx; this.dirY = dy; this.angle = a;
        }
    }
}
