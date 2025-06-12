package at.fhv.spiel_service.factoryaaa;


import at.fhv.spiel_service.entities.GameMap;
import org.springframework.stereotype.Component;
@Component
public class MapFactoryImpl implements IMapFactory {
    @Override
    public GameMap create(String levelId) {
        return new GameMap(levelId);
    }
}

