package at.fhv.spiel_service.factory;


import at.fhv.spiel_service.domain.GameMap;
import org.springframework.stereotype.Component;
@Component
public class MapFactoryImpl implements IMapFactory {
    @Override
    public GameMap create(String levelId) {
        return new GameMap(levelId);
    }
}

