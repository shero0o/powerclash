package at.fhv.spiel_backend.model;

import lombok.*;
import java.util.*;

public class ARAttack extends AbstractAttack {
    public ARAttack() {
        super("ar", 25, 1f, 3, 600f, 0.3f, 500f);
    }

    @Override
    public List<Projectile> fire(Brawler owner, float targetX, float targetY) {
        if (!canFire()) return List.of();
        recordFire();
        return List.of(createProjectile(owner, targetX, targetY));
    }
}
