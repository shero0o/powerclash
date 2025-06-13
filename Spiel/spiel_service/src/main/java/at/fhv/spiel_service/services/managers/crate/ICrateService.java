// src/main/java/at/fhv/spiel_service/services/managers/crate/ICrateService.java
package at.fhv.spiel_service.services.managers.crate;

import at.fhv.spiel_service.entities.Crate;

import java.util.Collection;

public interface ICrateService {
    void spawnInitialCrate(at.fhv.spiel_service.entities.Position pos);
    void handleCrateHit(String tileKey, int damage);
    Collection<Crate> getAllCrates();
}
