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

public class GameRoomImpl implements IGameRoom {
    private final String id = UUID.randomUUID().toString();
    private final Map<String,Object> players      = new ConcurrentHashMap<>();
    private final Map<String,Object> readyPlayers = new ConcurrentHashMap<>();
    private final Map<String,PlayerInput> inputs  = new ConcurrentHashMap<>();

    private final IMapFactory     mapFactory;
    private final DefaultGameLogic gameLogic;
    private final EventPublisher  eventPublisher;
    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    private static final int   MAX_PLAYERS = 2;
    private static final float MAX_SPEED   = 200f;    // pixels/sec
    private static final float TICK_DT     = 0.016f;  // seconds per tick

    public GameRoomImpl(
            IMapFactory mapFactory,
            DefaultGameLogic gameLogic,
            EventPublisher eventPublisher
    ) {
        this.mapFactory     = mapFactory;
        this.gameLogic      = gameLogic;
        this.eventPublisher = eventPublisher;
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
        if (!players.containsKey(playerId)) {
            players.put(playerId, new Object());
            gameLogic.addPlayer(playerId);
        }
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
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
        if (readyPlayers.size() < MAX_PLAYERS && !readyPlayers.containsKey(playerId)) {
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
    public Map<String,Object> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    /**
     * Record the client’s last input; applied on next tick.
     */
    public void setPlayerInput(String playerId, float dirX, float dirY, float angle) {
        inputs.put(playerId, new PlayerInput(dirX, dirY, angle));
    }

    /**
     * Starts the authorative game loop (~60Hz). Applies
     * all stored inputs, moves players, logs pos+speed,
     * then broadcasts state.
     */
    @Override
    public void start() {
        // Run the game loop at ~60Hz
        executor.scheduleAtFixedRate(() -> {
                    try {
                        // 1) Apply all stored inputs
                        inputs.forEach((pid, in) -> {
                            Player p = ((DefaultGameLogic) gameLogic).getPlayer(pid);
                            if (p == null) return;
                            Position pos = p.getPosition();

                            // normalize direction vector
                            float len = (float) Math.hypot(in.dirX, in.dirY);
                            float nx  = len > 0 ? in.dirX / len : 0;
                            float ny  = len > 0 ? in.dirY / len : 0;

                            // advance by MAX_SPEED * deltaTime
                            pos.setX(pos.getX() + nx * MAX_SPEED * TICK_DT);
                            pos.setY(pos.getY() + ny * MAX_SPEED * TICK_DT);
                            pos.setAngle(in.angle);

                            // debug log actual position & speed
                            System.out.printf(
                                    "Room %s – Player %s @ x=%.1f y=%.1f speed=%.1f%n",
                                    id, pid, pos.getX(), pos.getY(), (len > 0 ? MAX_SPEED : 0f)
                            );
                        });

                        // 2) Broadcast the updated state once per tick
                        StateUpdateMessage update = buildStateUpdate();
                        eventPublisher.publish(id, update);

                    } catch (Exception e) {
                        // ensure exceptions don’t kill the loop
                        e.printStackTrace();
                    }
                },
                /* initial delay */ 0,
                /* period in ms */   (long)(TICK_DT * 1000),
                TimeUnit.MILLISECONDS);
    }


    // internal holder for last input
    private static class PlayerInput {
        final float dirX, dirY, angle;
        PlayerInput(float dx, float dy, float a) {
            this.dirX  = dx;
            this.dirY  = dy;
            this.angle = a;
        }
    }
}
