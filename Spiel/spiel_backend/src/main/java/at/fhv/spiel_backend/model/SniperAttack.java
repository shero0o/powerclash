package at.fhv.spiel_backend.model;


import lombok.*;
import java.util.*;

public class SniperAttack extends AbstractAttack {
    public SniperAttack() {
        super("sniper", 50, 2f, 3, 800f, 0.05f, 600f);
    }

    @Override
    public List<Projectile> fire(Brawler owner, float targetX, float targetY) {
        if (!canFire()) return List.of();
        recordFire();
        return List.of(createProjectile(owner, targetX, targetY));
    }
}


