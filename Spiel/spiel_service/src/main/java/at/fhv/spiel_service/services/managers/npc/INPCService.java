// src/main/java/at/fhv/spiel_service/services/managers/npc/INPCService.java
package at.fhv.spiel_service.services.managers.npc;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.NPC;
import at.fhv.spiel_service.entities.Player;

import java.util.List;
import java.util.Map;

public interface INPCService {
    void addNpc(String npcId, NPC npc);
    void updateNPCs(float deltaSec,
                    GameMap map,
                    Map<String, Player> players,
                    List<NPC> ignored);
    /** liefert alle NPCs zurück */
    List<NPC> getAllNpcs();
}
