// src/main/java/at/fhv/spiel_service/services/core/GameLogicServiceImpl.java
package at.fhv.spiel_service.services.core;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Gadget;
import at.fhv.spiel_service.entities.Projectile;
import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.services.managers.combat.ICombatService;
import at.fhv.spiel_service.services.managers.crate.ICrateService;
import at.fhv.spiel_service.services.managers.effects.IEffectsService;
import at.fhv.spiel_service.services.managers.equipment.IEquipmentService;
import at.fhv.spiel_service.services.managers.map.IMapService;
import at.fhv.spiel_service.services.managers.movement.IMovementService;
import at.fhv.spiel_service.services.managers.npc.INPCService;
import at.fhv.spiel_service.services.managers.player.IPlayerService;
import at.fhv.spiel_service.services.managers.projectile.IProjectileService;
import at.fhv.spiel_service.services.managers.state.IStateService;
import at.fhv.spiel_service.services.managers.zone.IZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameLogicServiceImpl implements IGameLogicService {

    private GameMap map;
    private final Map<String, Projectile> projectiles = new ConcurrentHashMap<>();

    @Autowired private IMapService        mapService;
    @Autowired private IPlayerService     playerService;
    @Autowired private IMovementService   movementService;
    @Autowired private INPCService        npcService;
    @Autowired private IEffectsService    effectsService;
    @Autowired private IZoneService       zoneService;
    @Autowired private IEquipmentService  equipmentService;
    @Autowired private ICrateService      crateService;
    @Autowired private IProjectileService projectileService;
    @Autowired private ICombatService     combatService;
    @Autowired private IStateService      stateService;

    @Override
    public void initGame(GameMap map) {
        this.map = map;
        mapService.setGameMap(map);
    }

    @Override
    public Player addPlayer(String playerId, String brawlerId, String playerName) {
        return playerService.addPlayer(playerId, brawlerId, playerName);
    }

    @Override
    public void removePlayer(String playerId) {
        playerService.removePlayer(playerId);
    }

    @Override
    public Player getPlayer(String playerId) {
        return playerService.getPlayer(playerId);
    }

    @Override
    public void movePlayer(String playerId, float dirX, float dirY, float angle) {
        movementService.movePlayer(playerId, dirX, dirY, angle);
    }

    @Override
    public void setPlayerWeapon(String playerId, String weaponType) {
        equipmentService.assignWeapon(playerId, weaponType);
    }

    @Override
    public void setPlayerGadget(String playerId, String gadgetType) {
        equipmentService.assignGadget(playerId, gadgetType);
    }

    @Override
    public void spawnProjectile(String playerId,
                                float dirX,
                                float dirY,
                                String projectileType) {
        Player p = playerService.getPlayer(playerId);
        projectileService.spawnProjectile(
                playerId, p, dirX, dirY, projectileType, projectiles
        );
    }

    @Override
    public void update(float deltaSec) {
        Map<String, Player> players = playerService.getAllPlayers();
        movementService.updateMovement(deltaSec, map, players);
        npcService.updateNPCs(deltaSec, map, players, npcService.getAllNpcs());
        effectsService.applyEnvironmentalEffects(deltaSec, map, players);
        zoneService.updateZone(deltaSec, players);
        projectileService.updateProjectiles(
                deltaSec, map, players, projectiles, crateService, combatService
        );
    }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        return stateService.buildStateUpdate();
    }

    @Override
    public Gadget getGadget(String playerId) {
        return equipmentService.getGadget(playerId);
    }

    @Override
    public void useGadget(String playerId) {
        Player p = playerService.getPlayer(playerId);
        Gadget g = equipmentService.getGadget(playerId);
        if (p == null || g == null) {
            return;
        }
        long now = System.currentTimeMillis();
        switch (g.getType()) {
            case SPEED_BOOST -> {
                p.setSpeedBoostActive(true);
                p.setSpeedBoostEndTime(now + 10_000);
            }
            case HEALTH_BOOST -> {
                p.setMaxHealth(p.getMaxHealth() + Player.HP_BOOST_AMOUNT);
                p.setCurrentHealth(
                        Math.min(p.getMaxHealth(),
                                p.getCurrentHealth() + Player.HP_BOOST_AMOUNT)
                );
            }
            case DAMAGE_BOOST -> {
                p.setDamageBoostActive(true);
                p.setDamageBoostEndTime(now + 10_000);
            }
        }
        g.setTimeRemaining(10_000L);
        g.setRemainingUses(g.getRemainingUses() - 1);
    }
}
