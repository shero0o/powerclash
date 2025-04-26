package at.fhv.spiel_backend.model;

import lombok.*;
import java.util.*;

public class ShotgunAttack extends AbstractAttack {
    private static final int PELLETS = 5;

    public ShotgunAttack() {
        super("shotgun", 15, 1.2f, 3, 300f, 1.2f, 400f);
    }

    @Override
    public List<Projectile> fire(Brawler owner, float targetX, float targetY) {
        if (!canFire()) return List.of();
        recordFire();
        List<Projectile> pellets = new ArrayList<>(PELLETS);
        for (int i = 0; i < PELLETS; i++) {
            pellets.add(createProjectile(owner, targetX, targetY));
        }
        return pellets;
    }
}