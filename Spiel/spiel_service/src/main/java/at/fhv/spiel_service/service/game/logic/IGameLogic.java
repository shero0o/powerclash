package at.fhv.spiel_service.service.game.logic;

import at.fhv.spiel_service.domain.GameMap;
import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.domain.*;


public interface IGameLogic {


    StateUpdateMessage buildStateUpdate();



    void movePlayer(String playerId, float x, float y, float angle);





    void addPlayer(String playerId, String brawlerId, String playerName);


    void removePlayer(String playerId);


    void setGameMap(GameMap gameMap);


    Player getPlayer(String playerId);

    Gadget getGadget(String playerId);


    void spawnProjectile(String playerId, Position position, Position direction, ProjectileType type);


    void updateProjectiles(float delta);


    Position getPlayerPosition(String playerId);


    void setPlayerWeapon(String playerId, ProjectileType projectileType);

    void setPlayerGadget(String playerId, GadgetType chosenGadget);

    void applyEnvironmentalEffects();


}
