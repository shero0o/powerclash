package at.fhv.spiel_service.service.game.gameLoop;

import at.fhv.spiel_service.domain.*;
import at.fhv.spiel_service.messaging.EventPublisher;
import at.fhv.spiel_service.service.game.logic.IGameLogic;
import lombok.Getter;
import java.util.concurrent.*;
import static at.fhv.spiel_service.config.GameConstants.*;
import static at.fhv.spiel_service.domain.GadgetType.SPEED_BOOST;

public class GameLoop {
    private final String roomId;
    private final IGameLogic logic;
    private final EventPublisher publisher;
    private final GameMap map;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentMap<String, float[]> movementBuffer = new ConcurrentHashMap<>();
    @Getter
    private boolean running = false;

    public GameLoop(String roomId,
                    IGameLogic logic,
                    EventPublisher publisher,
                    GameMap map) {
        this.roomId    = roomId;
        this.logic     = logic;
        this.publisher = publisher;
        this.map       = map;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        executor.scheduleAtFixedRate(this::tick, 0, (long)(TICK_DT*1000), TimeUnit.MILLISECONDS);
    }

    private void tick() {
        try {
            processMovement();
            logic.updateProjectiles(TICK_DT);
            logic.applyEnvironmentalEffects();
            publisher.publish(roomId, logic.buildStateUpdate());
        } catch (Exception ex) {
            System.err.println("[GameLoop] Fehler im Tick: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void processMovement() {
        for (String pid : movementBuffer.keySet()) {
            float[] mv = movementBuffer.get(pid);
            Player p   = logic.getPlayer(pid);
            if (!isAlive(p)) {
                movementBuffer.remove(pid);
                continue;
            }
            float[] norm = normalize(mv[0], mv[1]);
            handleBuffs(p, mv);
            attemptMove(p, norm[0], norm[1], mv[2]);
        }
    }

    private boolean isAlive(Player p) {
        return p != null && p.getCurrentHealth() > 0;
    }

    private float[] normalize(float dx, float dy) {
        float len = (float)Math.hypot(dx, dy);
        return len > 0 ? new float[]{dx/len, dy/len} : new float[]{0,0};
    }

    private void handleBuffs(Player p, float[] mv) {
        Gadget g = logic.getGadget(p.getId());
        if (g == null) return;
        if (g.getTimeRemaining() > 0) {
            applyActiveBuff(p, g);
            g.setTimeRemaining(Math.max(0, g.getTimeRemaining() - (long)(TICK_DT*1000)));
        } else {
            removeExpiredBuffs(p);
        }
    }

    private void applyActiveBuff(Player p, Gadget g) {
        switch (g.getType()) {
            case SPEED_BOOST  -> p.setSpeedBoostActive(true);
            case HEALTH_BOOST -> p.setHpBoostActive(true);
            case DAMAGE_BOOST -> p.setDamageBoostActive(true);
        }
    }

    private void removeExpiredBuffs(Player p) {
        if (p.isHpBoostActive()) {
            p.setHpBoostActive(false);
            p.setMaxHealth(p.getMaxHealth() - HP_BOOST_AMOUNT);
            p.setCurrentHealth(Math.min(p.getCurrentHealth(), p.getMaxHealth()));
        }
        if (p.isDamageBoostActive()) {
            p.setDamageBoostActive(false);
        }
    }

    private void attemptMove(Player p, float nx, float ny, float ang) {
        float factor = getSpeedFactor(p);
        float newX = p.getPosition().getX() + nx * MAX_SPEED * factor * TICK_DT;
        float newY = p.getPosition().getY() + ny * MAX_SPEED * factor * TICK_DT;
        int tx = (int)(newX / map.getTileWidth());
        int ty = (int)(newY / map.getTileHeight());
        if (!map.isWallAt(tx, ty)) {
            logic.movePlayer(p.getId(), newX, newY, ang);
        }
    }

    private float getSpeedFactor(Player p) {
        Gadget g = logic.getGadget(p.getId());
        return (g != null && g.getType() == SPEED_BOOST && g.getTimeRemaining() > 0)
                ? 2f : 1f;
    }

    public void submitMovement(String playerId, float dirX, float dirY, float angle) {
        movementBuffer.put(playerId, new float[]{dirX, dirY, angle});
    }

    public void removePlayer(String playerId) {
        movementBuffer.remove(playerId);
    }
}
