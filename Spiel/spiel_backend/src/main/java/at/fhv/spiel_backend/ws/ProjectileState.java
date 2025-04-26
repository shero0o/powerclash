package at.fhv.spiel_backend.ws;

import at.fhv.spiel_backend.model.Position;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectileState {
    private String id;
    private Position position;
    private String ownerId;
}
