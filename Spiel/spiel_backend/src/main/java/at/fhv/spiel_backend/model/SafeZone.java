package at.fhv.spiel_backend.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SafeZone {
    Position center;
    float radius;
    float shrinkSpeed;
    float minRadius;

    public void update(float deltaSeconds) {
        if (radius > minRadius) {
            radius = Math.max(minRadius, radius - shrinkSpeed * deltaSeconds);
        }
    }
}

