// src/main/java/at/fhv/spiel_service/services/managers/projectile/ProjectileServiceImpl.java
package at.fhv.spiel_service.services.managers.projectile;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Position;
import at.fhv.spiel_service.entities.Projectile;
import at.fhv.spiel_service.entities.ProjectileType;
import at.fhv.spiel_service.services.managers.crate.ICrateService;
import at.fhv.spiel_service.services.managers.combat.ICombatService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProjectileServiceImpl implements IProjectileService {
    private final Map<String, Projectile> projectiles = new ConcurrentHashMap<>();

    @Override
    public void spawnProjectile(String playerId,
                                Player player,
                                float dirX,
                                float dirY,
                                String projectileType,
                                Map<String, Projectile> projectiles) {
        // Erzeuge eine eindeutige ID
        String id = UUID.randomUUID().toString();
        // Startposition = aktuelle Spielerposition
        Position start = player.getPosition();
        // Richtung als Vektor (normalisiert optional)
        Position direction = new Position(dirX, dirY, 0f);

        // Zeitpunkt der Erstellung
        long now = System.currentTimeMillis();

        // Typ aus Enum
        ProjectileType type = ProjectileType.valueOf(projectileType);

        // Beispiel-Werte pro Projektiltyp (kannst du anpassen oder aus Enum auslesen)
        float speed    = 500f;
        int   damage   = 20;
        float maxRange = 1000f;

        // Neues Projektil erstellen
        Projectile p = new Projectile(
                id,
                playerId,
                new Position(start.getX(), start.getY(), start.getAngle()),
                direction,
                speed,
                damage,
                now,
                type,
                maxRange,
                0f
        );

        // In die Map packen
        projectiles.put(p.getId(), p);
    }

    @Override
    public void updateProjectiles(float deltaSec,
                                  GameMap map,
                                  Map<String, Player> players,
                                  Map<String, Projectile> projectiles,
                                  ICrateService crateService,
                                  ICombatService combatService) {
        long now = System.currentTimeMillis();
        Iterator<Projectile> it = projectiles.values().iterator();

        while (it.hasNext()) {
            Projectile p = it.next();

            // 1) Bewegung: pos += dir * speed * deltaSec
            Position pos = p.getPosition();
            float dx = p.getDirection().getX() * p.getSpeed() * deltaSec;
            float dy = p.getDirection().getY() * p.getSpeed() * deltaSec;
            pos.setX(pos.getX() + dx);
            pos.setY(pos.getY() + dy);

            // 2) Reichweite prüfen (distance tracked via travelled field)
            p.setTravelled(p.getTravelled() + (float)Math.hypot(dx, dy));
            if (p.getTravelled() >= p.getMaxRange()) {
                it.remove();
                continue;
            }

            // 3) Kollisions- & Schaden‐Logik
            combatService.applyCombatOnProjectile(
                    p,
                    map,
                    players,
                    crateService,
                    it,
                    now
            );
        }
    }

    @Override
    public Collection<Projectile> getAllProjectiles() {
        return Collections.unmodifiableCollection(projectiles.values());
    }
}
