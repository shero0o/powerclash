package at.fhv.spiel_backend.model;

import at.fhv.spiel_backend.server.game.IGameRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Projectile implements Entity{

    private String id;
    private String playerId;
    private Position position;
    private Position direction;
    private float speed;
    private int damage;
    private long creationTime;
    private ProjectileType projectileType;
    private float maxRange;                 // max Distanz in px
    private float travelled = 0f;
    private boolean armed = false;
    private long armTime = 0L;  // creationTime + 2000ms

    public Projectile(String id, String playerId, Position position, Position direction, float speed, int damage, long creationTime, ProjectileType projectileType, float maxRange, float travelled) {
        this.id = id;
        this.playerId = playerId;
        this.position = position;
        this.direction = direction;
        this.speed = speed;
        this.damage = damage;
        this.creationTime = creationTime;
        this.projectileType = projectileType;
        this.maxRange = maxRange;
        this.travelled = travelled;
        this.armed = false;
        this.armTime = 0L;
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public void setPosition(Position position) {
        this.position = position;
    }
}
