package at.fhv.spiel_backend.logic;

import at.fhv.spiel_backend.model.*;
import at.fhv.spiel_backend.ws.PlayerState;
import at.fhv.spiel_backend.ws.StateUpdateMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultGameLogic implements GameLogic {
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, Projectile> projectiles = new ConcurrentHashMap<>();

    private static final int DEFAULT_MAX_AMMO = 3;
    private static final int RIFLE_MAX_AMMO = 15;
    private static final long AMMO_REFILL_MS = 2000;
    private final Map<String, Integer> ammoMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRefill = new ConcurrentHashMap<>();
    private final Map<String, Integer> rifleAmmoMap  = new ConcurrentHashMap<>();
    private final Map<String, Long>    lastRifleRefill = new ConcurrentHashMap<>();
    private final Set<String> rifleReloading = ConcurrentHashMap.newKeySet();

    private final Map<String, ProjectileType> playerWeapon = new ConcurrentHashMap<>();

    @Override
    public StateUpdateMessage buildStateUpdate() {
        List<PlayerState> ps = players.values().stream()
                .map(p -> {
                    String pid = p.getId();
                    // welches Ammo anzeigen?
                    ProjectileType wep = playerWeapon.getOrDefault(pid, ProjectileType.RIFLE_BULLET);
                    int ammo = (wep == ProjectileType.RIFLE_BULLET)
                            ? rifleAmmoMap.getOrDefault(pid, RIFLE_MAX_AMMO)
                            : ammoMap.getOrDefault(pid, DEFAULT_MAX_AMMO);
                    return new PlayerState(
                            p.getId(),
                            p.getPosition(),
                            p.getCurrentHealth(),
                            p.isVisible(),
                            ammo, wep);
                })
                .collect(Collectors.toList());
        StateUpdateMessage msg = new StateUpdateMessage();
        msg.setPlayers(ps);
        msg.setEvents(Collections.emptyList());
        msg.setProjectiles(getProjectiles());
        return msg;
    }

    @Override
    public void movePlayer(String playerId, float x, float y) {
        Player p = players.get(playerId);
        if (p != null) {
            p.setPosition(new Position(x, y));
        }
    }

    @Override
    public void addPlayer(String playerId) {
        // instantiate player with default brawler & gadget
        Brawler br = new Brawler(playerId, 1, 100, new Position(0,0));
        players.put(playerId, new Player(playerId, br.getLevel(), br.getMaxHealth(), br.getPosition()));

        playerWeapon.put(playerId, ProjectileType.RIFLE_BULLET);
        ammoMap.put(playerId, DEFAULT_MAX_AMMO);
        lastRefill.put(playerId, System.currentTimeMillis());
        rifleAmmoMap.put(playerId, RIFLE_MAX_AMMO);
        lastRifleRefill.put(playerId, System.currentTimeMillis());
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
        ammoMap.remove(playerId);
        lastRefill.remove(playerId);
    }


    public void setPlayerWeapon(String playerId, ProjectileType type) {
        playerWeapon.put(playerId, type);
    }

    @Override
    public void spawnProjectile(String playerId, Position position, Position direction, ProjectileType type) {
        if (playerWeapon.get(playerId) != type) return;
        long now = System.currentTimeMillis();

        // Ammo-Verbrauch einmalig
        if (type == ProjectileType.RIFLE_BULLET) {
            int ammoLeft = rifleAmmoMap.getOrDefault(playerId, RIFLE_MAX_AMMO);
            if (ammoLeft <= 0) return;
            ammoLeft -= 1;
            rifleAmmoMap.put(playerId, ammoLeft);
            if (ammoLeft == 0) {
                rifleReloading.add(playerId);
                lastRifleRefill.put(playerId, now);
            }

        }
        else {
            int ammoLeft = ammoMap.getOrDefault(playerId, DEFAULT_MAX_AMMO);
            if (ammoLeft <= 0) return;
            ammoMap.put(playerId, ammoLeft - 1);
        }

        double baseAngle = Math.atan2(direction.getY(), direction.getX());

        switch(type) {
            case SHOTGUN_PELLET -> {
                // Anzahl der Pellets und max. Spread in Grad
                final int PELLET_COUNT = 5;
                final float SPREAD_DEG = 25f;
                final double stepDeg      = SPREAD_DEG / (PELLET_COUNT - 1);
                for (int i = 0; i < PELLET_COUNT; i++) {
                    // zufälliger Winkel-Offset in [-SPREAD_DEG, +SPREAD_DEG]
                    double offsetDeg = -SPREAD_DEG/2 + i * stepDeg;
                    double theta = baseAngle + Math.toRadians(offsetDeg);;
                    Position dirI = new Position((float)Math.cos(theta), (float)Math.sin(theta));


                    spawnSingle(projectId(playerId), playerId, position, dirI,
                            /*speed*/500f, /*damage*/20, now,
                            ProjectileType.SHOTGUN_PELLET, /*range*/400f);
                }
            }
            case SNIPER -> {
                spawnSingle(projectId(playerId), playerId, position, direction,
                        800f, 75, now, ProjectileType.SNIPER, 2000f);
            }
            case RIFLE_BULLET -> {
                spawnSingle(projectId(playerId), playerId, position, direction,
                        500f, 15, now, ProjectileType.RIFLE_BULLET, 1000f);
            }
            case MINE -> {
                Projectile p = spawnSingle(projectId(playerId), playerId, position, direction,
                        300f, 100, now, ProjectileType.MINE, 300f);
                p.setArmTime(0L);
                p.setArmed(false);
            }
            default -> { /* no-op */ }
        }
    }

    // Hilfsmethode, damit Du nicht ständig new Projectile(...) tippen musst:
    private Projectile spawnSingle(String id, String playerId, Position startPos, Position startDir,
                                   float speed, int dmg, long creationTime,
                                   ProjectileType type, float maxRange) {

        Position posCopy = new Position(
                startPos.getX(),
                startPos.getY()
        );
        Position dirCopy = new Position(
                startDir.getX(),
                startDir.getY()
        );

        Projectile p = new Projectile(id, playerId, posCopy, dirCopy, speed, dmg, creationTime, type, maxRange, 0f);
        projectiles.put(id, p);
        return p;
    }

    private String projectId(String playerId) {
        return playerId + "-" + UUID.randomUUID();
    }

    @Override
    public void updateProjectiles() {
        long now = System.currentTimeMillis();

        projectiles.values().removeIf(p -> p.getProjectileType() != ProjectileType.MINE && now - p.getCreationTime() > 1000);

        float deltaTime = 0.016f; // ca. 60 updates/sec
        projectiles.values().forEach(projectile ->
        {  if (projectile.getProjectileType() != ProjectileType.MINE) {
            projectile.getPosition().setX(projectile.getPosition().getX() + projectile.getDirection().getX() * projectile.getSpeed() * deltaTime);
            projectile.getPosition().setY(projectile.getPosition().getY() + projectile.getDirection().getY() * projectile.getSpeed() * deltaTime);
            projectile.setTravelled(projectile.getTravelled() + (float) Math.hypot(projectile.getDirection().getX() * projectile.getSpeed() * deltaTime, projectile.getDirection().getY() * projectile.getSpeed() * deltaTime));
        }
        else{
            if (projectile.getTravelled() < projectile.getMaxRange()) {
                float dx = projectile.getDirection().getX() * projectile.getSpeed() * deltaTime;
                float dy = projectile.getDirection().getY() * projectile.getSpeed() * deltaTime;
                projectile.getPosition().setX(projectile.getPosition().getX() + dx);
                projectile.getPosition().setY(projectile.getPosition().getY() + dy);
                projectile.setTravelled(projectile.getTravelled() + (float) Math.hypot(dx, dy));
            }
            else {
                 if (projectile.getArmTime() == 0L) {
                    // erste Frame nach Wurfende: starte Timer
                     projectile.setArmTime(now + 2000);
                 } else if (!projectile.isArmed() && now >= projectile.getArmTime()) {
                     projectile.setArmed(true);
                 }}
        }
        });

        // 2) Range- und Lebenszeit-Check
        projectiles.values().removeIf(p ->
                (p.getProjectileType() != ProjectileType.MINE && p.getTravelled() >= p.getMaxRange())
        );

        projectiles.values().removeIf(p ->
                p.getProjectileType() == ProjectileType.MINE
                        && p.isArmed()
        );

        for (String playerId : ammoMap.keySet()) {
            int ammoLeft = ammoMap.get(playerId);
            long last    = lastRefill.getOrDefault(playerId, 0L);
            if (ammoLeft < DEFAULT_MAX_AMMO && now - last >= AMMO_REFILL_MS) {
                ammoMap.put(playerId, DEFAULT_MAX_AMMO);
                lastRefill.put(playerId, now);
            }
        }
        for (String pid : rifleAmmoMap.keySet()) {
            if (!rifleReloading.contains(pid)) continue;
            int left = rifleAmmoMap.get(pid);
            long last= lastRifleRefill.getOrDefault(pid, 0L);
            if (left < RIFLE_MAX_AMMO && now - last >= AMMO_REFILL_MS) {
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
    public Position getPlayerPosition(String playerId) {
        return players.get(playerId).getPosition();
    }






}

