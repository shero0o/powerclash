package at.fhv.spiel_service.service.game.manager.zone;

import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;

import java.util.Map;

public class ZoneManagerImpl implements ZoneManager {

    private boolean zoneActive = false;
    private float zoneRadius;
    private float zoneShrinkRate;
    private Position zoneCenter;

    private long zoneStartTimeMs;
    private long zoneDurationMs;

    @Override
    public void initZone(Position center, float startRadius, float shrinkRate) {

        this.zoneCenter = center;
        this.zoneRadius = startRadius;
        this.zoneShrinkRate = shrinkRate;
        this.zoneStartTimeMs = System.currentTimeMillis();
        this.zoneDurationMs  = (long)(startRadius / shrinkRate * 1000);
        this.zoneActive = true;
    }

    @Override
    public void updateZone(float deltaSec, Map<String, Player> players) {
        if (!zoneActive) return;
        zoneRadius = Math.max(0f, zoneRadius - zoneShrinkRate * deltaSec);
        for (Player p : players.values()) {
            if (p.getCurrentHealth() <= 0) continue;
            float dx = p.getPosition().getX() - zoneCenter.getX();
            float dy = p.getPosition().getY() - zoneCenter.getY();
            if (Math.hypot(dx, dy) > zoneRadius) {
                int dmg = (int) Math.ceil(0.05f * deltaSec);
                p.setCurrentHealth(Math.max(0, p.getCurrentHealth() - dmg));
            }
        }
    }

    @Override public float getZoneRadius()   { return zoneRadius; }
    @Override public boolean isZoneActive()  { return zoneActive; }

    @Override
    public Position getCenter() {
        return zoneCenter;
    }

    @Override
    public long getRemainingTimeMs() {
        if (!zoneActive) return 0L;
        long elapsed = System.currentTimeMillis() - zoneStartTimeMs;
        long left    = zoneDurationMs - elapsed;
        return left > 0 ? left : 0L;
    }
}
