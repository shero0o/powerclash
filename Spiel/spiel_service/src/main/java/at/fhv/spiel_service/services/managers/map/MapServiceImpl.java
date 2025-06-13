// src/main/java/at/fhv/spiel_service/services/managers/map/MapServiceImpl.java
package at.fhv.spiel_service.services.managers.map;

import at.fhv.spiel_service.entities.GameMap;
import at.fhv.spiel_service.entities.Position;
import at.fhv.spiel_service.services.managers.crate.ICrateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MapServiceImpl implements IMapService {

    private final ICrateService crateService;

    @Autowired
    public MapServiceImpl(ICrateService crateService) {
        this.crateService = crateService;
    }

    @Override
    public void setGameMap(GameMap map) {
        // initialisiere alle Crates basierend auf den Positionen im Map
        Position[] cratePositions = map.getCratePositions();
        for (Position pos : cratePositions) {
            crateService.spawnInitialCrate(pos);
        }
    }
}
