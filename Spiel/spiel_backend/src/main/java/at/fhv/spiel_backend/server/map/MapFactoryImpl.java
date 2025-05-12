package at.fhv.spiel_backend.server.map;

import org.springframework.stereotype.Component;
import at.fhv.spiel_backend.server.map.GameMap;
@Component
public class MapFactoryImpl implements IMapFactory {
    @Override
    public GameMap create(String levelId) {
        return new GameMap(levelId);
    }
}

