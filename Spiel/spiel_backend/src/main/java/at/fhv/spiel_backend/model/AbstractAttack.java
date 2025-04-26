package at.fhv.spiel_backend.model;

import java.util.Arrays;

public abstract class AbstractAttack implements Attack {
    private final String id;
    private final int damage;
    private final float cooldownSeconds;
    private final int maxAmmo;
    private final float range;
    private final float spread;
    private final float speed;

    private final long[] ammoReadyAt;
    private long lastAttackTime;

    protected AbstractAttack(String id,
                             int damage,
                             float cooldownSeconds,
                             int maxAmmo,
                             float range,
                             float spread,
                             float speed) {
        this.id = id;
        this.damage = damage;
        this.cooldownSeconds = cooldownSeconds;
        this.maxAmmo = maxAmmo;
        this.range = range;
        this.spread = spread;
        this.speed = speed;
        this.ammoReadyAt = new long[maxAmmo];
        this.lastAttackTime = 0L;
    }

    @Override
    public String getId() { return id; }

    @Override
    public int getDamage() { return damage; }

    @Override
    public float getCooldownSeconds() { return cooldownSeconds; }

    @Override
    public int getMaxAmmo() { return maxAmmo; }

    @Override
    public float getRange() { return range; }

    @Override
    public float getSpread() { return spread; }

    @Override
    public float getSpeed() { return speed; }

    @Override
    public synchronized boolean canFire() {
        long now = System.currentTimeMillis();
        // global cooldown
        if (now - lastAttackTime < (long)(cooldownSeconds * 1000)) {
            return false;
        }
        // per-ammo
        return Arrays.stream(ammoReadyAt).anyMatch(t -> now >= t);
    }

    @Override
    public synchronized void recordFire() {
        long now = System.currentTimeMillis();
        // stamp global
        lastAttackTime = now;
        // consume one ammo
        for (int i = 0; i < maxAmmo; i++) {
            if (now >= ammoReadyAt[i]) {
                ammoReadyAt[i] = now + (long)(cooldownSeconds * 1000);
                break;
            }
        }
    }

    /**
     * Helper to create a single projectile with spread.
     */
    protected Projectile createProjectile(Brawler owner, float targetX, float targetY) {
        float ox = owner.getPosition().getX();
        float oy = owner.getPosition().getY();
        float base = (float) Math.atan2(targetY - oy, targetX - ox);
        float half = spread / 2;
        float angle = base + ((float)Math.random() * spread - half);
        float dx = (float)Math.cos(angle);
        float dy = (float)Math.sin(angle);
        return new Projectile(owner.getId(), new Position(ox, oy), dx, dy, speed, range, damage);
    }
}