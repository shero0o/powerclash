package at.fhv.spiel_service.service.game.logic;
import at.fhv.spiel_service.domain.NPC;
import at.fhv.spiel_service.domain.GameMap;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Crate;
import at.fhv.spiel_service.domain.*;
import at.fhv.spiel_service.domain.ProjectileType;

import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.service.game.manager.collision.CollisionManager;
import at.fhv.spiel_service.service.game.manager.collision.CollisionManagerImpl;
import at.fhv.spiel_service.service.game.manager.environmentalEffects.EnvironmentalEffectsManagerImpl;
import at.fhv.spiel_service.service.game.manager.movement.MovementManagerImpl;
import at.fhv.spiel_service.service.game.manager.NPC.NPCManagerImpl;
import at.fhv.spiel_service.service.game.manager.player.IPlayerService;
import at.fhv.spiel_service.service.game.manager.player.PlayerServiceImpl;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileManager;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileManagerImpl;
import at.fhv.spiel_service.service.game.manager.state.StateUpdateManager;
import at.fhv.spiel_service.service.game.manager.state.StateUpdateManagerImpl;
import at.fhv.spiel_service.service.game.manager.zone.ZoneManagerImpl;
import at.fhv.spiel_service.service.game.manager.environmentalEffects.EnvironmentalEffectsManager;
import at.fhv.spiel_service.service.game.manager.movement.MovementManager;
import at.fhv.spiel_service.service.game.manager.NPC.NPCManager;
import at.fhv.spiel_service.service.game.manager.zone.ZoneManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultIGameLogic implements IGameLogic {

    private final List<NPC> npcs = new ArrayList<>();
    private GameMap gameMap;
    private long lastFrameTimeMs = System.currentTimeMillis();
    private final Map<String, Crate> crates = new ConcurrentHashMap<>();

    private MovementManager movementManager;
    private EnvironmentalEffectsManager envEffectsManager;
    private ZoneManager zoneManager;
    private NPCManager npcManager;
    private ProjectileManager projectileManager;
    private IPlayerService playerService;
    private CollisionManager collisionManager;
    private StateUpdateManager stateUpdateManager;

    @Override
    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;

        for (Position pos : gameMap.getCratePositions()) {
            int tileX = (int) pos.getX();
            int tileY = (int) pos.getY();
            String key = tileX + "," + tileY;
            crates.put(key, new Crate(UUID.randomUUID().toString(), new Position(tileX, tileY)));
        }
        Map<String, Player>      playersMap      = new ConcurrentHashMap<>();
        Map<String, String>      brawlerMap      = new ConcurrentHashMap<>();
        Map<String, String>      namesMap        = new ConcurrentHashMap<>();
        Map<String, Gadget>      gadgetMap       = new ConcurrentHashMap<>();
        Map<String, Integer>     coinsMap        = new ConcurrentHashMap<>();

        this.projectileManager = new ProjectileManagerImpl(gameMap, playersMap);
        this.playerService = new PlayerServiceImpl(playersMap, brawlerMap, namesMap, gadgetMap, coinsMap, projectileManager);
        this.collisionManager = new CollisionManagerImpl(projectileManager, gameMap);
        this.movementManager = new MovementManagerImpl(gameMap, playersMap, crates);
        this.envEffectsManager = new EnvironmentalEffectsManagerImpl(gameMap, playersMap);
        this.zoneManager = new ZoneManagerImpl();
        this.npcManager  = new NPCManagerImpl(gameMap, npcs);
        this.stateUpdateManager = new StateUpdateManagerImpl(playerService, projectileManager, crates, zoneManager, npcs);
    }

    @Override
    public Player getPlayer(String playerId) {
        return playerService.getPlayer(playerId);
    }

    @Override
    public Gadget getGadget(String playerId){
        return playerService.getGadget(playerId);
    }

    public void addNpc (String npcId, Position spawn, int health, float attackRadius, int damage, float speed,
        long attackCooldownMs){
            NPC npc = new NPC(npcId, spawn, health, attackRadius, damage, speed, attackCooldownMs);
            npcs.add(npc);
        }

    public void activateZone (Position center,float initialRadius, long durationMs){
        float shrinkRatePerSec = initialRadius / (durationMs / 1000f);
        zoneManager.initZone(center, initialRadius, shrinkRatePerSec);
    }

    @Override
    public void addPlayer (String playerId, String brawlerId, String playerName){
        playerService.addPlayer(playerId, brawlerId, playerName);
    }

    @Override
    public void removePlayer (String playerId){
        playerService.removePlayer(playerId);

    }

    @Override
    public void movePlayer (String playerId,float x, float y, float angle){
        movementManager.movePlayer(playerId, x, y, angle);
    }


    @Override
    public void setPlayerWeapon (String playerId, ProjectileType type){
        projectileManager.setWeapon(playerId, type);
        }

    @Override
    public void setPlayerGadget(String playerId, GadgetType type) {
        playerService.setGadget(playerId, type);
    }

    @Override
    public void applyEnvironmentalEffects () {
        long now     = System.currentTimeMillis();
        float delta  = (now - lastFrameTimeMs) / 1000f;
        lastFrameTimeMs = now;
        envEffectsManager.applyEnvironmentalEffects(delta);
        zoneManager.updateZone(delta, playerService.getPlayers());
        npcManager.updateNPCs(delta, playerService.getPlayers());
        collisionManager.processCollisions(
                projectileManager.getProjectiles(),
                playerService.getPlayers(),
                npcs,
                crates,
                playerService.getPlayerCoins()
        );
    }

    @Override
    public void spawnProjectile(String playerId, Position position, Position direction, ProjectileType type) {
        projectileManager.spawnProjectile(playerId, position, direction, type);
        }

        @Override
        public void updateProjectiles (float delta) {
            projectileManager.updateProjectiles(delta);
            collisionManager.processCollisions(projectileManager.getProjectiles(), playerService.getPlayers(), npcs, crates, playerService.getPlayerCoins());
        }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        return stateUpdateManager.buildStateUpdate();
    }

    @Override
    public Position getPlayerPosition (String playerId){
        return playerService.getPlayerPosition(playerId);
    }

}


