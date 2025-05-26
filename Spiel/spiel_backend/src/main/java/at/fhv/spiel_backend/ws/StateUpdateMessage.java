// at.fhv.spiel_backend.ws.StateUpdateMessage.java
package at.fhv.spiel_backend.ws;

import at.fhv.spiel_backend.model.NPC;
import at.fhv.spiel_backend.model.Projectile;
import at.fhv.spiel_backend.model.ZoneState;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StateUpdateMessage {
    private final String type = "STATE_UPDATE";
    private List<PlayerState> players;
    private List<Event> events;
    private List<Projectile> projectiles;
    private List<CrateState> crates;
    private ZoneState zoneState;
    private List<NPC> npcs = new ArrayList<>();

}
