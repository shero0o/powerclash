package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.model.*;
import at.fhv.spiel_backend.ws.PlayerState;
import at.fhv.spiel_backend.ws.StateUpdateMessage;
import at.fhv.spiel_backend.server.map.GameMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultGameLogic implements GameLogic {
    // --- Spielzustand ---
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, Projectile> projectiles = new ConcurrentHashMap<>();
    private GameMap gameMap;

    // --- Waffen & Ammo ---
    private static final int DEFAULT_MAX_AMMO      = 3;
    private static final int RIFLE_MAX_AMMO        = 15;
    private static final long AMMO_REFILL_MS       = 2000;
    private final Map<String, Integer> ammoMap             = new ConcurrentHashMap<>();
    private final Map<String, Long>    lastRefill          = new ConcurrentHashMap<>();
    private final Map<String, Integer> rifleAmmoMap        = new ConcurrentHashMap<>();
    private final Map<String, Long>    lastRifleRefill     = new ConcurrentHashMap<>();
    private final Set<String>          rifleReloading      = ConcurrentHashMap.newKeySet();
    private final Map<String, ProjectileType> playerWeapon = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    @Override
    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    @Override
    public void addPlayer(String playerId, String brawlerId) {
        // 1) Spawn-Position je nach Reihenfolge
        int index = players.size();
        Position spawn = (index == 0)
                ? new Position(1200, 1200, 0)   // Spieler 1
                : new Position(6520, 1200, 0);  // Spieler 2

        // 2) Brawler erzeugen
        Brawler br = switch (brawlerId.toLowerCase()) {
            case "tank"  -> new Brawler(playerId, 1, 200, spawn);
            case "mage"  -> new Brawler(playerId, 1,  80, spawn);
            case "healer"-> new Brawler(playerId, 1, 100, spawn);
            default      -> new Brawler(playerId, 1, 100, spawn); // sniper
        };

        // 3) Player-Objekt anlegen
        players.put(playerId,
                new Player(br.getId(), br.getLevel(), br.getMaxHealth(), br.getPosition())
        );

        // 4) Ammo-System initialisieren
        playerWeapon.put(playerId, ProjectileType.RIFLE_BULLET);
        ammoMap.put(playerId, getMaxAmmoForType(ProjectileType.RIFLE_BULLET));
        lastRefill.put(playerId, System.currentTimeMillis());
        rifleAmmoMap.put(playerId, RIFLE_MAX_AMMO);
        lastRifleRefill.put(playerId, System.currentTimeMillis());
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
        ammoMap.remove(playerId);
        lastRefill.remove(playerId);
        rifleAmmoMap.remove(playerId);
        lastRifleRefill.remove(playerId);
        rifleReloading.remove(playerId);
        playerWeapon.remove(playerId);

        // Auch alle zugehörigen Projektile löschen
        projectiles.values().removeIf(p -> p.getPlayerId().equals(playerId));
    }

    @Override
    public void movePlayer(String playerId, float x, float y, float angle) {
        Player p = players.get(playerId);
        if (p == null || gameMap == null) return;

        int tileX = (int)(x / gameMap.getTileWidth());
        int tileY = (int)(y / gameMap.getTileHeight());
        // nur bewegen, wenn kein Wall-Tile
        if (!gameMap.isWallAt(tileX, tileY)) {
            p.setPosition(new Position(x, y, angle));
        }
    }

    @Override
    public void setPlayerWeapon(String playerId, ProjectileType type) {
        playerWeapon.put(playerId, type);
        int max = getMaxAmmoForType(type);
        ammoMap.put(playerId, max);
        lastRefill.put(playerId, System.currentTimeMillis());
    }

    @Override
    public void spawnProjectile(String playerId,
                                Position position,
                                Position direction,
                                ProjectileType type) {
        // Nur erlaubte Waffe
        if (playerWeapon.getOrDefault(playerId, ProjectileType.RIFLE_BULLET) != type) {
            return;
        }
        long now = System.currentTimeMillis();

        // --- Ammo-Verbrauch ---
        if (type == ProjectileType.RIFLE_BULLET) {
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
                final float SPREAD_DEG = 25f;
                double step    = SPREAD_DEG / (PELLET_COUNT - 1);
                for (int i = 0; i < PELLET_COUNT; i++) {
                    double offset = -SPREAD_DEG/2 + i * step;
                    double theta  = baseAngle + Math.toRadians(offset);
                    Position dirI = new Position((float)Math.cos(theta), (float)Math.sin(theta));
                    spawnSingle(
                            projectId(playerId),
                            playerId,
                            position,
                            dirI,
                            500f, 20, now,
                            ProjectileType.SHOTGUN_PELLET, 400f
                    );
                }
            }
            case SNIPER -> spawnSingle(
                    projectId(playerId),
                    playerId,
                    position,
                    direction,
                    800f, 75, now,
                    ProjectileType.SNIPER, 2000f
            );
            case RIFLE_BULLET -> spawnSingle(
                    projectId(playerId),
                    playerId,
                    position,
                    direction,
                    500f, 15, now,
                    ProjectileType.RIFLE_BULLET, 1000f
            );
            case MINE -> {
                Projectile p = spawnSingle(
                        projectId(playerId),
                        playerId,
                        position,
                        direction,
                        300f, 100, now,
                        ProjectileType.MINE, 300f
                );
                p.setArmTime(0L);
                p.setArmed(false);
            }
            default -> { /* no-op */ }
        }
    }

    private Projectile spawnSingle(String id,
                                   String playerId,
                                   Position startPos,
                                   Position startDir,
                                   float speed,
                                   int damage,
                                   long creationTime,
                                   ProjectileType type,
                                   float maxRange) {
        Position pos = new Position(startPos.getX(), startPos.getY(), startPos.getAngle());
        Position dir = new Position(startDir.getX(), startDir.getY(), 0);
        Projectile p = new Projectile(
                id, playerId, pos, dir, speed, damage, creationTime, type, maxRange, 0f
        );
        projectiles.put(id, p);
        return p;
    }

    private String projectId(String playerId) {
        return playerId + "-" + UUID.randomUUID();
    }

    @Override
    public void updateProjectiles() {
        long now = System.currentTimeMillis();
        float delta = 0.016f; // ~60 FPS

        Iterator<Projectile> it = projectiles.values().iterator();
        while (it.hasNext()) {
            Projectile p = it.next();

            // --- Bewegung & Travelled ---
            if (p.getProjectileType() != ProjectileType.MINE) {
                p.getPosition().setX(p.getPosition().getX()
                        + p.getDirection().getX() * p.getSpeed() * delta);
                p.getPosition().setY(p.getPosition().getY()
                        + p.getDirection().getY() * p.getSpeed() * delta);
                p.setTravelled(p.getTravelled()
                        + (float)Math.hypot(
                        p.getDirection().getX() * p.getSpeed() * delta,
                        p.getDirection().getY() * p.getSpeed() * delta
                ));
            } else {
                // Mine rollt bis zur MaxRange, dann armt sie
                if (p.getTravelled() < p.getMaxRange()) {
                    float dx = p.getDirection().getX() * p.getSpeed() * delta;
                    float dy = p.getDirection().getY() * p.getSpeed() * delta;
                    p.getPosition().setX(p.getPosition().getX() + dx);
                    p.getPosition().setY(p.getPosition().getY() + dy);
                    p.setTravelled(p.getTravelled() + (float)Math.hypot(dx, dy));
                } else {
                    if (p.getArmTime() == 0L) {
                        p.setArmTime(now + 2000);
                    } else if (!p.isArmed() && now >= p.getArmTime()) {
                        p.setArmed(true);
                    }
                }
            }

            // --- Wand-Kollision ---
            int tx = (int)(p.getPosition().getX() / gameMap.getTileWidth());
            int ty = (int)(p.getPosition().getY() / gameMap.getTileHeight());
            if (gameMap.isWallAt(tx, ty)) {
                it.remove();
                continue;
            }

            // --- Spieler-Kollision ---
            for (Player target : players.values()) {
                if (target.getId().equals(p.getPlayerId()) || !target.isVisible()) continue;
                float dx = target.getPosition().getX() - p.getPosition().getX();
                float dy = target.getPosition().getY() - p.getPosition().getY();
                float radius = 32f;
                if (Math.hypot(dx, dy) <= radius) {
                    target.setCurrentHealth(
                            Math.max(0, target.getCurrentHealth() - p.getDamage())
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
                p.getProjectileType() != ProjectileType.MINE
                        && (now - p.getCreationTime() > 1000 || p.getTravelled() >= p.getMaxRange())
        );

        // --- Abgefeuerte Mines entfernen, sobald sie armed sind ---
        projectiles.values().removeIf(p ->
                p.getProjectileType() == ProjectileType.MINE && p.isArmed()
        );

        // --- Ammo-Refill für Nicht-Rifle-Waffen ---
        for (String pid : ammoMap.keySet()) {
            int left  = ammoMap.get(pid);
            long last = lastRefill.getOrDefault(pid, 0L);
            int max   = getMaxAmmoForType(playerWeapon.getOrDefault(pid, ProjectileType.RIFLE_BULLET));
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
            ProjectileType wep = playerWeapon.getOrDefault(pid, ProjectileType.RIFLE_BULLET);
            int ammo = (wep == ProjectileType.RIFLE_BULLET)
                    ? rifleAmmoMap.getOrDefault(pid, RIFLE_MAX_AMMO)
                    : ammoMap.getOrDefault(pid, getMaxAmmoForType(wep));
            return new PlayerState(
                    pid,
                    p.getPosition(),
                    p.getCurrentHealth(),
                    p.isVisible(),
                    ammo,
                    wep
            );
        }).collect(Collectors.toList());

        StateUpdateMessage msg = new StateUpdateMessage();
        msg.setPlayers(ps);
        msg.setEvents(Collections.emptyList());
        msg.setProjectiles(new ArrayList<>(projectiles.values()));
        return msg;
    }

    @Override
    public List<Projectile> getProjectiles() {
        return new ArrayList<>(projectiles.values());
    }

    @Override
    public Position getPlayerPosition(String playerId) {
        return players.get(playerId).getPosition();
    }

    // Hilfsfunktion
    private int getMaxAmmoForType(ProjectileType type) {
        return switch (type) {
            case SNIPER         -> 1;
            case SHOTGUN_PELLET -> 3;
            case MINE           -> 1;
            case RIFLE_BULLET   -> RIFLE_MAX_AMMO;
            default             -> DEFAULT_MAX_AMMO;
        };
    }
}
