package at.fhv.spiel_service.service.game.manager.projectile;

import at.fhv.spiel_service.domain.ProjectileType;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AmmoManager {

    private static final int DEFAULT_MAX = 3;
    private static final int RIFLE_MAX   = 15;
    private static final long REFILL_MS  = 2000;

    private final Map<String,Integer> ammoMap     = new ConcurrentHashMap<>();
    private final Map<String,Long>    lastRefill  = new ConcurrentHashMap<>();
    private final Map<String,Integer> rifleMap    = new ConcurrentHashMap<>();
    private final Set<String> reloading   = ConcurrentHashMap.newKeySet();
    private final Map<String,Long>    rifleRefill = new ConcurrentHashMap<>();

    public void init(String playerId, ProjectileType w) {
        if (w == ProjectileType.RIFLE_BULLET) {
            rifleMap.put(playerId,RIFLE_MAX);
            rifleRefill.put(playerId, System.currentTimeMillis());
        } else {
            ammoMap.put(playerId, getMax(w));
            lastRefill.put(playerId, System.currentTimeMillis());
        }
    }

    public boolean consume(String playerId, ProjectileType w) {
        if (w == ProjectileType.RIFLE_BULLET) {
            int left = rifleMap.getOrDefault(playerId,RIFLE_MAX);
            if (left<=0) return false;
            rifleMap.put(playerId, left-1);
            if (left-1==0) {
                reloading.add(playerId);
                rifleRefill.put(playerId, System.currentTimeMillis());
            }
            return true;
        } else {
            int left = ammoMap.getOrDefault(playerId,getMax(w));
            if (left<=0) return false;
            ammoMap.put(playerId, left-1);
            return true;
        }
    }

    public void refillAll(float deltaSec) {
        long now = System.currentTimeMillis();
        ammoMap.keySet().forEach(pid -> {
            if (ammoMap.get(pid)<getMax(getWeapon(pid))
                    && now-lastRefill.getOrDefault(pid,0L)>=REFILL_MS) {
                ammoMap.put(pid,getMax(getWeapon(pid)));
                lastRefill.put(pid,now);
            }
        });
        new ArrayList<>(reloading).forEach(pid->{
            if (now-rifleRefill.getOrDefault(pid,0L)>=REFILL_MS) {
                rifleMap.put(pid,RIFLE_MAX);
                reloading.remove(pid);
                rifleRefill.put(pid,now);
            }
        });
    }

    public int getCurrent(String playerId, ProjectileType w) {
        return w==ProjectileType.RIFLE_BULLET
                ? rifleMap.getOrDefault(playerId,RIFLE_MAX)
                : ammoMap.getOrDefault(playerId,getMax(w));
    }

    private int getMax(ProjectileType w) {
        return switch(w){
            case SNIPER, MINE        -> 1;
            case SHOTGUN_PELLET      -> 3;
            case RIFLE_BULLET        -> RIFLE_MAX;
            default                  -> DEFAULT_MAX;
        };
    }
    public void setWeapon(String pid, ProjectileType w){ init(pid,w); }
    public ProjectileType getWeapon(String pid){
        return ProjectileType.RIFLE_BULLET;
    }

    public void removePlayer(String playerId) {
        ammoMap.remove(playerId);
        lastRefill.remove(playerId);
        rifleMap.remove(playerId);
        rifleRefill.remove(playerId);
        reloading.remove(playerId);
    }
}
