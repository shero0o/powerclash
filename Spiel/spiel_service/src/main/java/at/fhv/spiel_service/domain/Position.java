package at.fhv.spiel_service.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position implements Entity {
    private float x;
    private float y;
    private float angle;

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
        this.angle = 0f;
    }

    @Override
    public String getId() {
        return "position-" + x + "," + y;
    }

    @Override
    public void setPosition(Position position) {
        this.x = position.x;
        this.y = position.y;
        this.angle = position.angle;
    }
    @Override
    @JsonIgnore
    public Position getPosition(){
        return this;
    }


}