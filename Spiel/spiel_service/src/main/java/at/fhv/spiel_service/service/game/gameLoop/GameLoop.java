package at.fhv.spiel_service.service.game.gameLoop;

import at.fhv.spiel_service.domain.*;
import at.fhv.spiel_service.messaging.EventPublisher;
import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.service.game.logic.IGameLogic;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Kapselt die eigentliche Game‐Loop (60Hz) und macht:
 * 1) Input‐Verarbeitung
 * 2) Projektil‐ & Umwelteinflüsse‐Update
 * 3) State‐Broadcast
 */
public class GameLoop {
    private static final float TICK_DT   = 0.016f; // ~60 Hz
    private static final float MAX_SPEED = 300f;

    private final String roomId;
    private final IGameLogic logic;
    private final EventPublisher publisher;
    private final GameMap map;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    // statt Movement-Klasse: key → [dirX, dirY, angle]
    private final ConcurrentMap<String, float[]> movementBuffer = new ConcurrentHashMap<>();

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

    /** Startet die Loop (einmalig) */
    public synchronized void start() {
        if (running) return;
        running = true;

        executor.scheduleAtFixedRate(() -> {
            try {
                // 1) Bewegungs-Buffer abarbeiten
                for (Map.Entry<String, float[]> e : movementBuffer.entrySet()) {
                    String pid     = e.getKey();
                    float[] mv     = e.getValue();     // [0]=dirX, [1]=dirY, [2]=angle
                    Player p       = logic.getPlayer(pid);
                    if (p == null || p.getCurrentHealth() <= 0) {
                        movementBuffer.remove(pid);
                        continue;
                    }

                    float dx = mv[0], dy = mv[1], ang = mv[2];
                    // Normierung
                    float len = (float)Math.hypot(dx, dy);
                    float nx  = len>0 ? dx/len : 0;
                    float ny  = len>0 ? dy/len : 0;

                    // Gadget‐Boost
                    Gadget g = logic.getGadget(pid);
                    float speedFactor = 1f;
                    if (g!=null && g.getType()==GadgetType.SPEED_BOOST && g.getTimeRemaining()>0) {
                        speedFactor = 2f;
                    }
                    // Buff‐Ablauf
                    if (g!=null && g.getTimeRemaining()>0) {
                        if (g.getType()==GadgetType.HEALTH_BOOST)  p.setHpBoostActive(true);
                        if (g.getType()==GadgetType.DAMAGE_BOOST)  p.setDamageBoostActive(true);
                        g.setTimeRemaining(Math.max(0, g.getTimeRemaining() - (long)(TICK_DT*1000)));
                    } else if (g!=null && g.getTimeRemaining()==0) {
                        if (p.isHpBoostActive()) {
                            p.setHpBoostActive(false);
                            p.setMaxHealth(p.getMaxHealth() - Player.HP_BOOST_AMOUNT);
                            if (p.getCurrentHealth()>p.getMaxHealth()) {
                                p.setCurrentHealth(p.getMaxHealth());
                            }
                        }
                        if (p.isDamageBoostActive()) {
                            p.setDamageBoostActive(false);
                        }
                    }

                    // Bewegung prüfen
                    float newX = p.getPosition().getX() + nx * MAX_SPEED * speedFactor * TICK_DT;
                    float newY = p.getPosition().getY() + ny * MAX_SPEED * speedFactor * TICK_DT;
                    int tx = (int)(newX / map.getTileWidth());
                    int ty = (int)(newY / map.getTileHeight());
                    if (!map.isWallAt(tx, ty)) {
                        logic.movePlayer(pid, newX, newY, ang);
                    }
                }

                // 2) Logic‐Updates
                logic.updateProjectiles(TICK_DT);
                logic.applyEnvironmentalEffects();

                // 3) State broadcasten
                StateUpdateMessage msg = logic.buildStateUpdate();
                publisher.publish(roomId, msg);

            } catch (Exception ex) {
                System.err.println("[GameLoop] Fehler im Tick: " + ex.getMessage());
                ex.printStackTrace();
            }
        }, 0, (long)(TICK_DT*1000), TimeUnit.MILLISECONDS);
    }

    /** Prüft, ob die Loop läuft */
    public boolean isRunning() {
        return running;
    }

    /** Puffer für nächsten Tick überschreiben */
    public void submitMovement(String playerId, float dirX, float dirY, float angle) {
        movementBuffer.put(playerId, new float[]{dirX, dirY, angle});
    }

    /** Aufräumen beim Entfernen eines Spielers */
    public void removePlayer(String playerId) {
        movementBuffer.remove(playerId);
    }
}
