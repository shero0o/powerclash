package at.fhv.spiel_backend.ws;

import lombok.Data;

@Data
public class InputMessage {
    private ActionType type;
    private String playerId;
    private Object payload;  // instance of MovePayload, AttackPayload, or null
}
