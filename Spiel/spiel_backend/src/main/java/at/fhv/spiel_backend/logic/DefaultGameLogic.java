package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.model.*;
import at.fhv.spiel_backend.ws.PlayerState;
import at.fhv.spiel_backend.ws.StateUpdateMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class DefaultGameLogic implements GameLogic {
    private final Map<String, Player> players = new ConcurrentHashMap<>();

    @Override
    public StateUpdateMessage buildStateUpdate() {
        List<PlayerState> ps = players.values().stream()
                .map(p -> new PlayerState(
                        p.getId(),
                        p.getPosition(),
                        p.getCurrentHealth(),
                        p.isVisible()))
                .collect(Collectors.toList());
        StateUpdateMessage msg = new StateUpdateMessage();
        msg.setPlayers(ps);
        msg.setEvents(Collections.emptyList());
        return msg;
    }

    @Override
    public void movePlayer(String playerId, float x, float y) {
        Player p = players.get(playerId);
        if (p != null) {
            p.setPosition(new Position(x, y));
        }
    }

    @Override
    public void addPlayer(String playerId) {
        // instantiate player with default brawler & gadget
        Brawler br = new Brawler(playerId, 1, 100, new Position(0,0));
        players.put(playerId, new Player(playerId, br.getLevel(), br.getMaxHealth(), br.getPosition()));
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
    }
}

