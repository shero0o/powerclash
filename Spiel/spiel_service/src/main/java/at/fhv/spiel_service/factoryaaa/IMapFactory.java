package at.fhv.spiel_service.factoryaaa;
import at.fhv.spiel_service.entities.GameMap;

public interface IMapFactory {
    GameMap create(String levelId);

}
