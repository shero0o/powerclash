package at.fhv.spiel_service.messaging;

import at.fhv.spiel_service.entities.Gadget;
import at.fhv.spiel_service.entities.NPC;
import at.fhv.spiel_service.entities.Projectile;
import at.fhv.spiel_service.entities.ZoneState;
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
    private List<Gadget>      gadgets = new ArrayList<>();


}
