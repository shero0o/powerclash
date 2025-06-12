package at.fhv.spiel_service.service.game.manager;

import at.fhv.spiel_service.domain.NPC;
import at.fhv.spiel_service.domain.Player;

import java.util.List;
import java.util.Map;

public interface NPCManager {
    /**
     * Updated alle registrierten NPCs: Bewegung, Facing und Angriffe.
     *
     * @param deltaSec Zeit seit letztem Frame in Sekunden
     * @param players  Map aller lebenden Spieler (playerId → Player)
     * @param npcs     Liste aller NPCs
     */
    void updateNPCs(float deltaSec, Map<String, Player> players, List<NPC> npcs);
}
