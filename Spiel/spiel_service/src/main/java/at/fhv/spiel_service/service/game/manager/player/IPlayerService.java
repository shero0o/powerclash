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

    /** Entfernt einen Spieler komplett (inkl. Projectile-Cleanup). */
    void removePlayer(String playerId);

    /** Liefert den Player oder null. */
    Player getPlayer(String playerId);

    /** Liefert Position des Spielers. */
    Position getPlayerPosition(String playerId);

    /** Liefert das Gadget des Spielers oder null. */
    Gadget getGadget(String playerId);

    /** Liefert Map aller Spieler (id → Player). */
    Map<String, Player> getPlayers();

    /** Für State-Updates: Liste aller Player. */
    List<Player> getAllPlayers();

    List<Gadget> getAllGadgets();

    Map<String, String> getPlayerBrawler();
    Map<String, String> getPlayerNames();
    Map<String, Gadget> getPlayerGadget();
    Map<String, Integer> getPlayerCoins();
    void setGadget(String playerId, GadgetType type);
}
