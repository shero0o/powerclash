package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.model.*;
import at.fhv.spiel_backend.ws.PlayerState;
import at.fhv.spiel_backend.ws.ProjectileState;
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
    private final List<Projectile> projectiles = Collections.synchronizedList(new ArrayList<>());
    private final SafeZone safeZone = new SafeZone(new Position(0,0), 1000f, 1f, 100f);
    private final GameMap gameMap = new GameMap();

    @Override
    public void update(float dt) {
        // shrink safe zone
        safeZone.update(dt);
        // move projectiles
        synchronized (projectiles) {
            projectiles.removeIf(p -> {
                p.update(dt);
                return p.getRemainingRange() <= 0;
            });
        }
        // process collisions and damage
        // TODO: raycast or proximity checks
    }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        List<PlayerState> ps = players.values().stream()
                .map(p -> new PlayerState(p.getId(), p.getPosition(), p.getCurrentHealth(), p.isVisible()))
                .collect(Collectors.toList());
        List<ProjectileState> pcs;
        synchronized (projectiles) {
            pcs = projectiles.stream()
                    .map(p -> new ProjectileState(p.getId(), p.getPosition(), p.getOwnerId()))
                    .collect(Collectors.toList());
        }
        StateUpdateMessage msg = new StateUpdateMessage();
        msg.setPlayers(ps);
        msg.setProjectiles(pcs);
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
    public void playerAttack(String playerId, float tx, float ty) {
        Player p = players.get(playerId);
        if (p != null && p.canAttack()) {
            projectiles.addAll(p.getAttack().fire(p, tx, ty));
            p.recordAttack();
        }
    }

    @Override
    public void useGadget(String playerId) {
        Player p = players.get(playerId);
        if (p != null && p.canUseGadget()) {
            // apply effect
            if (p.getGadget().getEffectType() == Gadget.EffectType.HEAL) {
                p.setCurrentHealth(Math.min(p.getMaxHealth(), p.getCurrentHealth() + p.getGadget().getEffectValue()));
            }
            // TODO: other effect types
            p.useGadget();
        }
    }

    @Override
    public void addPlayer(String playerId) {
        // instantiate player with default brawler & gadget
        Brawler br = new Brawler(playerId, new ARAttack(), 1, 100, new Position(0,0));
        players.put(playerId, new Player(playerId, br.getAttack(), br.getLevel(), br.getMaxHealth(), br.getPosition(), new Gadget("default-heal", Gadget.EffectType.HEAL, 30, 10f)));
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
    }
}

