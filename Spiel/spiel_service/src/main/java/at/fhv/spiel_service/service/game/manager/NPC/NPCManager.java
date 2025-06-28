package at.fhv.spiel_service.service.game.manager.NPC;

import at.fhv.spiel_service.domain.NPC;
import at.fhv.spiel_service.domain.Player;

import java.util.List;
import java.util.Map;

public interface NPCManager {

    void updateNPCs(float deltaSec, Map<String, Player> players);
}
