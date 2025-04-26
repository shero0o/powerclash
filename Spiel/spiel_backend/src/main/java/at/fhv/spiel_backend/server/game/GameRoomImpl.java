package at.fhv.spiel_backend.server.game;

import at.fhv.spiel_backend.handler.CommandProcessor;
import at.fhv.spiel_backend.logic.GameLogic;
import at.fhv.spiel_backend.server.EventPublisher;
import at.fhv.spiel_backend.server.map.GameMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GameRoomImpl implements IGameRoom {

    private final String id;
    private final Map<String, Object> players; // Platzhalter für PlayerSessions
    private final IMapFactory mapFactory;
    private final CommandProcessor commandProcessor;
    private final GameLogic gameLogic;
    private final EventPublisher eventPublisher;
    private final GameMap map;

    public GameRoomImpl(IMapFactory mapFactory,
                        CommandProcessor commandProcessor,
                        GameLogic gameLogic,
                        EventPublisher eventPublisher) {
        this.id = UUID.randomUUID().toString();
        this.players = new ConcurrentHashMap<>();
        this.mapFactory = mapFactory;
        this.commandProcessor = commandProcessor;
        this.gameLogic = gameLogic;
        this.eventPublisher = eventPublisher;
        this.map = mapFactory.create("default");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void addPlayer(String playerId) {
        players.putIfAbsent(playerId, new Object());
        gameLogic.registerPlayer(playerId);
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
        gameLogic.unregisterPlayer(playerId);
    }

    @Override
    public int getPlayerCount() {
        return players.size();
    }

    @Override
    public void start() {
        // Beispiel: GameLoop initialisieren
        gameLogic.initialize(map, players.keySet());
        eventPublisher.publish(new RoomStartedEvent(id));
    }

    public Map<String, Object> getPlayers() {
        return Collections.unmodifiableMap(players);
    }


//    private final GameLogic logic;
//    private final CommandProcessor processor;
//    private final EventPublisher publisher;
//    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
//    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
//
//    @Override
//    public void addPlayer(WebSocketSession s) {
//        sessions.put(s.getId(), s);
//        logic.addPlayer(s.getId());  // ensure logic tracks this player
//    }
//
//    @Override
//    public void removePlayer(WebSocketSession s) {
//        sessions.remove(s.getId());
//        logic.removePlayer(s.getId());
//    }
//
//    @Override
//    public void handleInput(ICommand cmd) {
//        processor.process(cmd, this);
//    }
//
//    @Override
//    public void start() {
//        exec.scheduleAtFixedRate(() -> {
//            logic.update(0.05f);
//            StateUpdateMessage su = logic.buildStateUpdate();
//            System.out.println("⏱  tick: sending STATE_UPDATE to " + sessions.size() + " sessions → " + su);
//            sessions.values().forEach(ws -> {
//                try {
//                    ws.sendMessage(new TextMessage(publisher.toJson(su)));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }, 0, 50, TimeUnit.MILLISECONDS);
//    }
//
//
//    @Override
//    public GameLogic getLogic() {
//        return logic;
//    }
}
