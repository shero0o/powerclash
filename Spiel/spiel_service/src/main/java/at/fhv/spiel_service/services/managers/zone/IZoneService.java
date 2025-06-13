// src/main/java/at/fhv/spiel_service/services/managers/zone/IZoneService.java
package at.fhv.spiel_service.services.managers.zone;

import at.fhv.spiel_service.entities.Position;
import at.fhv.spiel_service.entities.Player;

import java.util.Map;

public interface IZoneService {
    void activateZone(Position center, float radius, long durationMs);
    void updateZone(float deltaSec, Map<String, Player> players);
    boolean isZoneActive();
    float   getZoneRadius();
    long    getRemainingTimeMs();
    /** liefert den aktuellen Mittelpunkt der Zone */
    Position getCenter();
}
