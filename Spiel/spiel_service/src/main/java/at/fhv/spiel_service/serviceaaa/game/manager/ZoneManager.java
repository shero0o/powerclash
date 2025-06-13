package at.fhv.spiel_service.serviceaaa.game.manager;

import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Position;

import java.util.Map;

public interface ZoneManager {

    void initZone(Position center, float startRadius, float shrinkRate);
    void updateZone(float deltaSec, Map<String, Player> players);
    float getZoneRadius();
    boolean isZoneActive();

    Position getCenter();
    long  getRemainingTimeMs();

}
