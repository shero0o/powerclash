package at.fhv.spiel_backend.ws;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttackPayload {
    private float targetX;
    private float targetY;
}
