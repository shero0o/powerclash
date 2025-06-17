package at.fhv.spiel_service.domain;


import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)

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

    public static final int HP_BOOST_AMOUNT        = 50;
    public static final int DAMAGE_MULTIPLIER    = 2;
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
