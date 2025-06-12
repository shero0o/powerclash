// src/main/java/at/fhv/spiel_service/services/managers/map/IMapService.java
package at.fhv.spiel_service.services.managers.map;

import at.fhv.spiel_service.entities.GameMap;

public interface IMapService {
    /**
     * Setzt die Karte im GameLogic und initialisiert alle Kisten.
     */
    void setGameMap(GameMap map);
}
