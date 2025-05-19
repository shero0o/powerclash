// src/main/java/at/fhv/spiel_backend/model/Bullet.java
package at.fhv.spiel_backend.model;

import java.util.UUID;

public class Bullet {
    private final String id     = UUID.randomUUID().toString();
    private final String shooterId;
    private float x, y, angle;
    public static final float SPEED = 500f; // px/sec

    public Bullet(String shooterId, float x, float y, float angle) {
        this.shooterId = shooterId;
        this.x         = x;
        this.y         = y;
        this.angle     = angle;
    }
    public String getId()        { return id; }
    public String getShooterId(){ return shooterId; }
    public float getX()         { return x; }
    public float getY()         { return y; }
    public float getAngle()     { return angle; }

    /** advance bullet by dt seconds */
    public void update(float dt) {
        x += Math.cos(angle) * SPEED * dt;
        y += Math.sin(angle) * SPEED * dt;
    }
}
