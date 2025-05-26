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
    private final Map<String, Player> players      = new ConcurrentHashMap<>();
    private final Map<String, Projectile> projectiles = new ConcurrentHashMap<>();
    private final List<NPC> npcs = new ArrayList<>();
    private GameMap gameMap;
    private long lastFrameTimeMs = System.currentTimeMillis();

    // --- Zone fields ---
    private boolean zoneActive     = false;
    private Position zoneCenter;
    private float zoneRadius;
    private float zoneShrinkRate;      // px/sec
    private long  zoneStartTimeMs;
    private long  zoneDurationMs;

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

    @Override
    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    @Override
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    @Override
    public void attack(String playerId, float dirX, float dirY, float angle) {
        // unused for melee NPC setup
    }

    /**
     * Register a new NPC for melee AI
     */
    public void addNpc(String npcId,
                       Position spawn,
                       int health,
                       float attackRadius,
                       int damage,
                       float speed,
                       long attackCooldownMs) {
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
    public void activateZone(Position center, float initialRadius, long durationMs) {
        this.zoneActive      = true;
        this.zoneCenter      = center;
        this.zoneRadius      = initialRadius;
        this.zoneDurationMs  = durationMs;
        this.zoneStartTimeMs = System.currentTimeMillis();
        this.zoneShrinkRate  = initialRadius / (durationMs / 1000f);
    }

    @Override
    public void addPlayer(String playerId, String brawlerId, String playerName) {
        if (brawlerId == null || brawlerId.isBlank()) {
            brawlerId = "sniper";
        }

        int index = players.size();
        Position spawn = (index == 0)
                ? new Position(1200, 1200, 0)
                : new Position(6520, 1200, 0);

        Brawler br = switch(brawlerId.toLowerCase()) {
            case "tank"   -> new Brawler(playerId, 1, 200, spawn);
            case "mage"   -> new Brawler(playerId, 1,  80, spawn);
            case "healer" -> new Brawler(playerId, 1, 100, spawn);
            default       -> new Brawler(playerId, 1, 100, spawn);
        };

        players.put(playerId,
                new Player(br.getId(), br.getLevel(), br.getMaxHealth(), br.getPosition())
        );

        // initialize ammo
        playerWeapon.put(playerId, ProjectileType.RIFLE_BULLET);
        ammoMap.put(playerId, getMaxAmmoForType(ProjectileType.RIFLE_BULLET));
        lastRefill.put(playerId, System.currentTimeMillis());
        rifleAmmoMap.put(playerId, RIFLE_MAX_AMMO);
        lastRifleRefill.put(playerId, System.currentTimeMillis());
        playerBrawler.put(playerId, brawlerId);
        playerNames.put(playerId, playerName);
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
        playerNames.remove(playerId);

        projectiles.values().removeIf(p -> p.getPlayerId().equals(playerId));
    }

    @Override
    public void movePlayer(String playerId, float x, float y, float angle) {
        Player p = players.get(playerId);
        if (p == null || gameMap == null) return;

        int tileX = (int)(x / gameMap.getTileWidth());
        int tileY = (int)(y / gameMap.getTileHeight());
        if (!gameMap.isWallAt(tileX, tileY)) {
            p.setPosition(new Position(x, y, angle));
            Position tilePos = new Position(tileX, tileY);
            p.setVisible(!gameMap.isBushTile(tilePos));
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
        float deltaSec = (now - lastFrameTimeMs) / 1000f;
        lastFrameTimeMs = now;

        // poison/heal/zone on players
        for (Player p : players.values()) {
            if (p.getCurrentHealth() <= 0) continue;
            Position pos = p.getPosition();
            int tx = (int)(pos.getX() / gameMap.getTileWidth());
            int ty = (int)(pos.getY() / gameMap.getTileHeight());
            Position tile = new Position(tx, ty);

            if (gameMap.isPoisonTile(tile)) {
                if (now - p.getLastPoisonTime() >= 1000) {
                    p.setCurrentHealth(Math.max(0, p.getCurrentHealth() - 15));
                    p.setLastPoisonTime(now);
                    if (p.getCurrentHealth() <= 0) p.setVisible(false);
                }
            } else {
                p.setLastPoisonTime(0);
            }

            if (gameMap.isHealTile(tile)) {
                if (now - p.getLastHealTime() >= 1000) {
                    p.setCurrentHealth(Math.min(p.getMaxHealth(), p.getCurrentHealth() + 15));
                    p.setLastHealTime(now);
                }
            } else {
                p.setLastHealTime(0);
            }
        }

        // zone shrink & damage outside
        if (zoneActive) {
            zoneRadius = Math.max(0f, zoneRadius - zoneShrinkRate * deltaSec);
            for (Player p : players.values()) {
                if (p.getCurrentHealth() <= 0) continue;
                float dx = p.getPosition().getX() - zoneCenter.getX();
                float dy = p.getPosition().getY() - zoneCenter.getY();
                if (Math.hypot(dx, dy) > zoneRadius) {
                    int dmg = (int)Math.ceil(0.05f * deltaSec);
                    p.setCurrentHealth(Math.max(0, p.getCurrentHealth() - dmg));
                }
            }
        }

        // ── NPC AI: chase & melee ───────────────────────────────
        for (NPC npc : npcs) {
            // 1) find nearest alive player
            Player target = null;
            float minDist = Float.MAX_VALUE;
            for (Player p : players.values()) {
                if (p.getCurrentHealth() <= 0) continue;
                float dx = p.getPosition().getX() - npc.getPosition().getX();
                float dy = p.getPosition().getY() - npc.getPosition().getY();
                float d  = (float)Math.hypot(dx, dy);
                if (d < minDist) {
                    minDist = d;
                    target = p;
                }
            }
            if (target == null) continue;

            // 2) compute facing angle once
            float dx = target.getPosition().getX() - npc.getPosition().getX();
            float dy = target.getPosition().getY() - npc.getPosition().getY();
            float angle = (float)Math.atan2(dy, dx);

            // 3) move toward if outside attack radius
            if (minDist > npc.getAttackRadius()) {
                float len = (float)Math.hypot(dx, dy);
                if (len > 0) {
                    float nx = dx / len, ny = dy / len;
                    float newX = npc.getPosition().getX() + nx * npc.getSpeed() * deltaSec;
                    float newY = npc.getPosition().getY() + ny * npc.getSpeed() * deltaSec;
                    int tx = (int)(newX / gameMap.getTileWidth());
                    int ty = (int)(newY / gameMap.getTileHeight());
                    if (!gameMap.isWallAt(tx, ty)) {
                        npc.setPosition(new Position(newX, newY, angle));
                    } else {
                        // can't walk through wall, but still update facing
                        npc.setPosition(new Position(npc.getPosition().getX(),
                                npc.getPosition().getY(),
                                angle));
                    }
                }
            } else {
                // inside melee range: just update facing
                npc.setPosition(new Position(npc.getPosition().getX(),
                        npc.getPosition().getY(),
                        angle));
            }

            // 4) melee attack if in range & cooldown passed
            if (minDist <= npc.getAttackRadius()
                    && now - npc.getLastAttackTime() >= npc.getAttackCooldownMs()) {
                int newHp = Math.max(0, target.getCurrentHealth() - npc.getDamage());
                target.setCurrentHealth(newHp);
                if (newHp == 0) target.setVisible(false);
                npc.setLastAttackTime(now);
            }
        }
    }

    @Override
    public void spawnProjectile(String playerId,
                                Position position,
                                Position direction,
                                ProjectileType type) {
        // existing projectile code...
        // (unchanged; omitted for brevity)
    }

    @Override
    public void updateProjectiles() {
        // existing projectile update code...
        // (unchanged; omitted for brevity)
    }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        List<PlayerState> ps = players.values().stream()
                .map(p -> {
                    String pid = p.getId();
                    ProjectileType w = playerWeapon.getOrDefault(pid, ProjectileType.RIFLE_BULLET);
                    int ammo = w == ProjectileType.RIFLE_BULLET
                            ? rifleAmmoMap.getOrDefault(pid, RIFLE_MAX_AMMO)
                            : ammoMap.getOrDefault(pid, getMaxAmmoForType(w));
                    return new PlayerState(
                            pid,
                            p.getPosition(),
                            p.getCurrentHealth(),
                            p.isVisible(),
                            ammo,
                            w,
                            playerBrawler.getOrDefault(pid, "sniper"),
                            playerNames.getOrDefault(pid, "Player")
                    );
                })
                .collect(Collectors.toList());

        StateUpdateMessage msg = new StateUpdateMessage();
        msg.setPlayers(ps);
        msg.setEvents(Collections.emptyList());
        msg.setProjectiles(new ArrayList<>(projectiles.values()));
        if (zoneActive) {
            long elapsed = System.currentTimeMillis() - zoneStartTimeMs;
            long left = Math.max(0, zoneDurationMs - elapsed);
            msg.setZoneState(new ZoneState(zoneCenter, zoneRadius, left));
        }
        msg.setNpcs(new ArrayList<>(npcs));
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

    private int getMaxAmmoForType(ProjectileType type) {
        return switch(type) {
            case SNIPER         -> 1;
            case SHOTGUN_PELLET -> 3;
            case MINE           -> 1;
            case RIFLE_BULLET   -> RIFLE_MAX_AMMO;
            default             -> DEFAULT_MAX_AMMO;
        };
    }
}
