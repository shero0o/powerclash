package at.fhv.spiel_backend.server.map;
import at.fhv.spiel_backend.server.map.GameMap;

public interface IMapFactory {
    GameMap create(String levelId);

}
