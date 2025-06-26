package at.fhv.spiel_service.service.game.manager.movement;

public interface MovementManager {

    /**
     * Bewegt den Spieler, falls kein Hindernis im Zieltile ist,
     * und setzt seine Position (inkl. Blickwinkel).
     */
    void movePlayer(String playerId, float x, float y, float angle);

}
