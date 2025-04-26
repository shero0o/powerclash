package at.fhv.spiel_backend.server.map;

public class GameMap {
    private final String id;
    private final int[][] walls;
    private final List<Obstacle> obstacles;

    public GameMap(String id) {
        this.id = id;
        this.walls = new int[0][0]; // Platzhalter
        this.obstacles = List.of();
    }

    public String getId() {
        return id;
    }
}
