package at.fhv.spiel_service.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Gadget {
    private String id;
    private GadgetType type;
    private int remainingUses;
    private long timeRemaining;

    public Gadget(GadgetType type, String playerId) {
     id= playerId + "-" + UUID.randomUUID();
     this.type = type;
     timeRemaining = 0L;
     remainingUses = 3;
    }
}
