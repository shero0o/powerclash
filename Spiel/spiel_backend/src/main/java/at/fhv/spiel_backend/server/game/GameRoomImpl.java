package at.fhv.spiel_backend.server.game;

import at.fhv.spiel_backend.handler.CommandProcessor;
import at.fhv.spiel_backend.logic.GameLogic;
import at.fhv.spiel_backend.server.EventPublisher;
import at.fhv.spiel_backend.server.map.GameMap;
import at.fhv.spiel_backend.server.map.IMapFactory;
import at.fhv.spiel_backend.ws.StateUpdateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Konkrete Implementierung des GameRoom.
 * Autowiring erfolgt über den einzigen Konstruktor.
 */
@Component
public class GameRoomImpl implements IGameRoom {

    private final String id;
    private final Map<String, Object> players;
    private final IMapFactory mapFactory;
    private final CommandProcessor commandProcessor;
    private final GameLogic gameLogic;
    private final EventPublisher eventPublisher;
    private final GameMap map;

    /** Maximale Spielerzahl pro Raum */
    private static final int MAX_PLAYERS = 4;

    /**
     * Konstruktor für Spring-Bean-Injektion.
     */
    @Autowired
    public GameRoomImpl(
            IMapFactory mapFactory,
            CommandProcessor commandProcessor,
            GameLogic gameLogic,
            EventPublisher eventPublisher
    ) {
        this.id = UUID.randomUUID().toString();
        this.players = new ConcurrentHashMap<>();
        this.mapFactory = mapFactory;
        this.commandProcessor = commandProcessor;
        this.gameLogic = gameLogic;
        this.eventPublisher = eventPublisher;
        this.map = this.mapFactory.create("default");
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
    }

    public void removePlayer(String playerId) {

    }

    public int getPlayerCount() {
        return players.size();
    }

    @Override
    public boolean isFull() {
        return players.size() >= MAX_PLAYERS;
    }

    public void start() {
    }


    public StateUpdateMessage buildStateUpdate() {
        return gameLogic.buildStateUpdate();
    }

    /**
     * Liefert eine unveränderliche Sicht auf die Spieler.
     */
    public Map<String, Object> getPlayers() {
        return Collections.unmodifiableMap(players);
    }
}
