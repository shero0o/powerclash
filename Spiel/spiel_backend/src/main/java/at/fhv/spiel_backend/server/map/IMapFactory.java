package at.fhv.spiel_backend.server.map;

public interface IMapFactory {
    GameMap create(String levelId);

}
