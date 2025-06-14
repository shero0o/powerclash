package at.fhv.spiel_service.service.game.manager.collision;

import at.fhv.spiel_service.domain.Crate;
import at.fhv.spiel_service.domain.NPC;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Projectile;

import java.util.List;
import java.util.Map;

public interface CollisionManager {

    void processCollisions(
            List<Projectile> projectiles,
            Map<String, Player> players,
            List<NPC> npcs,
            Map<String, Crate> crates,
            Map<String, Integer> playerCoins
    );
}
