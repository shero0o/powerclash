// src/main/java/at/fhv/spiel_service/entities/Player.java
package at.fhv.spiel_service.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Ein Spieler in der Partie, erweitert Brawler um
 * Bewegungseingaben, Geschwindigkeit und Gadget-/Boost-State.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Player extends Brawler {
    public static final int HP_BOOST_AMOUNT     = 50;
    public static final int DAMAGE_MULTIPLIER   = 2;

    String brawlerId;
    String playerName;

    // Bewegung
    float inputX = 0f, inputY = 0f, inputAngle = 0f;
    float speed = 200f;

    // Gadget/Boost State
    int gadgetMaxUses, gadgetUsesThisRound;
    long lastGadgetTime;
    boolean hpBoostActive;     long hpBoostEndTime;
    boolean speedBoostActive;  long speedBoostEndTime;
    boolean damageBoostActive; long damageBoostEndTime;
    long lastPoisonTime, lastHealTime;
    int coinCount;

    public Player(String id,
                  String brawlerId,
                  String playerName,
                  int level,
                  int maxHealth,
                  Position pos) {
        super(id, level, maxHealth, pos);
        this.brawlerId   = brawlerId;
        this.playerName  = playerName;
        this.gadgetMaxUses       = 3;
        this.gadgetUsesThisRound = 0;
        this.lastGadgetTime      = 0L;
        this.hpBoostActive       = false;
        this.hpBoostEndTime      = 0L;
        this.speedBoostActive    = false;
        this.speedBoostEndTime   = 0L;
        this.damageBoostActive   = false;
        this.damageBoostEndTime  = 0L;
        this.lastPoisonTime      = 0L;
        this.lastHealTime        = 0L;
        this.coinCount           = 0;
    }
}
