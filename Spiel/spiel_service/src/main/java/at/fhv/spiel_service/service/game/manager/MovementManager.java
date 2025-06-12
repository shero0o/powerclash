package at.fhv.spiel_service.service.game.manager;

public interface MovementManager {

    /**
     * Bewegt den Spieler, falls kein Hindernis im Zieltile ist,
     * und setzt seine Position (inkl. Blickwinkel).
     */
    void movePlayer(String playerId, float x, float y, float angle);

    /**
     * Wendet alle Umwelteinflüsse an (Giftfelder, Heal-Tiles, Busch-Sichtbarkeit).
     */
    void applyEnvironmentalEffects();
}
