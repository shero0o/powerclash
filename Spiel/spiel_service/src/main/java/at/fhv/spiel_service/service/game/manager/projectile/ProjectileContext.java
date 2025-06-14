package at.fhv.spiel_service.service.game.manager.projectile;

import at.fhv.spiel_service.domain.*;

import java.util.Map;

public interface ProjectileContext {

    GameMap getGameMap();
    Map<String, Player> getPlayers();
    void addProjectile(Projectile p);
    void removeProjectileById(String id);
    long now();               // aktuelle Zeit in ms
    float deltaSec();
}
