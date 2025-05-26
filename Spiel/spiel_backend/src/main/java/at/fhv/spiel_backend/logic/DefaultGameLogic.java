package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.model.*;
import at.fhv.spiel_backend.ws.CrateState;
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
    private final Map<String, String> playerBrawler = new ConcurrentHashMap<>();
    private final Map<String, String> playerNames = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    private final Map<String, Crate> crates = new ConcurrentHashMap<>();
    private final Map<String, Integer> playerCoins = new ConcurrentHashMap<>();




    @Override
    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;

        for (Position pos : gameMap.getCratePositions()) {
            int tileX = (int) pos.getX();
            int tileY = (int) pos.getY();
            String key = tileX + "," + tileY;
            crates.put(key, new Crate(UUID.randomUUID().toString(), new Position(tileX, tileY)));
        }

    }



    @Override
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    @Override
    public void attack(String playerId, float dirX, float dirY, float angle) {

    }

    @Override
    public void addPlayer(String playerId, String brawlerId, String playerName) {
        // wenn kein Brawler übergeben wurde, Default nehmen
        if (brawlerId == null || brawlerId.isBlank()) {
            brawlerId = "sniper";
        }

        // spawn-Logik wie gehabt …
        int index = players.size();
        Position spawn = (index == 0)
                ? new Position(1200, 1200, 0)
                : new Position(6520, 1200, 0);
        Brawler br = switch(brawlerId.toLowerCase()) {
            case "tank"   -> new Brawler(playerId,1,200,spawn);
            case "mage"   -> new Brawler(playerId,1, 80,spawn);
            case "healer" -> new Brawler(playerId,1,100,spawn);
            default       -> new Brawler(playerId,1,100,spawn);
        };
        players.put(playerId,
                new Player(br.getId(), br.getLevel(), br.getMaxHealth(), br.getPosition())
        );

        // Ammo initialisieren …
        playerWeapon.put(playerId, ProjectileType.RIFLE_BULLET);
        ammoMap.put(playerId, getMaxAmmoForType(ProjectileType.RIFLE_BULLET));
        lastRefill.put(playerId, System.currentTimeMillis());
        rifleAmmoMap.put(playerId, RIFLE_MAX_AMMO);
        lastRifleRefill.put(playerId, System.currentTimeMillis());
        playerBrawler.put(playerId, brawlerId);
        playerNames.put(playerId, playerName); // aus DTO übergeben
        playerCoins.put(playerId, 0);


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
        playerBrawler.remove(playerId);

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
        boolean crateBlocks = crates.values().stream()
                .anyMatch(c -> {
                    int cx = (int) c.getPosition().getX();
                    int cy = (int) c.getPosition().getY();
                    return cx == tileX && cy == tileY;
                });

        if (!gameMap.isWallAt(tileX, tileY) && !crateBlocks) {
            System.out.println("Move to " + tileX + "," + tileY +
                    " – wall: " + gameMap.isWallAt(tileX, tileY) +
                    ", crate: " + crateBlocks);

            p.setPosition(new Position(x, y, angle));

            Position tilePos = new Position(tileX, tileY);
            if (gameMap.isBushTile(tilePos)) {
                p.setVisible(false);
            } else {
                p.setVisible(true);
            }
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
    public void applyEnvironmentalEffects() {
        long now = System.currentTimeMillis();

        for (Player p : players.values()) {
            if (p.getCurrentHealth() <= 0) continue;

            Position pos = p.getPosition();
            int tileX = (int)(pos.getX() / gameMap.getTileWidth());
            int tileY = (int)(pos.getY() / gameMap.getTileHeight());
            Position tilePos = new Position(tileX, tileY);

            if (gameMap.isPoisonTile(tilePos)) {
                long last = p.getLastPoisonTime();
                if (now - last >= 1000) {
                    p.setCurrentHealth(Math.max(0, p.getCurrentHealth() - 15));
                    p.setLastPoisonTime(now);


                    if (p.getCurrentHealth() <= 0) {
                        p.setVisible(false); // Optional: unsichtbar wenn tot
                    }
                }
            } else {
                // Nicht in Giftfeld → Reset poison timer
                p.setLastPoisonTime(0);
            }

            if (gameMap.isHealTile(tilePos)) {
                long lastHeal = p.getLastHealTime();
                if (now - lastHeal >= 1000) {
                    int newHP = Math.min(p.getMaxHealth(), p.getCurrentHealth() + 15);
                    p.setCurrentHealth(newHP);
                    p.setLastHealTime(now);

                }
            } else {
                p.setLastHealTime(0);
            }
        }
    }

    @Override
    public List<Crate> getCrates() {
        return new ArrayList<>(crates.values());
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
                final float SPREAD_DEG = 10f;
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
                    ProjectileType.SNIPER, 2500f
            );
            case RIFLE_BULLET -> spawnSingle(
                    projectId(playerId),
                    playerId,
                    position,
                    direction,
                    1000f, 2, now,
                    ProjectileType.RIFLE_BULLET, 2000f
            );
            case MINE -> {
                Projectile p = spawnSingle(
                        projectId(playerId),
                        playerId,
                        position,
                        direction,
                        750f, 40, now,
                        ProjectileType.MINE, 700f
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
                        + (float) Math.hypot(
                        p.getDirection().getX() * p.getSpeed() * delta,
                        p.getDirection().getY() * p.getSpeed() * delta
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
            if (p.getProjectileType() != ProjectileType.MINE) {
                if (gameMap.isWallAt(tx, ty)) {
                    it.remove();
                    continue;
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
                    wep,
                    playerBrawler.getOrDefault(p.getId(), "sniper"),
                    playerNames.getOrDefault(p.getId(), "Player"),
                    playerCoins.getOrDefault(pid, 0)
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
