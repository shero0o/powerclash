package at.fhv.spiel_backend.ws;

import lombok.Data;

import java.util.List;

@Data
public class StateUpdateMessage {
    private final String type = "STATE_UPDATE";
    private List<PlayerState> players;
    private List<ProjectileState> projectiles;
    private List<Event> events;
}
