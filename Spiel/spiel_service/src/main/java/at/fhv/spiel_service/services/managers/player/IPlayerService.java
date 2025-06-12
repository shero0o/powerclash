// src/main/java/at/fhv/spiel_service/services/managers/player/IPlayerService.java
package at.fhv.spiel_service.services.managers.player;

import at.fhv.spiel_service.entities.Gadget;
import at.fhv.spiel_service.entities.Player;

import java.util.Map;

public interface IPlayerService {
    Player addPlayer(String playerId, String brawlerId, String playerName);
    void   removePlayer(String playerId);
    Player getPlayer(String playerId);
    Gadget getGadget(String playerId);
    Map<String, Player> getAllPlayers();
}
