package at.fhv.spiel_backend.model;

import java.util.List;

public interface Attack {
    String getId();
    int getDamage();
    float getCooldownSeconds();
    int getMaxAmmo();
    float getRange();
    float getSpread();
    float getSpeed();

    /**
     * Attempts to fire; returns list of projectiles if successful, or empty if on cooldown/ammo.
     */
    List<Projectile> fire(Brawler owner, float targetX, float targetY);

    boolean canFire();
    void recordFire();
}
