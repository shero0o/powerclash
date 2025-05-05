package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.model.Position;
import at.fhv.spiel_backend.model.Projectile;
import at.fhv.spiel_backend.model.ProjectileType;
import at.fhv.spiel_backend.ws.StateUpdateMessage;

import java.util.List;

public interface GameLogic {

    /**
     * Build a snapshot of the current game state for clients
     */
    StateUpdateMessage buildStateUpdate();

    /**
     * Move the specified player to (x,y)
     */
    void movePlayer(String playerId, float x, float y);

    /**
     * Register a new player in the simulation
     */
    void addPlayer(String playerId);

    /**
     * Remove a player from the simulation
     */
    void removePlayer(String playerId);

    void spawnProjectile(String playerId, Position position, Position direction, ProjectileType type);

    void updateProjectiles();

    Position getPlayerPosition(String playerId);
    List<Projectile> getProjectiles();

    void setPlayerWeapon(String playerId, ProjectileType projectileType);
}


