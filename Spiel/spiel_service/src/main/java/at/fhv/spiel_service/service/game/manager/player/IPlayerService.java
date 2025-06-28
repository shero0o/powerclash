package at.fhv.spiel_service.service.game.manager.player;

import at.fhv.spiel_service.domain.Gadget;
import at.fhv.spiel_service.domain.GadgetType;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public interface IPlayerService {
    void addPlayer(String playerId, String brawlerId, String playerName);


    void removePlayer(String playerId);


    Player getPlayer(String playerId);


    Position getPlayerPosition(String playerId);


    Gadget getGadget(String playerId);


    Map<String, Player> getPlayers();


    List<Player> getAllPlayers();

    List<Gadget> getAllGadgets();

    Map<String, String> getPlayerBrawler();
    Map<String, String> getPlayerNames();
    Map<String, Gadget> getPlayerGadget();
    Map<String, Integer> getPlayerCoins();
    void setGadget(String playerId, GadgetType type);
}
