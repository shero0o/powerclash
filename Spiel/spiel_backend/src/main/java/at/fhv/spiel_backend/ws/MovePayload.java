package at.fhv.spiel_backend.ws;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MovePayload {
    private float x;
    private float y;
    private float angle;
}

