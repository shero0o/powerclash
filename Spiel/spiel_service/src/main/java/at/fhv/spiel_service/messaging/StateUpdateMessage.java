package at.fhv.spiel_service.messaging;

import at.fhv.spiel_service.domain.Gadget;
import at.fhv.spiel_service.domain.NPC;
import at.fhv.spiel_service.domain.Projectile;
import at.fhv.spiel_service.domain.Zone;
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
    private Zone zone;
    private List<NPC> npcs = new ArrayList<>();
    private List<Gadget> gadgets = new ArrayList<>();

}
