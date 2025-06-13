// src/main/java/at/fhv/spiel_service/services/managers/zone/ZoneServiceImpl.java
package at.fhv.spiel_service.services.managers.zone;

import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Position;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Strategy-Pattern für Safe-Zone:
 * - aktiviert einmal die Zone
 * - schrumpft sie stetig
 * - fügt Spielern außerhalb Strafschaden zu
 */
@Service
public class ZoneServiceImpl implements IZoneService {

    private boolean zoneActive = false;
    private Position center = new Position(0, 0, 0);
    private float radius = 5000f;          // Start-Radius
    private float shrinkRate;              // Einheiten pro Sekunde
    private long startTimeMs;
    private long durationMs = 600_000L;     // z.B. 10 Min

    @Override
    public void activateZone(Position center, float radius, long durationMs) {
        this.center     = center;
        this.radius     = radius;
        this.durationMs = durationMs;
        this.shrinkRate = radius / (durationMs / 1000f);
        this.startTimeMs = System.currentTimeMillis();
        this.zoneActive  = true;
    }

    @Override
    public void updateZone(float deltaSec, Map<String, Player> players) {
        if (!zoneActive) return;

        // Schrumpfen
        radius = Math.max(0, radius - shrinkRate * deltaSec);

        // Schaden außerhalb der Zone
        for (Player p : players.values()) {
            Position pos = p.getPosition();
            float dx = pos.getX() - center.getX();
            float dy = pos.getY() - center.getY();
            float dist = (float)Math.hypot(dx, dy);
            if (dist > radius) {
                int dmg = (int)(getDamagePerSec() * deltaSec);
                p.setCurrentHealth(Math.max(0, p.getCurrentHealth() - dmg));
            }
        }
    }

    @Override
    public boolean isZoneActive() {
        return zoneActive;
    }

    @Override
    public float getZoneRadius() {
        return radius;
    }

    @Override
    public long getRemainingTimeMs() {
        long elapsed = System.currentTimeMillis() - startTimeMs;
        long left = durationMs - elapsed;
        return left > 0 ? left : 0;
    }

    @Override
    public Position getCenter() {
        return center;
    }

    private int getDamagePerSec() {
        return 5;
    }
}
