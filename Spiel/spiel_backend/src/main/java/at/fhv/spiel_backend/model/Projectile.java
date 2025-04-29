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
