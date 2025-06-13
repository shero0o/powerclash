package at.fhv.spiel_service.service.game.core;


import at.fhv.spiel_service.domain.NPC;
import at.fhv.spiel_service.domain.GameMap;
import at.fhv.spiel_service.domain.Projectile;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Crate;
import at.fhv.spiel_service.domain.*;
import at.fhv.spiel_service.domain.ProjectileType;


import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.messaging.CrateState;
import at.fhv.spiel_service.messaging.PlayerState;
import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.service.game.manager.environmentalEffects.EnvironmentalEffectsManagerImpl;
import at.fhv.spiel_service.service.game.manager.movement.MovementManagerImpl;
import at.fhv.spiel_service.service.game.manager.NPC.NPCManagerImpl;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileManager;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileManagerImpl;
import at.fhv.spiel_service.service.game.manager.zone.ZoneManagerImpl;
import at.fhv.spiel_service.service.game.manager.environmentalEffects.EnvironmentalEffectsManager;
import at.fhv.spiel_service.service.game.manager.movement.MovementManager;
import at.fhv.spiel_service.service.game.manager.NPC.NPCManager;
import at.fhv.spiel_service.service.game.manager.zone.ZoneManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static at.fhv.spiel_service.domain.ProjectileType.*;

public class DefaultGameLogic implements GameLogic {
    // --- Spielzustand ---
    private final Map<String, Player> players = new ConcurrentHashMap<>();

    private final List<NPC> npcs = new ArrayList<>();
    private GameMap gameMap;
    private long lastFrameTimeMs = System.currentTimeMillis();

    // --- Waffen & Ammo ---

    private final Map<String, String> playerBrawler = new ConcurrentHashMap<>();
    private final Map<String, String> playerNames = new ConcurrentHashMap<>();

    // --- Gadget ---
    private final Map<String, Gadget> playerGadget = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    private final Map<String, Crate> crates = new ConcurrentHashMap<>();
    private final Map<String, Integer> playerCoins = new ConcurrentHashMap<>();

    private MovementManager movementManager;
    private EnvironmentalEffectsManager envEffectsManager;
    private ZoneManager zoneManager;
    private NPCManager npcManager;
    private ProjectileManager projectileManager;


    @Override
    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;

        for (Position pos : gameMap.getCratePositions()) {
            int tileX = (int) pos.getX();
            int tileY = (int) pos.getY();
            String key = tileX + "," + tileY;
            crates.put(key, new Crate(UUID.randomUUID().toString(), new Position(tileX, tileY)));
        }

