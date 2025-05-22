package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.model.Position;
import at.fhv.spiel_backend.model.Projectile;
import at.fhv.spiel_backend.model.ProjectileType;
import at.fhv.spiel_backend.model.Player;
import at.fhv.spiel_backend.server.map.GameMap;
import at.fhv.spiel_backend.ws.StateUpdateMessage;

import java.util.List;

/**
 * Kombinierte GameLogic-Schnittstelle, die
 * - Player- & Brawler-Management,
 * - Bewegung mit und ohne Winkel,
 * - Map-Initialisierung,
 * - Projectile- und Waffen-Logik
 * - State-Updates
 * zusammenführt.
 */
public interface GameLogic {

    /**
     * Build a snapshot of the current game state for clients
     */
    StateUpdateMessage buildStateUpdate();


    /**
     * Move the specified player to (x, y) with rotation angle
     */
    void movePlayer(String playerId, float x, float y, float angle);

    /**
     * Register a new player in the simulation (default Brawler)
     */


    /**
     * Register a new player with specific Brawler
     */
    void addPlayer(String playerId, String brawlerId, String playerName);

    /**
     * Remove a player from the simulation
     */
    void removePlayer(String playerId);

    /**
     * Set the GameMap for collision checks
     */
    void setGameMap(GameMap gameMap);

    /**
     * Expose the internal Player object
     */
    Player getPlayer(String playerId);

    /**
     * Simple attack (legacy) for Bullet-based systems
     */
    void attack(String playerId, float dirX, float dirY, float angle);

    /**
     * Spawn a projectile with full weapon logic
     */
    void spawnProjectile(String playerId, Position position, Position direction, ProjectileType type);

    /**
     * Update existing projectiles (movement, lifetime, collisions)
     */
    void updateProjectiles();

    /**
     * Get raw player position (used for camera follow)
     */
    Position getPlayerPosition(String playerId);

    /**
     * Retrieve all active projectiles for state updates
     */
    List<Projectile> getProjectiles();

    /**
     * Change the current weapon & reset ammo
     */
    void setPlayerWeapon(String playerId, ProjectileType projectileType);
}
