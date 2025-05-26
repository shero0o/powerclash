package at.fhv.spiel_backend.model;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Player extends Brawler {
    int gadgetMaxUses = 3;
    int gadgetUsesThisRound = 0;
    long lastGadgetTime = 0L;
    private long lastPoisonTime = 0;
    private long lastHealTime = 0;
    boolean visible = true;
    int coinCount = 0;

    public Player(String id, int level, int maxHealth, Position pos) {
        super(id, level, maxHealth, pos);
    }
}
