// src/main/java/at/fhv/spiel_service/messaging/ZoneState.java
package at.fhv.spiel_service.messaging;

import at.fhv.spiel_service.entities.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Repräsentiert den aktuellen Safe-Zone-Status im State-Update.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneState {
    /** Mittelpunkt der Zone (Pixelkoordinaten) */
    private Position center;
    /** Aktueller Radius der Zone */
    private float    radius;
    /** Verbleibende Zeit bis zum kompletten Shrink (in Millisekunden) */
    private long     remainingTimeMs;
}
