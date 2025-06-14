package at.fhv.spiel_service.service.game.manager.projectile;

import at.fhv.spiel_service.domain.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static at.fhv.spiel_service.domain.ProjectileType.*;

public class ProjectileManagerImpl implements ProjectileManager {
    private static final int DEFAULT_MAX_AMMO = 3;
    private static final int RIFLE_MAX_AMMO = 15;
    private static final long AMMO_REFILL_MS = 2000;

    private final GameMap gameMap;
    private final Map<String, Player> players;
    private final Map<String, Projectile> projectiles = new ConcurrentHashMap<>();

    // Ammo-Tracking
    private final Map<String, Integer> ammoMap          = new ConcurrentHashMap<>();
    private final Map<String, Long>    lastRefill       = new ConcurrentHashMap<>();
    private final Map<String, Integer> rifleAmmoMap     = new ConcurrentHashMap<>();
    private final Set<String> rifleReloading   = ConcurrentHashMap.newKeySet();
    private final Map<String, Long>    lastRifleRefill  = new ConcurrentHashMap<>();
    private final Map<String, ProjectileType> playerWeapon = new ConcurrentHashMap<>();

    public ProjectileManagerImpl(GameMap gameMap, Map<String, Player> players) {
        this.gameMap = gameMap;
        this.players = players;
    }

    @Override
    public void spawnProjectile(String playerId, Position position, Position direction, ProjectileType type) {
        // (1) Waffe prüfen & Ammo verwalten
        ProjectileType current = playerWeapon.getOrDefault(playerId, RIFLE_BULLET);
        if (current != type) return;
        long now = System.currentTimeMillis();

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

        // (2) Projektil(s) erzeugen
        double baseAngle = Math.atan2(direction.getY(), direction.getX());
        switch (type) {
            case SHOTGUN_PELLET -> handleShotgun(projectiles, playerId, position, baseAngle, now);
            case SNIPER        -> spawnSingle(playerId, position, direction, 1400f, 30, now, SNIPER, 2500f);
            case RIFLE_BULLET  -> spawnSingle(playerId, position, direction, 1000f, 2,  now, RIFLE_BULLET, 2000f);
            case MINE          -> {
                Projectile p = spawnSingle(playerId, position, direction, 750f, 40, now, MINE, 700f);
                p.setArmTime(0L);
                p.setArmed(false);
            }
            default -> { /* no-op */ }
        }
    }

