package at.fhv.spiel_service.service.game.manager.zone;

import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;

import java.util.Map;

public interface ZoneManager {

    void initZone(Position center, float startRadius, float shrinkRate);
    void updateZone(float deltaSec, Map<String, Player> players);
    float getZoneRadius();
    boolean isZoneActive();

    Position getCenter();
    long  getRemainingTimeMs();

}
