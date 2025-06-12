// src/main/java/at/fhv/spiel_service/services/managers/effects/IEffectsService.java
package at.fhv.spiel_service.services.managers.effects;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.Player;

import java.util.Map;

/**
 * Wendet alle Umwelteinflüsse aufs Spielfeld an (Heal‐/Poison‐Tiles, …).
 */
public interface IEffectsService {
    /**
     * @param deltaSec Sekunden seit letztem Update
     * @param gameMap  die aktuelle Spielkarte
     * @param players  Map von SpielerId → Player
     */
    void applyEnvironmentalEffects(float deltaSec,
                                   GameMap gameMap,
                                   Map<String, Player> players);
}
