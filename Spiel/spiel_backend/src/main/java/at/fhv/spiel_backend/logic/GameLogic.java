package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.ws.StateUpdateMessage;

public interface GameLogic {
    /**
     * Advance simulation by deltaSeconds
     */
    void update(float deltaSeconds);

    /**
     * Build a snapshot of the current game state for clients
     */
    StateUpdateMessage buildStateUpdate();

    /**
     * Move the specified player to (x,y)
     */
    void movePlayer(String playerId, float x, float y);

    /**
     * Have the specified player perform an attack towards (tx,ty)
     */
    void playerAttack(String playerId, float tx, float ty);

    /**
     * Have the specified player use their gadget
     */
    void useGadget(String playerId);

    /**
     * Register a new player in the simulation
     */
    void addPlayer(String playerId);

    /**
     * Remove a player from the simulation
     */
    void removePlayer(String playerId);
}


