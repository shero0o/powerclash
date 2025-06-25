package at.fhv.spiel_service.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Player extends Brawler {
    private int gadgetMaxUses;
    private int gadgetUsesThisRound;
    private long lastGadgetTime;
    private boolean hpBoostActive ;
    private long    hpBoostEndTime;
    private boolean speedBoostActive ;
    private long    speedBoostEndTime;
    private boolean damageBoostActive;
    private long    damageBoostEndTime;

    private long lastPoisonTime = 0;
    private long lastHealTime = 0;
    boolean visible = true;
    int coinCount = 0;

    public Player(String id, int level, int maxHealth, Position pos) {
        super(id, level, maxHealth, pos);
        hpBoostActive = false;
        hpBoostEndTime = 0L;
        damageBoostActive = false;
        damageBoostEndTime = 0L;
        gadgetMaxUses = 3;
        gadgetUsesThisRound = 0;
        lastGadgetTime = 0L;
    }
}
