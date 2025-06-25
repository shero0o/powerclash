package at.fhv.spiel_service.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

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
    private long lastAttackTime;


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
