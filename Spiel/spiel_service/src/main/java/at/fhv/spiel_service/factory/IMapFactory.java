package at.fhv.spiel_service.factory;
import at.fhv.spiel_service.domain.GameMap;

public interface IMapFactory {
    GameMap create(String levelId);

}
