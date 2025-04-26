package at.fhv.spiel_backend.server;

import at.fhv.spiel_backend.command.ICommand;
import at.fhv.spiel_backend.handler.CommandProcessor;
import at.fhv.spiel_backend.logic.GameLogic;
import at.fhv.spiel_backend.ws.StateUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class GameRoomImpl implements GameRoom {
    private final GameLogic logic;
    private final CommandProcessor processor;
    private final EventPublisher publisher;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void addPlayer(WebSocketSession s) {
        sessions.put(s.getId(), s);
        logic.addPlayer(s.getId());  // ensure logic tracks this player
    }

    @Override
    public void removePlayer(WebSocketSession s) {
        sessions.remove(s.getId());
        logic.removePlayer(s.getId());
    }

    @Override
    public void handleInput(ICommand cmd) {
        processor.process(cmd, this);
    }

    @Override
    public void start() {
        exec.scheduleAtFixedRate(() -> {
            logic.update(0.05f);
            StateUpdateMessage su = logic.buildStateUpdate();
            System.out.println("⏱  tick: sending STATE_UPDATE to " + sessions.size() + " sessions → " + su);
            sessions.values().forEach(ws -> {
                try {
                    ws.sendMessage(new TextMessage(publisher.toJson(su)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }, 0, 50, TimeUnit.MILLISECONDS);
    }


    @Override
    public GameLogic getLogic() {
        return logic;
    }
}
