package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.model.*;
import at.fhv.spiel_backend.ws.PlayerState;
import at.fhv.spiel_backend.ws.StateUpdateMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultGameLogic implements GameLogic {
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, Projectile> projectiles = new ConcurrentHashMap<>();

    private static final int MAX_AMMO = 3;
    private static final long AMMO_REFILL_MS = 2000;
    private final Map<String, Integer> ammoMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRefill = new ConcurrentHashMap<>();

    @Override
    public StateUpdateMessage buildStateUpdate() {
        List<PlayerState> ps = players.values().stream()
                .map(p -> new PlayerState(
                        p.getId(),
                        p.getPosition(),
                        p.getCurrentHealth(),
                        p.isVisible(),
                        ammoMap.getOrDefault(p.getId(), MAX_AMMO)))
                .collect(Collectors.toList());
        StateUpdateMessage msg = new StateUpdateMessage();
        msg.setPlayers(ps);
        msg.setEvents(Collections.emptyList());
        msg.setProjectiles(getProjectiles());
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

        ammoMap.put(playerId, MAX_AMMO);
        lastRefill.put(playerId, System.currentTimeMillis());
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
        ammoMap.remove(playerId);
        lastRefill.remove(playerId);
    }



    @Override
    public void spawnProjectile(String playerId, Position position, Position direction) {
        String projectileId = UUID.randomUUID().toString();
        Projectile projectile = new Projectile(projectileId, playerId, position, direction, 500f, 10, System.currentTimeMillis());
        projectiles.put(projectileId, projectile);


        long now = System.currentTimeMillis();
        // 1) ggf. nachfüllen
        long last = lastRefill.getOrDefault(playerId, 0L);
        if (now - last >= AMMO_REFILL_MS) {
            ammoMap.put(playerId, MAX_AMMO);
            lastRefill.put(playerId, now);
        }
        // 2) prüfen
        int ammoLeft = ammoMap.getOrDefault(playerId, MAX_AMMO);
        if (ammoLeft <= 0) {
            return; // kein Schuss möglich
        }
        // 3) abziehen und tatsächlich spawnen
        ammoMap.put(playerId, ammoLeft - 1);

    }

    @Override
    public void updateProjectiles() {
        long now = System.currentTimeMillis();

        projectiles.values().removeIf(p -> now - p.getCreationTime() > 1000);

        float deltaTime = 0.016f; // ca. 60 updates/sec
        projectiles.values().forEach(projectile ->
        {   projectile.getPosition().setX(projectile.getPosition().getX() + projectile.getDirection().getX() * projectile.getSpeed() * deltaTime);
            projectile.getPosition().setY(projectile.getPosition().getY() + projectile.getDirection().getY() * projectile.getSpeed() * deltaTime);
        });


        for (String playerId : ammoMap.keySet()) {
            int ammoLeft = ammoMap.get(playerId);
            long last    = lastRefill.getOrDefault(playerId, 0L);
            if (ammoLeft < MAX_AMMO && now - last >= AMMO_REFILL_MS) {
                ammoMap.put(playerId, MAX_AMMO);
                lastRefill.put(playerId, now);
            }
        }

    }

    @Override
    public List<Projectile> getProjectiles() {
        return new ArrayList<>(projectiles.values());
    }

    @Override
    public Position getPlayerPosition(String playerId) {
        return players.get(playerId).getPosition();
    }



}

