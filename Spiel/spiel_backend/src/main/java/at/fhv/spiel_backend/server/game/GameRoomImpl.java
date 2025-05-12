package at.fhv.spiel_backend.server.game;

import at.fhv.spiel_backend.logic.GameLogic;
import at.fhv.spiel_backend.model.Player;
import at.fhv.spiel_backend.model.Position;
import at.fhv.spiel_backend.server.EventPublisher;
import at.fhv.spiel_backend.server.map.GameMap;
import at.fhv.spiel_backend.server.map.IMapFactory;
import at.fhv.spiel_backend.ws.StateUpdateMessage;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * GameRoom implementation that handles:
 * - Player join/leave with optional Brawler selection
 * - Ready-state tracking
 * - Input buffering for movement & rotation
 * - Projectile & game logic updates
 * - State broadcasting via EventPublisher
 */
public class GameRoomImpl implements IGameRoom {
    private final String id = UUID.randomUUID().toString();
    private final Map<String, Object> players     = new ConcurrentHashMap<>();
    private final Map<String, Object> readyPlayers= new ConcurrentHashMap<>();
    private final Map<String, PlayerInput> inputs = new ConcurrentHashMap<>();

    private final IMapFactory mapFactory;
    private final GameLogic gameLogic;
    private final EventPublisher eventPublisher;
    private final GameMap gameMap;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static final int MAX_PLAYERS = 2;
    private static final float MAX_SPEED   = 300f;      // pixels/sec for movement
    private static final float TICK_DT     = 0.016f;   // ~60 ticks/sec

    public GameRoomImpl(IMapFactory mapFactory,
                        GameLogic gameLogic,
                        EventPublisher eventPublisher,
                        String levelId) {
        this.mapFactory     = mapFactory;
        this.gameLogic      = gameLogic;
        this.eventPublisher = eventPublisher;
        this.gameMap        = mapFactory.create(levelId);
        this.gameLogic.setGameMap(this.gameMap);
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Adds a player with default Brawler setup
     */
    @Override
    public void addPlayer(String playerId) {
        addPlayer(playerId, null);
    }

    /**
     * Adds a player, optionally specifying Brawler
     */
    @Override
    public void addPlayer(String playerId, String brawlerId) {
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("Room " + id + " is full");
        }
        if (playerId == null) {
            System.err.println("[ERROR] addPlayer called with null ID");
            return;
        }
        // Only add once
        players.computeIfAbsent(playerId, pid -> {
            if (brawlerId == null) {
                gameLogic.addPlayer(pid);
            } else {
                gameLogic.addPlayer(pid, brawlerId);
            }
            return new Object();
        });
        System.out.println("[INFO] Player added: " + playerId);
    }

    @Override
    public void removePlayer(String playerId) {
        if (playerId == null) {
            System.err.println("[ERROR] removePlayer called with null ID");
            return;
        }
        players.remove(playerId);
        readyPlayers.remove(playerId);
        inputs.remove(playerId);
        gameLogic.removePlayer(playerId);
        System.out.println("[INFO] Player removed: " + playerId);
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
        if (playerId == null) {
            System.err.println("[ERROR] markReady called with null ID");
            return;
        }
        if (readyPlayers.size() < MAX_PLAYERS) {
            readyPlayers.putIfAbsent(playerId, new Object());
            System.out.println("[INFO] Player ready: " + playerId);
        } else {
            System.out.println("[WARN] Ready list full");
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

    /**
     * Stores the latest movement & rotation input for a player
     */
    public void setPlayerInput(String playerId, float dirX, float dirY, float angle) {
        inputs.put(playerId, new PlayerInput(dirX, dirY, angle));
    }

    /**
     * Handles firing/spawn of a projectile
     */
    public void handleFire(String playerId,
                           Position position,
                           Position direction,
                           at.fhv.spiel_backend.model.ProjectileType type) {
        gameLogic.spawnProjectile(playerId, position, direction, type);
    }

    /**
     * Starts the game loop: applies inputs, updates logic, broadcasts state
     */
    @Override
    public void start() {
        executor.scheduleAtFixedRate(() -> {
            try {
                // 1) Process movement inputs
                for (var entry : inputs.entrySet()) {
                    String pid = entry.getKey();
                    PlayerInput in = entry.getValue();
                    Player p = gameLogic.getPlayer(pid);
                    if (p == null || p.getCurrentHealth() <= 0) continue;

                    Position pos = p.getPosition();
                    float len = (float) Math.hypot(in.dirX, in.dirY);
                    float nx  = len > 0 ? in.dirX / len : 0;
                    float ny  = len > 0 ? in.dirY / len : 0;
                    float newX = pos.getX() + nx * MAX_SPEED * TICK_DT;
                    float newY = pos.getY() + ny * MAX_SPEED * TICK_DT;

                    int tx = (int)(newX / gameMap.getTileWidth());
                    int ty = (int)(newY / gameMap.getTileHeight());
                    if (!gameMap.isWallAt(tx, ty)) {
                        // Delegate to logic for angle-aware move
                        gameLogic.movePlayer(pid, newX, newY, in.angle);
                    }
                }

                // 2) Update projectiles & collisions
                gameLogic.updateProjectiles();

                // 3) Broadcast state to all clients
                StateUpdateMessage update = buildStateUpdate();
                eventPublisher.publish(id, update);

            } catch (Exception e) {
                System.err.println("[ERROR] Game loop exception: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, (long)(TICK_DT * 1000), TimeUnit.MILLISECONDS);
    }

    // --- Helper DTO for inputs ---
    private static class PlayerInput {
        final float dirX, dirY, angle;
        PlayerInput(float dx, float dy, float a) {
            this.dirX = dx;
            this.dirY = dy;
            this.angle = a;
        }
    }
}