    @Override
    public void updateProjectiles(float delta) {
        long now = System.currentTimeMillis();

        Function<String, Float> getSpeedFactor = shooterId -> {
            Player shooter = getPlayer(shooterId);
            if (shooter != null
                    && shooter.isSpeedBoostActive()
                    && now <= shooter.getSpeedBoostEndTime()) {
                return 2f;
            }
            return 1f;
        };

        Iterator<Projectile> it = projectiles.values().iterator();
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
//            int tx = (int) (p.getPosition().getX() / gameMap.getTileWidth());
//            int ty = (int) (p.getPosition().getY() / gameMap.getTileHeight());
//            if (p.getProjectileType() != MINE) {
//                if (gameMap.isWallAt(tx, ty)) {
//                    it.remove();
//                    continue;
//                }
//            }

            // --- Treffer mit NPCs prüfen ---
//            for (NPC npc : npcs) {
//                if (npc.getCurrentHealth() <= 0) continue;
//
//                float dx = npc.getPosition().getX() - p.getPosition().getX();
//                float dy = npc.getPosition().getY() - p.getPosition().getY();
//                float dist = (float) Math.hypot(dx, dy);
//
//                if (dist < 40f) { // Kollision bei Nähe
//                    npc.setCurrentHealth(Math.max(0, npc.getCurrentHealth() - p.getDamage()));
//                    it.remove(); // Projektil entfernen
//                    break;
//                }
//            }


//            int tileX = (int)(p.getPosition().getX() / gameMap.getTileWidth());
//            int tileY = (int)(p.getPosition().getY() / gameMap.getTileHeight());
//            String key = tileX + "," + tileY;

            //Crate crate = crates.get(key);
//            if (crate != null) {
//                crate.setWasHit(true);
//                crate.setCurrentHealth(Math.max(0, crate.getCurrentHealth() - p.getDamage()));
//                it.remove();
//
//                System.out.println("💥 Kiste getroffen bei Tile: " + key);
//
//                if (crate.getCurrentHealth() <= 0) {
//                    crates.remove(key);
//                    System.out.println("🧨 Kiste zerstört");
//
//                    // 💰 Coins vergeben
//                    String shooterId = p.getPlayerId();
//                    int old = playerCoins.getOrDefault(shooterId, 0);
//                    playerCoins.put(shooterId, old + 10);
//                    System.out.println("💰 " + shooterId + " bekommt 10 Coins → gesamt: " + (old + 10));
//                }
//
//
//                continue;
//            }

            // --- Spieler-Kollision ---
//            for (Player target : players.values()) {
//                if (target.getId().equals(p.getPlayerId())) continue;
//                float dx = target.getPosition().getX() - p.getPosition().getX();
//                float dy = target.getPosition().getY() - p.getPosition().getY();
//                float radius = 32f;
//                if (Math.hypot(dx, dy) <= radius) {
//                    Player shooter = players.get(p.getPlayerId());
//                    int damage = p.getDamage();
//                    if (shooter.isDamageBoostActive()
//                            && System.currentTimeMillis() <= shooter.getDamageBoostEndTime()) {
//                        damage *= Player.DAMAGE_MULTIPLIER;
//                    }target.setCurrentHealth(
//                            Math.max(0, target.getCurrentHealth() - damage)
//                    );
//                    if (target.getCurrentHealth() <= 0) {
//                        target.setVisible(false);
//                    }
//                    it.remove();
//                    break;
//                }
//            }
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
    public List<Projectile> getProjectiles() {
        return new ArrayList<>(projectiles.values());
    }

    @Override
    public int getCurrentAmmo(String playerId) {
        ProjectileType wep = getCurrentWeapon(playerId);
        return wep == RIFLE_BULLET
                ? rifleAmmoMap.getOrDefault(playerId, RIFLE_MAX_AMMO)
                : ammoMap.getOrDefault(playerId, getMaxAmmoForType(wep));
    }

    @Override
    public ProjectileType getCurrentWeapon(String playerId) {
        return playerWeapon.getOrDefault(playerId, RIFLE_BULLET);
    }

    /* ─── Hilfs-Methoden ─────────────────────────────────────────────────── */

    private void handleShotgun(Map<String, Projectile> projMap,
                               String playerId,
                               Position pos,
                               double baseAngle,
                               long now) {
        final int PELLET_COUNT = 5;
        final float SPREAD = 10f;
        double step = SPREAD / (PELLET_COUNT - 1);
        for (int i = 0; i < PELLET_COUNT; i++) {
            double offset = -SPREAD/2 + i*step;
            double theta  = baseAngle + Math.toRadians(offset);
            Position dirI = new Position((float)Math.cos(theta),(float)Math.sin(theta));
            spawnSingle(playerId, pos, dirI, 800f, 5, now, SHOTGUN_PELLET, 700f);
        }
    }

    private Projectile spawnSingle(String playerId,
                                   Position startPos,
                                   Position startDir,
                                   float speed,
                                   int damage,
                                   long now,
                                   ProjectileType type,
                                   float maxRange) {
        String id = playerId + "-" + UUID.randomUUID();
        Projectile p = new Projectile(id, playerId,
                new Position(startPos.getX(), startPos.getY(), startPos.getAngle()),
                new Position(startDir.getX(),   startDir.getY(),   0),
                speed, damage, now, type, maxRange, 0f);
        projectiles.put(id, p);
        return p;
    }

    @Override
    public int getMaxAmmoForType(ProjectileType type) {
        return switch (type) {
            case SNIPER         -> 1;
            case SHOTGUN_PELLET -> 3;
            case MINE           -> 1;
            case RIFLE_BULLET   -> RIFLE_MAX_AMMO;
            default             -> DEFAULT_MAX_AMMO;
        };
    }

    @Override
    public void initPlayer(String playerId, ProjectileType initialWeapon) {
        playerWeapon.put(playerId, initialWeapon);

        // Init Ammo-Maps
        if (initialWeapon == RIFLE_BULLET) {
            rifleAmmoMap.put(playerId, RIFLE_MAX_AMMO);
            lastRifleRefill.put(playerId, System.currentTimeMillis());
        } else {
            int max = getMaxAmmoForType(initialWeapon);
            ammoMap.put(playerId, max);
            lastRefill.put(playerId, System.currentTimeMillis());
        }
    }

    @Override
    public void removeProjectile(String playerId) {

        projectiles.values().removeIf(p -> p.getPlayerId().equals(playerId));
        // Ammo-Maps aufräumen
        ammoMap.remove(playerId);
        lastRefill.remove(playerId);
        rifleAmmoMap.remove(playerId);
        lastRifleRefill.remove(playerId);
        rifleReloading.remove(playerId);
        // Waffe zurücksetzen
        playerWeapon.remove(playerId);
    }

    @Override
    public void setWeapon(String playerId, ProjectileType weapon) {
        playerWeapon.put(playerId, weapon);
        long now = System.currentTimeMillis();

        // Nachladen je nach Typ
        if (weapon == RIFLE_BULLET) {
            rifleAmmoMap.put(playerId, RIFLE_MAX_AMMO);
            rifleReloading.remove(playerId);
            lastRifleRefill.put(playerId, now);
        } else {
            int max = getMaxAmmoForType(weapon);
            ammoMap.put(playerId, max);
            lastRefill.put(playerId, now);
        }
    }

    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

}
