package at.fhv.spiel_service.services.core;

import at.fhv.spiel_service.entities.Gadget;
import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.messaging.StateUpdateMessage;

public interface IGameLogicService {
    void initGame(GameMap map);
    Player addPlayer(String playerId, String brawlerId, String playerName);
    void removePlayer(String playerId);
    Player getPlayer(String playerId);
    void movePlayer(String playerId, float dirX, float dirY, float angle);
    void setPlayerWeapon(String playerId, String weaponType);
    void setPlayerGadget(String playerId, String gadgetType);
    void spawnProjectile(String playerId,
                         float dirX,
                         float dirY,
                         String projectileType);     // use String here
    void update(float deltaSec);
    StateUpdateMessage buildStateUpdate();
    Gadget getGadget(String playerId);
    void useGadget(String playerId);                // add this
}
