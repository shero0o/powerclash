// src/main/java/at/fhv/spiel_service/services/managers/crate/CrateServiceImpl.java
package at.fhv.spiel_service.services.managers.crate;

import at.fhv.spiel_service.entities.Crate;
import at.fhv.spiel_service.entities.Position;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CrateServiceImpl implements ICrateService {
    private final Map<String, Crate> crates = new ConcurrentHashMap<>();

    @Override
    public void spawnInitialCrate(Position pos) {
        String key = ((int) pos.getX()) + "," + ((int) pos.getY());
        crates.put(key, new Crate(key, pos));
    }

    @Override
    public void handleCrateHit(String key, int damage) {
        Crate c = crates.get(key);
        if (c != null) {
            c.setCurrentHealth(Math.max(0, c.getCurrentHealth() - damage));
            if (c.getCurrentHealth() <= 0) {
                crates.remove(key);
            }
        }
    }

    @Override
    public List<Crate> getAllCrates() {
        return crates.values()
                .stream()
                .collect(Collectors.toList());
    }
}
