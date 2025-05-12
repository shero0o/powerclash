// DefaultGameLogic.java
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
import at.fhv.spiel_backend.server.map.GameMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultGameLogic implements GameLogic {
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, Bullet> bullets = new ConcurrentHashMap<>();
    private GameMap gameMap;

    @Override
    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    @Override
    public void addPlayer(String playerId, String brawlerId) {
        int index = players.size(); // 0 = erster Spieler, 1 = zweiter Spieler
        Position spawn;

        // Unterschiedliche Startpositionen
        if (index == 0) {
            spawn = new Position(1200, 1200, 0); // Spieler 1 (links oben)
        } else {
            spawn = new Position(6520, 1200, 0); // Spieler 2 (rechts unten)
        }

        Brawler br;
        switch (brawlerId) {
            case "tank":
                br = new Brawler(playerId, 1, 200, spawn); break;
            case "mage":
                br = new Brawler(playerId, 1, 80, spawn); break;
            case "healer":
                br = new Brawler(playerId, 1, 100, spawn); break;
            default: // sniper
                br = new Brawler(playerId, 1, 100, spawn); break;
        }

        players.put(playerId, new Player(
                br.getId(),
                br.getLevel(),
                br.getMaxHealth(),
                br.getPosition()
        ));
    }



    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
    }

    @Override
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    @Override
    public void movePlayer(String playerId, float x, float y, float angle) {
        Player p = players.get(playerId);
        if (p == null || gameMap == null) return;
        int tileX = (int)(x / gameMap.getTileWidth());
        int tileY = (int)(y / gameMap.getTileHeight());
        if (!gameMap.isWallAt(tileX, tileY)) {
            p.setPosition(new Position(x, y, angle));
        }
    }

    @Override
    public void attack(String playerId, float dirX, float dirY, float angle) {
        Player shooter = players.get(playerId);
        if (shooter == null) return;
        Position pos = shooter.getPosition();
        Bullet b = new Bullet(playerId, pos.getX(), pos.getY(), angle);
        bullets.put(b.getId(), b);
    }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        List<PlayerState> ps = players.values().stream()
                .map(p -> new PlayerState(
                        p.getId(),
                        p.getPosition(),
                        p.getCurrentHealth(),
                        p.isVisible()))
                .collect(Collectors.toList());

        List<Event> ev = bullets.values().stream()
                .map(b -> new Event(
                        ActionType.ATTACK.name(),
                        new BulletState(b.getId(), b.getX(), b.getY(), b.getAngle())))
                .collect(Collectors.toList());

        StateUpdateMessage msg = new StateUpdateMessage();
        msg.setPlayers(ps);
        msg.setEvents(ev);
        return msg;
    }

    public void updateBullets(float dt) {
        Iterator<Bullet> it = bullets.values().iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.update(dt);

            int bx = (int)(b.getX() / gameMap.getTileWidth());
            int by = (int)(b.getY() / gameMap.getTileHeight());

            if (gameMap.isWallAt(bx, by)) {
                it.remove();
                continue;
            }

            for (Player target : players.values()) {
                if (target.getId().equals(b.getShooterId()) || !target.isVisible()) continue;

                float dx = target.getPosition().getX() - b.getX();
                float dy = target.getPosition().getY() - b.getY();
                float collisionRadius = 32f;

                if (Math.hypot(dx, dy) <= collisionRadius) {
                    target.setCurrentHealth(Math.max(0, target.getCurrentHealth() - 10));
                    if (target.getCurrentHealth() <= 0) {
                        target.setVisible(false);
                    }
                    it.remove();
                    break;
                }
            }
        }
    }
}
