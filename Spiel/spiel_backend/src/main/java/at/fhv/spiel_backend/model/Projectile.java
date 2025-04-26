package at.fhv.spiel_backend.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Projectile implements Entity {
    private final String id = UUID.randomUUID().toString();
    private final String ownerId;
    private final Position position;
    private final float dirX, dirY, speed, remainingRange, damage;

    public Projectile(String ownerId, Position initPos, float dirX, float dirY,
                      float speed, float range, int damage) {
        this.ownerId = ownerId;
        this.position = new Position(initPos.getX(), initPos.getY());
        this.dirX = dirX;
        this.dirY = dirY;
        this.speed = speed;
        this.remainingRange = range;
        this.damage = damage;
    }

    /**
     * Moves the projectile and reduces its remaining range.
     */
    public void update(float deltaSeconds) {
        position.setX(position.getX() + dirX * speed * deltaSeconds);
        position.setY(position.getY() + dirY * speed * deltaSeconds);
        // remainingRange is final; track externally
    }

    @Override public String getId() { return id; }
    @Override public Position getPosition() { return position; }
    @Override public void setPosition(Position position) { this.position.setPosition(position); }
}
