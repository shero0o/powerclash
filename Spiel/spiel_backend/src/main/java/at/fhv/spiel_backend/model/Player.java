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
    Gadget gadget;
    int gadgetMaxUses = 3;
    int gadgetUsesThisRound = 0;
    long lastGadgetTime = 0L;

    public Player(String id, Attack attack, int level, int maxHealth, Position pos, Gadget gadget) {
        super(id, attack, level, maxHealth, pos);
        this.gadget = gadget;
    }

    public boolean canUseGadget() {
        long now = System.currentTimeMillis();
        return gadgetUsesThisRound < gadgetMaxUses
                && now - lastGadgetTime >= (long)(gadget.getCooldownSeconds() * 1000);
    }

    public void useGadget() {
        if (!canUseGadget()) return;
        // apply effect externally
        gadgetUsesThisRound++;
        lastGadgetTime = System.currentTimeMillis();
    }
}