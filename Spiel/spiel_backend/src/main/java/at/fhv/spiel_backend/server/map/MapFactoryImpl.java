package at.fhv.spiel_backend.server.map;

public class MapFactoryImpl {

    @Override
    public GameMap create(String levelId) {
        // Erzeugung einer GameMap basierend auf levelId
        // z.B. aus Konfig oder Datenbank
        return new GameMap(levelId);
    }
}
