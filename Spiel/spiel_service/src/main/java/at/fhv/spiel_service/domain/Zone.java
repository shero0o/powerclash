package at.fhv.spiel_service.domain;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Zone {
    private Position center;
    private float    radius;
    private long     timeMsRemaining;

    public Zone(Position center, float radius, long timeMsRemaining) {
        this.center           = center;
        this.radius           = radius;
        this.timeMsRemaining  = timeMsRemaining;
    }

}

