package at.fhv.spiel_backend.server.game;

import at.fhv.spiel_backend.handler.CommandProcessor;
import at.fhv.spiel_backend.logic.GameLogic;
import at.fhv.spiel_backend.server.EventPublisher;
import at.fhv.spiel_backend.server.map.IMapFactory;
import at.fhv.spiel_backend.ws.StateUpdateMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class GameRoomImpl implements IGameRoom {
    private final String id = UUID.randomUUID().toString();
    private final Map<String, Object> players = new ConcurrentHashMap<>();
    private final Map<String, Object> readyPlayers = new ConcurrentHashMap<>();

    private final IMapFactory mapFactory;
    private final CommandProcessor commandProcessor;
    private final GameLogic gameLogic;
    private final EventPublisher eventPublisher;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static final int MAX_PLAYERS = 1;

    public GameRoomImpl(
            IMapFactory mapFactory,
            CommandProcessor commandProcessor,
            GameLogic gameLogic,
            EventPublisher eventPublisher
    ) {
        this.mapFactory = mapFactory;
        this.commandProcessor = commandProcessor;
        this.gameLogic = gameLogic;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void addPlayer(String playerId) {
        if (playerId == null) {
            System.out.println("[ERROR] Attempted to add player with null ID!");
            return;
        }
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("Room " + id + " is full");
        }
        if (players.containsKey(playerId)) {
            System.out.println("[WARN] Player already exists in room: " + playerId);
            return;
        }

        // Insert a proper player representation
        players.put(playerId, new Object());
        gameLogic.addPlayer(playerId);

        System.out.println("[INFO] Player successfully added: " + playerId);
    }

    @Override
    public void removePlayer(String playerId) {
        if (playerId == null) {
            System.out.println("[ERROR] Attempted to remove player with null ID!");
            return;
        }
        players.remove(playerId);
        gameLogic.removePlayer(playerId);

        System.out.println("[INFO] Player successfully removed: " + playerId);
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
    public void start() {
        final long tickMs = 16;
        final float tickSec = tickMs / 1000f;

        executor.scheduleAtFixedRate(() -> {
            try {
                gameLogic.update(tickSec);
                StateUpdateMessage update = buildStateUpdate();
                eventPublisher.publish(id, update);
            } catch (Exception e) {
                System.err.println("[ERROR] Exception in scheduled update: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, tickMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        return gameLogic.buildStateUpdate();
    }

    public Map<String, Object> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    @Override
    public void markReady(String playerId) {
        if (playerId == null) {
            System.out.println("[ERROR] markReady called with null playerId!");
            return;
        }
        readyPlayers.put(playerId, new Object());
        System.out.println("[INFO] Player marked as ready: " + playerId);
    }

    @Override
    public int getReadyCount() {
        return readyPlayers.size();
    }

    @Override
    public GameLogic getGameLogic(){
        return this.gameLogic;
    }
}
