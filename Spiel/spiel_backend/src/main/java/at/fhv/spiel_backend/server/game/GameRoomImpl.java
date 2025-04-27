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

    private static final int MAX_PLAYERS = 2;

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
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("Room " + id + " is full");
        }
        players.put(playerId, new Object());
        gameLogic.addPlayer(playerId);
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
    public void start() {
        final long tickMs = 16;
        final float tickSec = tickMs / 1000f;

        executor.scheduleAtFixedRate(() -> {
            // 1) Logik vorwärts treiben
            gameLogic.update(tickSec);

            // 2) neuen State holen und senden
            StateUpdateMessage update = buildStateUpdate();
            eventPublisher.publish(id, update);
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
        readyPlayers.put(playerId, new Object());
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
