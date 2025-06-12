package at.fhv.spiel_service.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A simple zombie‐style NPC with position, health, melee radius, damage, speed, and cooldown.
 */
@Data
@NoArgsConstructor
public class NPC implements Entity {
    private String id;
    private Position position;
    private int    currentHealth;
    private float  attackRadius;
    private int    damage;
    private float  speed;
    private long   attackCooldownMs;

    // tracks last attack time
    private long lastAttackTime = 0;

    /**
     * Seven-arg ctor for addNpc(...)
     */
    public NPC(String id,
               Position position,
               int currentHealth,
               float attackRadius,
               int damage,
               float speed,
               long attackCooldownMs) {
        this.id                = id;
        this.position          = position;
        this.currentHealth     = currentHealth;
        this.attackRadius      = attackRadius;
        this.damage            = damage;
        this.speed             = speed;
        this.attackCooldownMs  = attackCooldownMs;
        this.lastAttackTime    = 0L;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setPosition(Position position) {
        this.position = position;
    }
}