        this.movementManager = new MovementManagerImpl(
                gameMap, players, crates
        );
        this.envEffectsManager = new EnvironmentalEffectsManagerImpl(
                gameMap, players
        );
        this.zoneManager = new ZoneManagerImpl();
        this.npcManager  = new NPCManagerImpl(gameMap, npcs);
        this.projectileManager = new ProjectileManagerImpl(gameMap, players);

    }

    @Override
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }


    @Override
    public Gadget getGadget(String playerId){
        return playerGadget.get(playerId);
    }

        /**
         * Register a new NPC for melee AI
         */
    public void addNpc (String npcId, Position spawn, int health, float attackRadius, int damage, float speed,
        long attackCooldownMs){
            NPC npc = new NPC(
                    npcId,
                    spawn,
                    health,
                    attackRadius,
                    damage,
                    speed,
                    attackCooldownMs
            );
            npcs.add(npc);
        }

        /**
         * Activate shrinking zone
         */
        public void activateZone (Position center,float initialRadius, long durationMs){
            float shrinkRatePerSec = initialRadius / (durationMs / 1000f);
            zoneManager.initZone(center, initialRadius, shrinkRatePerSec);
        }

        @Override
        public void addPlayer (String playerId, String brawlerId, String playerName){
            // wenn kein Brawler übergeben wurde, Default nehmen
            if (brawlerId == null || brawlerId.isBlank()) {
                brawlerId = "sniper";
            }

            // spawn-Logik wie gehabt …
            int index = players.size();
            Position spawn;
            switch (index) {
                case 0:
                    // Spieler 1: oben links
                    spawn = new Position(1200, 1200, 0);
                    break;
                case 1:
                    // Spieler 2: oben rechts
                    spawn = new Position(6520, 1200, 0);
                    break;
                case 2:
                    // Spieler 3: unten links
                    spawn = new Position(1200, 6480, 0);
                    break;
                case 3:
                    // Spieler 4: unten rechts
                    spawn = new Position(6520, 6480, 0);
                    break;
                default:
                    // Falls mehr als 4 Spieler hinzukommen, z.B. zentriert spawnen:
                    spawn = new Position(3860, 2700, 0);
                    break;
            }
            Brawler br =  new Brawler(playerId, 1, 100, spawn);

            players.put(playerId,
                    new Player(br.getId(), br.getLevel(), br.getMaxHealth(), br.getPosition())
            );

            // Ammo initialisieren …
            projectileManager.initPlayer(playerId, RIFLE_BULLET);
            playerBrawler.put(playerId, brawlerId);
            playerNames.put(playerId, playerName); // aus DTO übergeben
            playerCoins.put(playerId, 0);


        }

        @Override
        public void removePlayer (String playerId){
            players.remove(playerId);
            projectileManager.removePlayer(playerId);
            playerBrawler.remove(playerId);
            playerNames.remove(playerId);

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
        playerGadget.put(playerId, new Gadget(type, playerId));
    }


    @Override
    public void applyEnvironmentalEffects () {
        // 1) Zeit berechnen
        long now     = System.currentTimeMillis();
        float delta  = (now - lastFrameTimeMs) / 1000f;
        lastFrameTimeMs = now;
        // 2) Umwelteinflüsse aufs Spielfeld
        envEffectsManager.applyEnvironmentalEffects(delta);
        // 3) Zone-Shrink & Schaden außerhalb
        zoneManager.updateZone(delta, players);
        // 4) NPC-KI (Bewegung & melee)
        npcManager.updateNPCs(delta, players, npcs);
    }

    @Override
    public List<Crate> getCrates () {
        return new ArrayList<>(crates.values());
    }

    @Override
    public void spawnProjectile(String playerId,
                                Position position,
                                Position direction,
                                ProjectileType type) {
        projectileManager.spawnProjectile(playerId, position, direction, type);

        }



        @Override
        public void updateProjectiles (float delta) {
            projectileManager.updateProjectiles(delta);
        }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        // Spieler-Status mit aktuellem Ammo & Weapon
        List<PlayerState> ps = players.values().stream().map(p -> {
            String pid = p.getId();
            ProjectileType wep = projectileManager.getCurrentWeapon(pid);
            int ammo = projectileManager.getCurrentAmmo(pid);
            return new PlayerState(
                    pid,
                    p.getPosition(),
                    p.getCurrentHealth(),
                    p.isVisible(),
                    ammo,
                    wep,
                    playerBrawler.getOrDefault(p.getId(), "sniper"),
                    playerNames.getOrDefault(p.getId(), "Player"),
                    playerCoins.getOrDefault(pid, 0),
                    p.getMaxHealth()

            );
        }).collect(Collectors.toList());

            StateUpdateMessage msg = new StateUpdateMessage();
            msg.setPlayers(ps);
            msg.setEvents(Collections.emptyList());
            msg.setProjectiles(projectileManager.getProjectiles());


        List<CrateState> crateStates = crates.values().stream()
                    .map(c -> new CrateState(
                            c.getId(),
                            (int) c.getPosition().getX(),
                            (int) c.getPosition().getY(),
                            c.getCurrentHealth()
                    ))
                    .collect(Collectors.toList());
            msg.setCrates(crateStates);
        List<Gadget> gadgets = getAllGadgets();  // oder wie immer Dein Logic das hält
        msg.setGadgets(gadgets);


        if (zoneManager.isZoneActive()) {
            // Wir müssen eine ZoneState erzeugen – dafür brauchen wir
            // den Restzeit-Wert. Den liefert z.B. ein neues Helper-Api:
            long remainingMs = zoneManager.getRemainingTimeMs();
            msg.setZoneState(new ZoneState(
                    zoneManager.getCenter(),
                    zoneManager.getZoneRadius(),
                    remainingMs
            ));
        }
            msg.setNpcs(new ArrayList<>(npcs));
            return msg;
        }

    public List<Gadget> getAllGadgets() {
        return new ArrayList<>(this.playerGadget.values());
    }


    @Override
    public List<Projectile> getProjectiles () {
            return projectileManager.getProjectiles();
        }

        @Override
        public Position getPlayerPosition (String playerId){
            return players.get(playerId).getPosition();
        }

    }


