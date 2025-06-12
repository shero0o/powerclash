package at.fhv.spiel_service.service.game.impl;


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
import at.fhv.spiel_service.service.game.core.GameLogic;
import at.fhv.spiel_service.service.game.impl.manager.EnvironmentalEffectsManagerImpl;
import at.fhv.spiel_service.service.game.impl.manager.MovementManagerImpl;
import at.fhv.spiel_service.service.game.impl.manager.NPCManagerImpl;
import at.fhv.spiel_service.service.game.impl.manager.ZoneManagerImpl;
import at.fhv.spiel_service.service.game.manager.EnvironmentalEffectsManager;
import at.fhv.spiel_service.service.game.manager.MovementManager;
import at.fhv.spiel_service.service.game.manager.NPCManager;
import at.fhv.spiel_service.service.game.manager.ZoneManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static at.fhv.spiel_service.domain.ProjectileType.*;

public class DefaultGameLogic implements GameLogic {
    // --- Spielzustand ---
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, Projectile> projectiles = new ConcurrentHashMap<>();
    private final List<NPC> npcs = new ArrayList<>();
    private GameMap gameMap;
    private long lastFrameTimeMs = System.currentTimeMillis();

    // --- Waffen & Ammo ---
    private static final int DEFAULT_MAX_AMMO = 3;
    private static final int RIFLE_MAX_AMMO = 15;
    private static final long AMMO_REFILL_MS = 2000;
    private final Map<String, Integer> ammoMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRefill = new ConcurrentHashMap<>();
    private final Map<String, Integer> rifleAmmoMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRifleRefill = new ConcurrentHashMap<>();
    private final Set<String> rifleReloading = ConcurrentHashMap.newKeySet();
    private final Map<String, ProjectileType> playerWeapon = new ConcurrentHashMap<>();
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

    }

    @Override
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    @Override
    public Gadget getGadget(String playerId){
        return playerGadget.get(playerId);
    }

    @Override
    public void attack(String playerId, float dirX, float dirY, float angle) {
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
            playerWeapon.put(playerId, RIFLE_BULLET);
            ammoMap.put(playerId, getMaxAmmoForType(RIFLE_BULLET));
            lastRefill.put(playerId, System.currentTimeMillis());
            rifleAmmoMap.put(playerId, RIFLE_MAX_AMMO);
            lastRifleRefill.put(playerId, System.currentTimeMillis());
            playerBrawler.put(playerId, brawlerId);
            playerNames.put(playerId, playerName); // aus DTO übergeben
            playerCoins.put(playerId, 0);


        }

        @Override
        public void removePlayer (String playerId){
            players.remove(playerId);
            ammoMap.remove(playerId);
            lastRefill.remove(playerId);
            rifleAmmoMap.remove(playerId);
            lastRifleRefill.remove(playerId);
            rifleReloading.remove(playerId);
            playerWeapon.remove(playerId);
            playerBrawler.remove(playerId);
            playerNames.remove(playerId);

            // Auch alle zugehörigen Projektile löschen
            projectiles.values().removeIf(p -> p.getPlayerId().equals(playerId));
        }

        @Override
        public void movePlayer (String playerId,float x, float y, float angle){
            movementManager.movePlayer(playerId, x, y, angle);
        }


        @Override
        public void setPlayerWeapon (String playerId, ProjectileType type){
            playerWeapon.put(playerId, type);
            int max = getMaxAmmoForType(type);
            ammoMap.put(playerId, max);
            lastRefill.put(playerId, System.currentTimeMillis());
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
        // Nur erlaubte Waffe
        if (playerWeapon.getOrDefault(playerId, RIFLE_BULLET) != type) {
            return;
        }
        long now = System.currentTimeMillis();

            // --- Ammo-Verbrauch ---
            if (type == RIFLE_BULLET) {
                int left = rifleAmmoMap.getOrDefault(playerId, RIFLE_MAX_AMMO);
                if (left <= 0) return;
                rifleAmmoMap.put(playerId, --left);
                if (left == 0) {
                    rifleReloading.add(playerId);
                    lastRifleRefill.put(playerId, now);
                }
            } else {
                int maxAmmo = getMaxAmmoForType(type);
                int left = ammoMap.getOrDefault(playerId, maxAmmo);
                if (left <= 0) return;
                ammoMap.put(playerId, left - 1);
            }

            // --- Projektile erzeugen ---
            double baseAngle = Math.atan2(direction.getY(), direction.getX());
            switch (type) {
                case SHOTGUN_PELLET -> {
                    final int PELLET_COUNT = 5;
                    final float SPREAD_DEG = 10f;
                    double step = SPREAD_DEG / (PELLET_COUNT - 1);
                    for (int i = 0; i < PELLET_COUNT; i++) {
                        double offset = -SPREAD_DEG / 2 + i * step;
                        double theta = baseAngle + Math.toRadians(offset);
                        Position dirI = new Position((float) Math.cos(theta), (float) Math.sin(theta));
                        spawnSingle(
                                projectId(playerId),
                                playerId,
                                position,
                                dirI,
                                800f, 5, now,
                                ProjectileType.SHOTGUN_PELLET, 700f
                        );
                    }
                }
                case SNIPER -> spawnSingle(
                        projectId(playerId),
                        playerId,
                        position,
                        direction,
                        1400f, 30, now,
                        SNIPER, 2500f
                );
                case RIFLE_BULLET -> spawnSingle(
                        projectId(playerId),
                        playerId,
                        position,
                        direction,
                        1000f, 2, now,
                        RIFLE_BULLET, 2000f
                );
                case MINE -> {
                    Projectile p = spawnSingle(
                            projectId(playerId),
                            playerId,
                            position,
                            direction,
                            750f, 40, now,
                            MINE, 700f
                    );
                    p.setArmTime(0L);
                    p.setArmed(false);
                }
                default -> { /* no-op */ }
            }
        }

        private Projectile spawnSingle (String id,
                String playerId,
                Position startPos,
                Position startDir,
        float speed,
        int damage,
        long creationTime,
        ProjectileType type,
        float maxRange){
            Position pos = new Position(startPos.getX(), startPos.getY(), startPos.getAngle());
            Position dir = new Position(startDir.getX(), startDir.getY(), 0);
            Projectile p = new Projectile(
                    id, playerId, pos, dir, speed, damage, creationTime, type, maxRange, 0f
            );
            projectiles.put(id, p);
            return p;
        }

        private String projectId (String playerId){
            return playerId + "-" + UUID.randomUUID();
        }

        @Override
        public void updateProjectiles (float delta) {
            long now = System.currentTimeMillis();


            java.util.function.Function<String, Float> getSpeedFactor = shooterId -> {
            Player shooter = getPlayer(shooterId);
            if (shooter != null
                        && shooter.isSpeedBoostActive()
                        && now <= shooter.getSpeedBoostEndTime()) {
                    return 2f;
                }
            return 1f;
            };Iterator<Projectile> it = projectiles.values().iterator();
            while (it.hasNext()) {
                Projectile p = it.next();

                // --- Bewegung & Travelled ---
                if (p.getProjectileType() != MINE) {
                    // 1) Normierung
                float dxDir = p.getDirection().getX();
                            float dyDir = p.getDirection().getY();
                float len   = (float)Math.hypot(dxDir, dyDir);
                float nx    = len > 0 ? dxDir / len : 0;
                float ny    = len > 0 ? dyDir / len : 0;

                // 2) Geschwindigkeit inklusive Boost‐Faktor
                float speedFactor = getSpeedFactor.apply(p.getPlayerId());
                float v = p.getSpeed() * delta * speedFactor;
                float dx = nx * v;
                float dy = ny * v;

                // 3) Position & Travelled updaten
                    p.getPosition().setX(p.getPosition().getX() + dx);
                            p.getPosition().setY(p.getPosition().getY() + dy);
                    p.setTravelled(p.getTravelled()
                            + (float) Math.hypot(
                            dx, dy
                    ));
                } else {
                    // Mine rollt bis zur MaxRange, dann armt sie
                    if (p.getTravelled() < p.getMaxRange()) {
                        float dx = p.getDirection().getX() * p.getSpeed() * delta;
                        float dy = p.getDirection().getY() * p.getSpeed() * delta;
                        float newX = p.getPosition().getX() + dx;
                        float newY = p.getPosition().getY() + dy;

                        int tileX = (int) (newX / gameMap.getTileWidth());
                        int tileY = (int) (newY / gameMap.getTileHeight());

                        // Wenn neue Position NICHT auf Wand ist, normal bewegen
                        if (!gameMap.isWallAt(tileX, tileY)) {
                            p.getPosition().setX(newX);
                            p.getPosition().setY(newY);
                        } else {
                            // Wenn Wand, trotzdem weiterreisen (ohne Bewegung)
                            // Optional: Du kannst hier die Position trotzdem aktualisieren,
                            // um sie "durchfliegen" zu lassen – einfach:
                            p.getPosition().setX(newX);
                            p.getPosition().setY(newY);
                        }

                        p.setTravelled(p.getTravelled() + (float) Math.hypot(dx, dy));
                    } else {
                        if (p.getArmTime() == 0L) {
                            int tx = (int) (p.getPosition().getX() / gameMap.getTileWidth());
                            int ty = (int) (p.getPosition().getY() / gameMap.getTileHeight());

                            // Wenn Mine über Wand ist → nicht scharf machen → leicht weiterbewegen
                            if (gameMap.isWallAt(tx, ty)) {
                                float dx = p.getDirection().getX() * p.getSpeed() * delta;
                                float dy = p.getDirection().getY() * p.getSpeed() * delta;
                                p.getPosition().setX(p.getPosition().getX() + dx);
                                p.getPosition().setY(p.getPosition().getY() + dy);
                            } else {
                                p.setArmTime(now + 2000);
                            }
                        } else if (!p.isArmed() && now >= p.getArmTime()) {
                            p.setArmed(true);
                        }
                    }


                }

                // --- Wand-Kollision ---
                int tx = (int) (p.getPosition().getX() / gameMap.getTileWidth());
                int ty = (int) (p.getPosition().getY() / gameMap.getTileHeight());
                if (p.getProjectileType() != MINE) {
                    if (gameMap.isWallAt(tx, ty)) {
                        it.remove();
                        continue;
                    }
                }

                // --- Treffer mit NPCs prüfen ---
                for (NPC npc : npcs) {
                    if (npc.getCurrentHealth() <= 0) continue;

                    float dx = npc.getPosition().getX() - p.getPosition().getX();
                    float dy = npc.getPosition().getY() - p.getPosition().getY();
                    float dist = (float) Math.hypot(dx, dy);

                    if (dist < 40f) { // Kollision bei Nähe
                        npc.setCurrentHealth(Math.max(0, npc.getCurrentHealth() - p.getDamage()));
                        it.remove(); // Projektil entfernen
                        break;
                    }
                }


                int tileX = (int)(p.getPosition().getX() / gameMap.getTileWidth());
                int tileY = (int)(p.getPosition().getY() / gameMap.getTileHeight());
                String key = tileX + "," + tileY;

                Crate crate = crates.get(key);
                if (crate != null) {
                    crate.setWasHit(true);
                    crate.setCurrentHealth(Math.max(0, crate.getCurrentHealth() - p.getDamage()));
                    it.remove();

                    System.out.println("💥 Kiste getroffen bei Tile: " + key);

                    if (crate.getCurrentHealth() <= 0) {
                        crates.remove(key);
                        System.out.println("🧨 Kiste zerstört");

                        // 💰 Coins vergeben
                        String shooterId = p.getPlayerId();
                        int old = playerCoins.getOrDefault(shooterId, 0);
                        playerCoins.put(shooterId, old + 10);
                        System.out.println("💰 " + shooterId + " bekommt 10 Coins → gesamt: " + (old + 10));
                    }


                    continue;
                }

                // --- Spieler-Kollision ---
                for (Player target : players.values()) {
                    if (target.getId().equals(p.getPlayerId())) continue;
                    float dx = target.getPosition().getX() - p.getPosition().getX();
                    float dy = target.getPosition().getY() - p.getPosition().getY();
                    float radius = 32f;
                    if (Math.hypot(dx, dy) <= radius) {
                        Player shooter = players.get(p.getPlayerId());
                    int damage = p.getDamage();
                    if (shooter.isDamageBoostActive()
                            && System.currentTimeMillis() <= shooter.getDamageBoostEndTime()) {
                        damage *= Player.DAMAGE_MULTIPLIER;
                    }target.setCurrentHealth(
                                Math.max(0, target.getCurrentHealth() - damage)
                        );
                        if (target.getCurrentHealth() <= 0) {
                            target.setVisible(false);
                        }
                        it.remove();
                        break;
                    }
                }
            }

            // --- Lifetime & Range Cleanup (non-mines) ---
            projectiles.values().removeIf(p ->
                    p.getProjectileType() != MINE
                            && (now - p.getCreationTime() > 1000 || p.getTravelled() >= p.getMaxRange())
            );

            // --- Abgefeuerte Mines entfernen, sobald sie armed sind ---
            projectiles.values().removeIf(p ->
                    p.getProjectileType() == MINE && p.isArmed()
            );

            // --- Ammo-Refill für Nicht-Rifle-Waffen ---
            for (String pid : ammoMap.keySet()) {
                int left = ammoMap.get(pid);
                long last = lastRefill.getOrDefault(pid, 0L);
                int max = getMaxAmmoForType(playerWeapon.getOrDefault(pid, RIFLE_BULLET));
                if (left < max && now - last >= AMMO_REFILL_MS) {
                    ammoMap.put(pid, max);
                    lastRefill.put(pid, now);
                }
            }
            // --- Rifle-Reload ---
            for (String pid : new ArrayList<>(rifleReloading)) {
                long last = lastRifleRefill.getOrDefault(pid, 0L);
                if (rifleAmmoMap.getOrDefault(pid, 0) < RIFLE_MAX_AMMO
                        && now - last >= AMMO_REFILL_MS) {
                    rifleAmmoMap.put(pid, RIFLE_MAX_AMMO);
                    rifleReloading.remove(pid);
                    lastRifleRefill.put(pid, now);
                }
            }

        }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        // Spieler-Status mit aktuellem Ammo & Weapon
        List<PlayerState> ps = players.values().stream().map(p -> {
            String pid = p.getId();
            ProjectileType wep = playerWeapon.getOrDefault(pid, RIFLE_BULLET);
            int ammo = (wep == RIFLE_BULLET)
                    ? rifleAmmoMap.getOrDefault(pid, RIFLE_MAX_AMMO)
                    : ammoMap.getOrDefault(pid, getMaxAmmoForType(wep));
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
            msg.setProjectiles(new ArrayList<>(projectiles.values()));

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
            return new ArrayList<>(projectiles.values());
        }

        @Override
        public Position getPlayerPosition (String playerId){
            return players.get(playerId).getPosition();
        }

        // Hilfsfunktion
        private int getMaxAmmoForType (ProjectileType type){
            return switch (type) {
                case SNIPER -> 1;
                case SHOTGUN_PELLET -> 3;
                case MINE -> 1;
                case RIFLE_BULLET -> RIFLE_MAX_AMMO;
                default -> DEFAULT_MAX_AMMO;
            };
        }

    }


