package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.model.Brawler;
import at.fhv.spiel_backend.model.Player;
import at.fhv.spiel_backend.model.Position;
import at.fhv.spiel_backend.model.Bullet;
import at.fhv.spiel_backend.ws.PlayerState;
import at.fhv.spiel_backend.ws.BulletState;
import at.fhv.spiel_backend.ws.StateUpdateMessage;
import at.fhv.spiel_backend.ws.Event;
import at.fhv.spiel_backend.ws.ActionType;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultGameLogic implements GameLogic {
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, Bullet> bullets = new ConcurrentHashMap<>();

    @Override
    public StateUpdateMessage buildStateUpdate() {
        // 1) snapshot all player states
        List<PlayerState> playerStates = players.values().stream()
                .map(p -> new PlayerState(
                        p.getId(),
                        p.getPosition(),
                        p.getCurrentHealth(),
                        p.isVisible()))
                .collect(Collectors.toList());

        // 2) snapshot all bullets as events
        List<Event> bulletEvents = bullets.values().stream()
                .map(b -> new Event(
                        ActionType.ATTACK.name(),
                        new BulletState(b.getId(), b.getX(), b.getY(), b.getAngle())
                ))
                .collect(Collectors.toList());

        // 3) assemble message
        StateUpdateMessage msg = new StateUpdateMessage();
        msg.setPlayers(playerStates);
        msg.setEvents(bulletEvents);
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
        Brawler br = new Brawler(playerId, 1, 100, new Position(500, 500, 0));
        players.put(playerId, new Player(
                playerId, br.getLevel(), br.getMaxHealth(), br.getPosition()));
    }

    @Override
    public void attack(String playerId, float dirX, float dirY, float angle) {
        // 1) look up the shooter
        Player shooter = players.get(playerId);
        if (shooter == null) return;

        // 2) spawn a bullet carrying the shooter’s ID so we don’t hit ourselves
        Position pos = shooter.getPosition();
        Bullet bullet = new Bullet(playerId, pos.getX(), pos.getY(), angle);
        bullets.put(bullet.getId(), bullet);
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
    }

    @Override
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    /**
     * Advance all bullets by dt seconds.  Call this once per tick.
     */
    // still inside DefaultGameLogic.java
    public void updateBullets(float dt) {
        // iterate over a copy to allow removal
        for (Iterator<Bullet> it = bullets.values().iterator(); it.hasNext(); ) {
            Bullet b = it.next();
            b.update(dt);

            // check against EVERY player except the shooter
            for (Player target : players.values()) {
                if (target.getId().equals(b.getShooterId())) continue;
                float dx = target.getPosition().getX() - b.getX();
                float dy = target.getPosition().getY() - b.getY();
                // simple radius check (tweak as needed)
                if (Math.hypot(dx, dy) < 16) {
                    // hit! deduct 10 HP from the target
                    int newHp = Math.max(0, target.getCurrentHealth() - 10);
                    target.setCurrentHealth(newHp);
                    if (newHp == 0) target.setVisible(false);

                    // remove this bullet
                    it.remove();
                    break;
                }
            }
        }
    }

}
