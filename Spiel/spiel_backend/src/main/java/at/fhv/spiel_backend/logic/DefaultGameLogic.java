package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.model.Brawler;
import at.fhv.spiel_backend.model.Player;
import at.fhv.spiel_backend.model.Position;
import at.fhv.spiel_backend.ws.PlayerState;
import at.fhv.spiel_backend.ws.StateUpdateMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        msg.setEvents(List.of());
        return msg;
    }

    @Override
    public void movePlayer(String playerId, float x, float y, float angle) {
        Player p = players.get(playerId);
        if (p != null) {
            p.setPosition(new Position(x, y, angle));
        }
    }

    @Override
    public void addPlayer(String playerId) {
        Brawler br = new Brawler(playerId, 1, 100, new Position(500,500,0));
        players.put(playerId, new Player(
                playerId, br.getLevel(), br.getMaxHealth(), br.getPosition()));
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
    }

    @Override
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }
}
