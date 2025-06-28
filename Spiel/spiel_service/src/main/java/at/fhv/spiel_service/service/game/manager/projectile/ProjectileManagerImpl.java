package at.fhv.spiel_service.service.game.manager.projectile;

import at.fhv.spiel_service.domain.*;
import at.fhv.spiel_service.service.game.manager.projectile.behavior.*;

import java.util.*;

public class ProjectileManagerImpl implements ProjectileManager, ProjectileContext {
    private final GameMap gameMap;
    private final Map<String,Player> players;
    private final Map<String,Projectile> projectiles = new HashMap<>();
    private final AmmoManager ammo = new AmmoManager();
    private final Map<ProjectileType, ProjectileBehavior> behaviors = Map.of(
            ProjectileType.RIFLE_BULLET, new RifleBulletBehavior(),
            ProjectileType.SHOTGUN_PELLET, new ShotgunBehavior(),
            ProjectileType.SNIPER,        new SniperBehavior(),
            ProjectileType.MINE,          new MineBehavior()
    );
    private float lastDelta;

    public ProjectileManagerImpl(GameMap gameMap, Map<String,Player> players){
        this.gameMap = gameMap;
        this.players = players;
    }

    @Override
    public void spawnProjectile(String pid, Position pos, Position dir, ProjectileType type) {
        if (!ammo.consume(pid,type)) return;
        behaviors.get(type).spawn(pid,pos,dir,type,this);
    }

    @Override
    public void updateProjectiles(float deltaSec) {
        this.lastDelta = deltaSec;
        ammo.refillAll(deltaSec);
        new ArrayList<>(projectiles.values()).forEach(p-> {
            behaviors.get(p.getProjectileType()).update(p,this);
        });
    }

    @Override public List<Projectile> getProjectiles() {
        return List.copyOf(projectiles.values());
    }
    @Override public int getCurrentAmmo(String pid) {
        ProjectileType w = getCurrentWeapon(pid);
        return ammo.getCurrent(pid,w);
    }
    @Override public ProjectileType getCurrentWeapon(String pid) {
        return ammo.getWeapon(pid);
    }
    @Override public void initPlayer(String pid, ProjectileType w){
        ammo.init(pid,w);
    }

    @Override public void setWeapon(String pid, ProjectileType w){
        ammo.setWeapon(pid,w);
    }
    @Override public void removeProjectileById(String projId){
        projectiles.remove(projId);
    }

    @Override
    public long now() {
        return System.currentTimeMillis();
    }

    @Override
    public float deltaSec() {
        return lastDelta;
    }

    @Override public GameMap getGameMap()     { return gameMap; }
    @Override public Map<String,Player> getPlayers(){ return players; }
    @Override public void addProjectile(Projectile p){
        projectiles.put(p.getId(),p);
    }


}
