package at.fhv.spiel_service.service.room;

import at.fhv.spiel_service.domain.*;
import at.fhv.spiel_service.messaging.EventPublisher;
import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.service.game.logic.IGameLogic;
import at.fhv.spiel_service.service.game.logic.DefaultIGameLogic;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final String levelId;
    private final Map<String, Object> players      = new ConcurrentHashMap<>();
    private final Map<String, Object> readyPlayers = new ConcurrentHashMap<>();
    private final Map<String, PlayerInput> inputs  = new ConcurrentHashMap<>();

    private final IGameLogic IGameLogic;
    private final EventPublisher eventPublisher;
    private final GameMap gameMap;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static final int MAX_PLAYERS = 4;
    private static final float MAX_SPEED   = 300f;      // pixels/sec for movement
    private static final float TICK_DT     = 0.016f;   // ~60 ticks/sec

    // Guard so we only start the loop once
    private boolean started = false;
    private boolean hasStarted = false;


    public GameRoomImpl(IGameLogic gameLogic,
                        EventPublisher eventPublisher,
                        String levelId) {
        this.IGameLogic = gameLogic;
        this.eventPublisher = eventPublisher;
        this.levelId = levelId;
        this.gameMap = new GameMap(levelId);
        this.IGameLogic.setGameMap(this.gameMap);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLevelId() {
        return this.levelId;
    }

    @Override
    public void addPlayer(String playerId, String brawlerId, String playerName) {
        if (hasStarted) {
            throw new IllegalStateException("Cannot join: Game already started.");
        }
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("Room " + id + " is full");
        }
        if (playerId == null) {
            System.err.println("[ERROR] addPlayer called with null ID");
            return;
        }

        // 1) register in logic
        IGameLogic.addPlayer(playerId, brawlerId, playerName);
        // 2) track locally
        players.put(playerId, new Object());
        System.out.println("[INFO] Player added: " + playerId);

        // ↓↓↓ new: immediately push the very first snapshot ↓↓↓
        StateUpdateMessage initial = IGameLogic.buildStateUpdate();
        eventPublisher.publish(id, initial);
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
        IGameLogic.removePlayer(playerId);
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
    public void markReady(String playerId, String brawlerId) {
        if (!readyPlayers.containsKey(playerId)) {
            readyPlayers.put(playerId, new Object());
        }
    }

    @Override
    public int getReadyCount() {
        return readyPlayers.size();
    }

    @Override
    public IGameLogic getGameLogic() {
        return IGameLogic;
    }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        return IGameLogic.buildStateUpdate();
    }

    @Override
    public Map<String, Object> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    /**
     * Buffer latest movement input for next tick
     */
    public void setPlayerInput(String playerId, float dirX, float dirY, float angle) {
        inputs.put(playerId, new PlayerInput(dirX, dirY, angle));
    }

    /**
     * Spawn projectile immediately
     */
    public void handleFire(String playerId,
                           Position position,
                           Position direction,
                           ProjectileType type) {
        IGameLogic.spawnProjectile(playerId, position, direction, type);
    }

    /**
     * Start the main game loop; safe to call multiple times but will only schedule once
     */
    @Override
    public synchronized void start() {
        if (started || getPlayerCount() < MAX_PLAYERS) return;
        started = true;
        hasStarted = true;

        // Only for level 3: spawn two melee‐NPCs (“zombies”) at map center
        if ("level3".equals(levelId) && IGameLogic instanceof DefaultIGameLogic) {
            DefaultIGameLogic logic = (DefaultIGameLogic) IGameLogic;

            // compute center of map in pixels
            float cx = gameMap.getWidthInPixels()  / 2f;
            float cy = gameMap.getHeightInPixels() / 2f;

            // spawn two NPCs at center: id, position, health, attackRadius, damage, speed, cooldown
            logic.addNpc("zombie-1", new Position(3200, 3200, 0),
                    /*health*/ 50,
                    /*attackRadius*/ 32f,
                    /*damage*/ 10,
                    /*speed*/ 100f,
                    /*attackCooldownMs*/ 1000L);

            logic.addNpc("zombie-2", new Position(cx, cy, 0),
                    50, 32f, 10, 100f, 1000L);

            // activate safe‐zone shrink for level 3
            float initialRadius = (float) Math.hypot(cx, cy);
            long durationMs     = 2 * 60_000L;
            logic.activateZone(new Position(cx, cy, 0), initialRadius, durationMs);
        }

        // schedule the main game loop at ~60 ticks/sec
        executor.scheduleAtFixedRate(() -> {
            try {
                // 1) process buffered player inputs
                for (var entry : inputs.entrySet()) {
                    String pid = entry.getKey();
                    PlayerInput in = entry.getValue();
                    Player p = IGameLogic.getPlayer(pid);
                    if (p == null || p.getCurrentHealth() <= 0) continue;

                    Position pos = p.getPosition();
                    float len = (float) Math.hypot(in.dirX, in.dirY);
                    float nx  = len > 0 ? in.dirX / len : 0;
                    float ny  = len > 0 ? in.dirY / len : 0;

                    Gadget gadget = IGameLogic.getGadget(pid);
                    float speedFactor = (gadget != null && gadget.getTimeRemaining() > 0 && gadget.getType() == GadgetType.SPEED_BOOST)
                            ? 2
                            : 1;

                    if (gadget != null && gadget.getTimeRemaining() > 0) {
                        // HP-Boost starten
                        if (gadget.getType() == GadgetType.HEALTH_BOOST) {
                            p.setHpBoostActive(true);
                        }
                        // Damage-Boost starten
                        if (gadget.getType() == GadgetType.DAMAGE_BOOST) {
                            p.setDamageBoostActive(true);
                        }
                    } else if (gadget != null && gadget.getTimeRemaining() == 0) {
                        // Buffs ablaufen lassen
                        if (p.isHpBoostActive()) {
                            p.setHpBoostActive(false);
                            // MaxHP zurücksetzen
                            p.setMaxHealth(p.getMaxHealth() - Player.HP_BOOST_AMOUNT);
                            if (p.getCurrentHealth() > p.getMaxHealth()) {
                                p.setCurrentHealth(p.getMaxHealth());
                            }
                        }
                        if (p.isDamageBoostActive()) {
                            p.setDamageBoostActive(false);
                        }
                    }

                    float newX = pos.getX() + nx * MAX_SPEED * speedFactor * TICK_DT;
                    float newY = pos.getY() + ny * MAX_SPEED * speedFactor * TICK_DT;

                    int tx = (int)(newX / gameMap.getTileWidth());
                    int ty = (int)(newY / gameMap.getTileHeight());
                    if (!gameMap.isWallAt(tx, ty)) {
                        IGameLogic.movePlayer(pid, newX, newY, in.angle);
                    }

                    // Runterzählen des Gadget-Timers (pro Tick einmal TICK_DT*1000 ms)
                    if (gadget != null && gadget.getTimeRemaining() > 0) {
                        long updated = gadget.getTimeRemaining() - (long)(TICK_DT * 1000);
                        gadget.setTimeRemaining(Math.max(0, updated));
                    }
                }

                // 2) update projectiles & apply environmental effects (including NPC AI)
                IGameLogic.updateProjectiles(TICK_DT);
                IGameLogic.applyEnvironmentalEffects();

                // 3) broadcast the new state to all clients in this room
                StateUpdateMessage update = buildStateUpdate();
                eventPublisher.publish(id, update);

            } catch (Exception e) {
                System.err.println("[ERROR] Game loop exception: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, (long)(TICK_DT * 1000), TimeUnit.MILLISECONDS);
    }


    // DTO for input buffering
    private static class PlayerInput {
        final float dirX, dirY, angle;
        PlayerInput(float dx, float dy, float a) {
            this.dirX = dx;
            this.dirY = dy;
            this.angle = a;
        }
    }

    public boolean hasGameStarted() {
        return hasStarted;
    }

    public int getMaxPlayers(){
        return MAX_PLAYERS;
    }


}
