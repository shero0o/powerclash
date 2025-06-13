// src/main/java/at/fhv/spiel_service/services/managers/combat/ICombatService.java
package at.fhv.spiel_service.services.managers.combat;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.NPC;
import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Projectile;
import at.fhv.spiel_service.services.managers.crate.ICrateService;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Kapselt alle Kollisions- und Schadens-Logiken:
 *  - Projectile vs. World/Player/Crate
 *  - NPC vs. Player
 */

public interface ICombatService {
    /**
     * Prüft für ein einzelnes Projectile:
     * 1) Wand-Kollision (Tile-Basis)
     * 2) Spieler-Treffer (Abstand < 40px)
     * 3) Crate-Treffer (Tile-Key)
     * Entfernt das Projectile im Iterator, wenn es getroffen hat.
     *
     * @param proj         das Projektil
     * @param map          die Spielkarte (Tile-Checks)
     * @param players      alle Spieler nach ID
     * @param crateService Crate-Service für Treffer-Handling
     * @param projectiles  Iterator über alle aktiven Projectiles
     */
    void applyCombatOnProjectile(Projectile proj,
                                 GameMap map,
                                 Map<String, Player> players,
                                 ICrateService crateService,
                                 Iterator<Projectile> projectiles,
                                 long now);

    /**
     * Prüft alle NPC-Angriffe auf Spieler:
     * - Cooldown
     * - Reichweite (npc.getAttackRadius())
     * - Apply Damage
     *
     * @param deltaSec Zeit seit letztem Update (sec)
     * @param map      die Spielkarte (für Distanz)
     * @param players  alle Spieler nach ID
     * @param npcs     Liste aller NPCs
     */
    void applyCombatNPCs(float deltaSec,
                         GameMap map,
                         Map<String, Player> players,
                         List<NPC> npcs);
}
