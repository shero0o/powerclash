// at.fhv.spiel_backend.ws.StateUpdateMessage.java
package at.fhv.spiel_backend.ws;

import at.fhv.spiel_backend.model.Projectile;
import lombok.Data;
import java.util.List;

@Data
public class StateUpdateMessage {
    private final String type = "STATE_UPDATE";
    private List<PlayerState> players;
    private List<Event> events;
    private List<Projectile> projectiles;
    private List<CrateState> crates;
}
